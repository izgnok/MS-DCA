import re

logfile = "sample_security_log.txt"

print(f"확인할 로그: {logfile}")

pattern = re.compile(r"failed", re.IGNORECASE)

fails = []
with open(logfile, "r") as f:
    for line in f:
        if pattern.search(line):
            fails.append(line)

if fails:
    print("실패 관련 로그 발견")
    print("마지막 5줄:")
    print("".join(fails[-5:]))
else:
    print("실패 관련 로그 없음")
