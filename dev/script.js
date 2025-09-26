import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
    vus: 10,         // 가상 사용자 수
    duration: '30s', // 실행 시간
};

export default function () {
    let res = http.get('http://dca-backend.default.svc.cluster.local:8080/backend/api/play');
    check(res, { 'status was 200': (r) => r.status === 200 });
    sleep(1);
}