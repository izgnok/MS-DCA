TARGET="/"
[ -d /System/Volumes/Data ] && TARGET="/System/Volumes/Data"

percent=$(df -k "$TARGET" | awk 'NR==2 {gsub("%","",$5); print $5}')

echo "현재 전체 디스크 사용량: ${percent}%"

if [ "$percent" -ge 80 ]; then
  echo "경고: 전체 사용량이 ${percent}% 입니다."

  curl --url "smtp://smtp.gmail.com:587" \
       --ssl-reqd \
       --mail-from "ggoreb.kim@gmail.com" \
       --mail-rcpt "seorab@naver.com" \
       --upload-file <(echo -e "From: ggoreb.kim@gmail.com\nTo: seorab@naver.com\nSubject: 디스크 경고 메일\n\n현재 전체 디스크 사용량이 ${percent}% 입니다. 점검이 필요합니다.") \
       --user "ggoreb.kim@gmail.com:tbngkagsivcanyjf"

else
  echo "정상: 전체 사용량이 ${percent}% 입니다."
fi
