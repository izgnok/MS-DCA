servers=("https://google.com" "https://naver.com" "https://daum.net")

for server in "${servers[@]}"; do
  echo "🔎 서버: $server"

  result=$(curl -L -o /dev/null -s -w "%{http_code} %{time_total}\n" "$server")
  http_status=$(echo $result | awk '{print $1}')
  time=$(echo $result | awk '{print $2}')

  if [ "$http_status" -eq 200 ]; then
    echo "상태 코드: $http_status, 응답 시간: ${time}s"
    echo "$(date) - $server 상태코드: $http_status, 응답시간: ${time}s" >> http_log.txt
  else
    echo "응답 없음 또는 오류 (상태 코드: $http_status)"
    echo "$(date) - $server 오류 상태코드: $http_status" >> http_log.txt
  fi
done
