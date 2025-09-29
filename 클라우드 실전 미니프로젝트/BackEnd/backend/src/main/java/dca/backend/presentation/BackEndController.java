package dca.backend.presentation;

import dca.backend.application.BackEndService;
import dca.backend.common.module.response.ResponseDto;
import dca.backend.common.module.status.StatusCode;
import dca.backend.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/backend/api")
public class BackEndController {

    private final BackEndService backEndService;


    // 성능부하테스트용 더미 API
    @PostMapping("/play")
    public ResponseEntity<ResponseDto> play() {
        backEndService.play();
        return ResponseDto.response(StatusCode.SUCCESS, "연산 완료");
    }


    /** 연도별 영화 조회 */
    @PostMapping("/movie/year")
    public ResponseEntity<ResponseDto> getMoviesByYear(@RequestBody MovieByYearRequest request) {
        MoviePageResponse movies = backEndService.getMoviesByYear(request);
        return ResponseDto.response(StatusCode.SUCCESS, movies);
    }

    /** 영화 검색 (제목/감독/배우 LIKE) */
    @PostMapping("/movie/search")
    public ResponseEntity<ResponseDto> searchMovies(@RequestBody MovieSearchRequest request) {
        MoviePageResponse movies = backEndService.searchMovies(request);
        return ResponseDto.response(StatusCode.SUCCESS, movies);
    }

    /** 영화 상세 조회 */
    @GetMapping("/movie/{movieSeq}")
    public ResponseEntity<ResponseDto> getMovieDetail(@PathVariable Integer movieSeq) {
        MovieDetailResponse movie = backEndService.getMovieDetail(movieSeq);
        return ResponseDto.response(StatusCode.SUCCESS, movie);
    }




//    // k6 요청 API
//    @PostMapping("/k6")
//    public ResponseEntity<ResponseDto> k6() {
//        backEndService.runK6("docker");
//        backEndService.runK6("kubernetes");
//        return ResponseDto.response(StatusCode.SUCCESS, "테스트 성공");
//    }
//
//    // 결과 조회
//    @GetMapping("/k6/results")
//    public ResponseEntity<ResponseDto> getK6Results() {
//        return ResponseDto.response(StatusCode.SUCCESS, backEndService.getRecentResults());
//    }

    // 더미데이터 삽입 API
//    @PostMapping("/insert")
//    public ResponseEntity<ResponseDto> insertDummyData() {
//        backEndService.insertDummyData();
//        return ResponseDto.response(StatusCode.SUCCESS, "더미 데이터 삽입 완료");
//    }
}
