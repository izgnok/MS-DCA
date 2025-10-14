import sqlite3
import csv

conn = sqlite3.connect("test2.db")
cursor = conn.cursor()
cursor.execute("""
CREATE TABLE IF NOT EXISTS users(
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT,
  age INTEGER
)
""")

cursor.executemany(
    "INSERT INTO users (name, age) VALUES (?, ?)",
    [("Alice", 23), ("Bob", 30), ("Charlie", 27)]
)
conn.commit()

cursor.execute("SELECT * FROM users")
rows = cursor.fetchall()

with open("report2.csv", "w", newline="", encoding="utf-8") as f:
    writer = csv.writer(f)
    writer.writerow(["id", "name", "age"])
    writer.writerows(rows)

print("report2.csv 생성 완료")

cursor.close()
conn.close()
