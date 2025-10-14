import os
import psutil
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

path = "/System/Volumes/Data" \
  if os.path.exists("/System/Volumes/Data") else "/"

usage = psutil.disk_usage(path)
percent = int(usage.percent)

print(f"현재 전체 디스크 사용량: {percent}%")

SMTP_SERVER = "smtp.gmail.com"
SMTP_PORT = 587
SENDER = "ggoreb.kim@gmail.com"
PASSWORD = "tbngkagsivcanyjf"
RECIPIENT = "seorab@naver.com"

def send_email(subject, body):
    msg = MIMEMultipart()
    msg["From"] = SENDER
    msg["To"] = RECIPIENT
    msg["Subject"] = subject
    msg.attach(MIMEText(body, "html"))

    with smtplib.SMTP(SMTP_SERVER, SMTP_PORT) as s:
        s.starttls()
        s.login(SENDER, PASSWORD)
        s.sendmail(SENDER, RECIPIENT, msg.as_string())

if percent >= 80:
    print(f"경고: 전체 사용량이 {percent}% 입니다.")

    subject = f"[경고] 디스크 사용량 {percent}% 초과"
    body = f"""<h3>디스크 사용량 경고</h3>
    <p>현재 전체 디스크 사용량은 <b>{percent}%</b> 입니다.<br>
    서버 점검이 필요합니다.</p>"""
    send_email(subject, body)
    print("이메일 발송 완료")

else:
    print(f"정상: 전체 사용량이 {percent}% 입니다.")
