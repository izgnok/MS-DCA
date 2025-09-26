package dca.backend.application;

import dca.backend.common.module.exception.RestApiException;
import dca.backend.common.module.status.StatusCode;
import dca.backend.dto.K6ResultResponse;
import dca.backend.entity.K6Result;
import dca.backend.entity.Review;
import dca.backend.entity.User;
import dca.backend.infrastructure.BackEndRepository;
import dca.backend.infrastructure.ReviewRepository;
import dca.backend.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
public class BackEndService {

    private final BackEndRepository backEndRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    private static final String NAMESPACE = "default";
    private static final String K6_IMAGE = "grafana/k6:latest";
    private static final String K6_SCRIPT_CM = "k6-test-scripts";
    private static final String K6_SCRIPT_PATH = "/scripts/test.js";
    private static final long   JOB_TIMEOUT_SEC = 180;
    private static final long   POD_LOG_WAIT_SEC = 15;
    private static final boolean CLEANUP_JOB_AFTER_DONE = true;

    private static final Pattern RX_CHECKS =
            Pattern.compile("checks_succeeded.*?:\\s*([0-9.]+)%\\s*(\\d+)\\s*(?:out of|/)?\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern RX_HTTP_FAILED =
            Pattern.compile("http_req_failed.*?:\\s*([0-9.]+)%", Pattern.CASE_INSENSITIVE);

    private static final Pattern RX_HTTP_DURATION_AVG =
            Pattern.compile("http_req_duration.*?avg=\\s*([0-9.]+)\\s*(ms|s)", Pattern.CASE_INSENSITIVE);

    @Transactional
    public void play() {
        Random random = new Random();
        log.info("▶ play() 시작");

        List<Integer> topUsers = reviewRepository.findTop10UsersByReviewCount();
        log.info("▶ 상위 유저 seq: {}", topUsers);

        try {
            for (int userSeq : topUsers) {
                User user = userRepository.findById(userSeq).orElseThrow();
                List<Review> reviews = user.getReviews();
                log.info(" - User {} 리뷰 수: {}", userSeq, reviews.size());
                if (reviews.isEmpty()) {
                    log.warn(" - User {} 리뷰 없음 → skip", userSeq);
                    continue;
                }

                Review review = Review.builder()
                        .content("부하테스트 리뷰 " + UUID.randomUUID())
                        .createdAt(LocalDateTime.now())
                        .isActive(true)      // ✅ Boolean 수정
                        .isSpoiler(false)    // ✅ Boolean 수정
                        .likes(random.nextInt(100))
                        .movieSeq(reviews.get(0).getMovieSeq())
                        .reviewRating(ThreadLocalRandom.current().nextDouble(1.0, 5.0))
                        .sentimentScore(ThreadLocalRandom.current().nextDouble(-1.0, 1.0))
                        .user(user)
                        .build();

                reviewRepository.save(review);
                log.debug(" - User {} 리뷰 저장 OK", userSeq);
            }
            log.info("▶ play() 완료");
        } catch (Exception e) {
            log.error("❌ play() 실패", e);
            throw new RestApiException(StatusCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Transactional
    public void runK6(String category) {
        String jobName = "k6-load-test-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("▶ runK6 시작: category={}, jobName={}", category, jobName);

        ApiClient client = buildApiClient();
        Configuration.setDefaultApiClient(client);

        BatchV1Api batchApi = new BatchV1Api(client);
        CoreV1Api  coreApi  = new CoreV1Api(client);

        try {
            V1Job job = buildK6Job(jobName, category);

            // ✅ 최신 시그니처 반영 (6개 인자 필요)
            batchApi.createNamespacedJob(NAMESPACE, job, null, null, null, null);
            log.info(" - Job 생성 완료: {}", jobName);

            waitForJobCompletion(batchApi, jobName, Duration.ofSeconds(JOB_TIMEOUT_SEC));

            String podName = waitAndGetFirstPodName(coreApi, jobName, Duration.ofSeconds(POD_LOG_WAIT_SEC));
            String logs = readFullPodLogs(coreApi, podName);
            log.debug("▼▼▼ K6 RAW LOGS ({} lines) ▼▼▼\n{}\n▲▲▲", logs.split("\n").length, logs);

            K6Parsed parsed = parseK6Logs(logs);
            K6Result result = K6Result.builder()
                    .category(category)
                    .requestCount(parsed.checksSucceeded)
                    .avgResponseTime(parsed.avgDurationSec)
                    .errorRate(parsed.errorRate)
                    .executedAt(LocalDateTime.now().toString())
                    .build();

            backEndRepository.save(result);
            log.info("▶ runK6 저장 완료: {}", result);

        } catch (Exception e) {
            log.error("❌ runK6 실패: {}", e.getMessage(), e);
            throw new RestApiException(StatusCode.INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            if (CLEANUP_JOB_AFTER_DONE) {
                try {
                    deleteJobAndPods(batchApi, coreApi, jobName);
                    log.info(" - Job/POD 정리 완료: {}", jobName);
                } catch (Exception cleanEx) {
                    log.warn(" - 정리 중 경고: {}", cleanEx.getMessage());
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public List<K6ResultResponse> getRecentResults() {
        log.info("▶ 최근 결과 조회");

        List<K6Result> dockerResults = backEndRepository.findTop5ByCategoryOrderByExecutedAtDesc("docker");
        List<K6Result> kubeResults   = backEndRepository.findTop5ByCategoryOrderByExecutedAtDesc("kubernetes");
        log.debug(" - docker={}, kube={}", dockerResults.size(), kubeResults.size());

        return Stream.concat(dockerResults.stream(), kubeResults.stream())
                .map(r -> K6ResultResponse.builder()
                        .category(r.getCategory())
                        .requestCount(r.getRequestCount())
                        .avgResponseTime(r.getAvgResponseTime())
                        .errorRate(r.getErrorRate())
                        .executedAt(r.getExecutedAt())
                        .build())
                .toList();
    }

    private ApiClient buildApiClient() {
        try {
            try {
                log.debug(" - ApiClient: in-cluster 로드 시도");
                return Config.fromCluster();
            } catch (Exception inClusterFail) {
                log.debug(" - ApiClient: kubeconfig 로드 시도");
                ApiClient client = Config.defaultClient();
                client.setReadTimeout((int) Duration.ofSeconds(JOB_TIMEOUT_SEC + POD_LOG_WAIT_SEC + 30).toMillis());
                return client;
            }
        } catch (Exception e) {
            throw new RestApiException(StatusCode.INTERNAL_SERVER_ERROR, "K8S ApiClient 생성 실패: " + e.getMessage());
        }
    }

    private V1Job buildK6Job(String jobName, String category) {
        V1PodSpec podSpec = new V1PodSpec()
                .restartPolicy("Never")
                .containers(List.of(
                        new V1Container()
                                .name("k6")
                                .image(K6_IMAGE)
                                .imagePullPolicy("IfNotPresent")
                                .command(List.of("k6", "run", K6_SCRIPT_PATH))
                                .env(List.of(
                                        new V1EnvVar().name("CATEGORY").value(category)
                                ))
                                .volumeMounts(List.of(
                                        new V1VolumeMount().name("k6-scripts").mountPath("/scripts")
                                ))
                ))
                .volumes(List.of(
                        new V1Volume()
                                .name("k6-scripts")
                                .configMap(new V1ConfigMapVolumeSource().name(K6_SCRIPT_CM))
                ));

        V1PodTemplateSpec template = new V1PodTemplateSpec()
                .metadata(new V1ObjectMeta()
                        .name(jobName + "-pod")
                        .labels(Map.of("job-name", jobName, "app", "k6-load-test")))
                .spec(podSpec);

        V1JobSpec jobSpec = new V1JobSpec()
                .template(template)
                .backoffLimit(0)
                .ttlSecondsAfterFinished(300);

        return new V1Job()
                .apiVersion("batch/v1")
                .kind("Job")
                .metadata(new V1ObjectMeta()
                        .name(jobName)
                        .namespace(NAMESPACE)
                        .labels(Map.of("app", "k6-load-test")))
                .spec(jobSpec);
    }

    private void waitForJobCompletion(BatchV1Api batchApi, String jobName, Duration timeout) throws Exception {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        log.info(" - Job 완료 대기 (timeout={}s)", timeout.getSeconds());

        while (System.currentTimeMillis() < deadline) {
            V1Job j = batchApi.readNamespacedJob(jobName, NAMESPACE, null); // ✅ 시그니처 최신화
            V1JobStatus st = j.getStatus();
            Integer succeeded = st.getSucceeded();
            Integer failed    = st.getFailed();
            Integer active    = st.getActive();

            log.debug("   · Job 상태: active={}, succeeded={}, failed={}", n(active), n(succeeded), n(failed));

            if (succeeded != null && succeeded > 0) return;
            if (failed != null && failed > 0) {
                throw new RestApiException(StatusCode.INTERNAL_SERVER_ERROR, "k6 Job 실패 (failed=" + failed + ")");
            }
            Thread.sleep(1000);
        }
        throw new RestApiException(StatusCode.INTERNAL_SERVER_ERROR, "k6 Job 타임아웃"); // ✅ REQUEST_TIMEOUT → INTERNAL_SERVER_ERROR
    }

    private String waitAndGetFirstPodName(CoreV1Api coreApi, String jobName, Duration timeout) throws Exception {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            V1PodList pods = coreApi.listNamespacedPod(
                    NAMESPACE, null, null, null, null,
                    "job-name=" + jobName, null, null, null, null, null
            );

            if (!pods.getItems().isEmpty()) {
                String podName = pods.getItems().get(0).getMetadata().getName();
                log.info(" - Job Pod 발견: {}", podName);
                return podName;
            }
            Thread.sleep(1000);
        }
        throw new RestApiException(StatusCode.INTERNAL_SERVER_ERROR, "Job Pod 생성 대기 타임아웃"); // ✅ 수정
    }

    private String readFullPodLogs(CoreV1Api coreApi, String podName) throws ApiException {
        return coreApi.readNamespacedPodLog(
                podName, NAMESPACE,
                null, null, null,
                null, null, null,
                null, null, null
        );
    }

    private K6Parsed parseK6Logs(String logs) {
        long checksSucceeded = 0;
        double errorRate = 0.0;
        double avgDurationSec = 0.0;

        Matcher m1 = RX_CHECKS.matcher(logs);
        if (m1.find()) {
            try { checksSucceeded = Long.parseLong(m1.group(2)); } catch (Exception ignore) {}
            log.info(" - 파싱: checks_succeeded = {}", checksSucceeded);
        }

        Matcher m2 = RX_HTTP_FAILED.matcher(logs);
        if (m2.find()) {
            try { errorRate = Double.parseDouble(m2.group(1)) / 100.0; } catch (Exception ignore) {}
            log.info(" - 파싱: http_req_failed (errorRate) = {}", errorRate);
        }

        Matcher m3 = RX_HTTP_DURATION_AVG.matcher(logs);
        if (m3.find()) {
            try {
                double val = Double.parseDouble(m3.group(1));
                String unit = m3.group(2);
                avgDurationSec = "ms".equalsIgnoreCase(unit) ? val / 1000.0 : val;
            } catch (Exception ignore) {}
            log.info(" - 파싱: http_req_duration avg(sec) = {}", avgDurationSec);
        }

        return new K6Parsed(checksSucceeded, errorRate, avgDurationSec);
    }

    private void deleteJobAndPods(BatchV1Api batchApi, CoreV1Api coreApi, String jobName) throws Exception {
        V1DeleteOptions opts = new V1DeleteOptions().propagationPolicy("Foreground");
        try {
            batchApi.deleteNamespacedJob(jobName, NAMESPACE, null, null, null, null, null, opts); // ✅ 최신 시그니처 반영
        } catch (ApiException e) {
            if (e.getCode() != 404) throw e;
        }

        try {
            V1PodList pods = coreApi.listNamespacedPod(
                    NAMESPACE, null, null, null, null,
                    "job-name=" + jobName, null, null, null, null, null
            );
            for (V1Pod p : pods.getItems()) {
                String podName = p.getMetadata().getName();
                try {
                    coreApi.deleteNamespacedPod(podName, NAMESPACE, null, null, null, null, null, null); // ✅ 최신 시그니처 반영
                } catch (ApiException e) {
                    if (e.getCode() != 404) throw e;
                }
            }
        } catch (ApiException e) {
            if (e.getCode() != 404) throw e;
        }
    }

    private static Integer n(Integer v) { return v == null ? 0 : v; }

    private record K6Parsed(long checksSucceeded, double errorRate, double avgDurationSec) {}
}