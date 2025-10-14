LOGFILE="sample_security_log.txt"

echo "확인할 로그: $LOGFILE"

fails=$(grep -i "failed" "$LOGFILE")

if [ -n "$fails" ]; then
  echo "실패 관련 로그 발견"
  echo "$fails" | mail -s "[경고] 보안 로그 탐지" admin@example.com
else
  echo "실패 관련 로그 없음"
fi