import requests
import datetime

servers = ["https://google.com", "https://naver.com", "https://daum.net"]
logfile = "http_log.txt"

for server in servers:
    print(f"🔎 서버: {server}")
    try:
        res = requests.get(server, timeout=5)
        status = res.status_code
        elapsed = res.elapsed.total_seconds()

        if status == 200:
            print(f"상태 코드: {status}, 응답 시간: {elapsed:.3f}s")
            with open(logfile, "a") as f:
                f.write(f"{datetime.datetime.now()} - {server} 상태코드: {status}, 응답시간: {elapsed:.3f}s\n")
        else:
            print(f"오류 상태 코드: {status}")
            with open(logfile, "a") as f:
                f.write(f"{datetime.datetime.now()} - {server} 오류 상태코드: {status}\n")
    except requests.exceptions.RequestException as e:
        print(f"응답 없음: {e}")
        with open(logfile, "a") as f:
            f.write(f"{datetime.datetime.now()} - {server} 응답 없음: {e}\n")
