#!/usr/bin/env python3
"""按顺序执行 scripts 下 MySQL 迁移（可重复执行，跳过已存在对象）。"""

from __future__ import annotations

import re
import sys
from pathlib import Path

import pymysql

ROOT = Path(__file__).resolve().parent.parent
SCRIPTS = [
    "mysql-users-onboarding-profile.sql",
    "mysql-ai-chat.sql",
    "mysql-ai-chat-task-reminder.sql",
    "mysql-user-assistant-tasks-due-at.sql",
    "mysql-user-media-assets.sql",
    "mysql-in-app-notifications.sql",
    "mysql-user-llm-settings.sql",
    "mysql-scheduler-lock.sql",
    "mysql-user-assistant-tasks-reminder-index.sql",
]

# 与 application-dev.yaml 默认一致；可用环境变量覆盖
DEFAULT = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "kx051019",
    "database": "ai_gp",
}


def load_dev_password() -> str:
    dev = ROOT / "src/main/resources/application-dev.yaml"
    if not dev.is_file():
        return DEFAULT["password"]
    text = dev.read_text(encoding="utf-8")
    m = re.search(r"password:\s*\$\{MYSQL_PASSWORD:([^}]+)\}", text)
    return m.group(1) if m else DEFAULT["password"]


def split_statements(sql: str) -> list[str]:
    """按分号拆成单条语句（忽略纯注释行）。"""
    cleaned = []
    for line in sql.splitlines():
        if line.strip().startswith("--"):
            continue
        cleaned.append(line)
    blob = "\n".join(cleaned)
    parts = []
    for chunk in blob.split(";"):
        stmt = chunk.strip()
        if stmt:
            parts.append(stmt + ";")
    return parts


def run_file(cur, path: Path) -> None:
    print(f"\n--- {path.name} ---")
    sql = path.read_text(encoding="utf-8")
    for stmt in split_statements(sql):
        try:
            cur.execute(stmt)
            head = stmt.split("\n", 1)[0].strip()[:80]
            print(f"  OK: {head}...")
        except pymysql.MySQLError as e:
            if e.args[0] in (1060, 1050):  # 列/表已存在
                print(f"  SKIP: {e}")
                continue
            raise


def main() -> int:
    password = load_dev_password()
    conn = pymysql.connect(
        host=DEFAULT["host"],
        port=DEFAULT["port"],
        user=DEFAULT["user"],
        password=password,
        database=DEFAULT["database"],
        charset="utf8mb4",
        autocommit=True,
    )
    try:
        with conn.cursor() as cur:
            for name in SCRIPTS:
                path = ROOT / "scripts" / name
                if not path.is_file():
                    print(f"Missing: {path}", file=sys.stderr)
                    return 1
                run_file(cur, path)
        print("\nAll migrations finished.")
        return 0
    finally:
        conn.close()


if __name__ == "__main__":
    sys.exit(main())
