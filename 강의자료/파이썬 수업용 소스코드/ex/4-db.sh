DB="test1.db"

sqlite3 $DB <<EOF
CREATE TABLE IF NOT EXISTS users(
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT,
  age INTEGER
);
INSERT INTO users (name, age) VALUES ('Alice', 23);
INSERT INTO users (name, age) VALUES ('Bob', 30);
EOF

sqlite3 -header -csv $DB "SELECT * FROM users;" > report1.csv

echo "report1.csv 생성 완료"
