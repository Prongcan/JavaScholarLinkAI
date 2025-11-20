"""
Simple smoke test for DbManager.

This script will:
1) Connect to DB and ping
2) Insert a test user and a test paper
3) Insert a recommendation linking them
4) Query back the joined result
5) Update the user's interest
6) Clean up all inserted rows

Run:
    python backend/service/test_db_manager.py

It uses the same configuration resolution as DbManager:
config.yaml > .env/env vars > defaults
"""
from __future__ import annotations

import time
from typing import Any, Dict

import os, sys
CURRENT_DIR = os.path.dirname(__file__)
BACKEND_DIR = os.path.abspath(os.path.join(CURRENT_DIR, ".."))
if BACKEND_DIR not in sys.path:
    sys.path.insert(0, BACKEND_DIR)
from service.dbmanager import DbManager


def main() -> None:
    db = DbManager()

    print("[1/6] Pinging database...")
    assert db.ping(), "Cannot connect to database. Check config.yaml/.env and MySQL service."
    print("    OK")

    # Generate unique names to avoid unique-key conflicts
    suffix = str(int(time.time()))
    username = f"test_user_{suffix}"
    paper_title = f"Test Paper {suffix}"

    user_id = None
    paper_id = None
    rec_id = None

    try:
        print("[2/6] Inserting test user and paper...")
        res_u = db.execute(
            "INSERT INTO users(username, password, interest) VALUES(%s,%s,%s)",
            (username, "pwd123", "AI"),
        )
        user_id = res_u["lastrowid"]
        assert user_id, "Failed to insert user"

        res_p = db.execute(
            "INSERT INTO papers(title, abstract, pdf_url, author) VALUES(%s,%s,%s,%s)",
            (paper_title, "This is a test abstract.", "http://example.com/test.pdf", "Tester"),
        )
        paper_id = res_p["lastrowid"]
        assert paper_id, "Failed to insert paper"
        print(f"    user_id={user_id}, paper_id={paper_id}")

        print("[3/6] Inserting recommendation...")
        res_r = db.execute(
            "INSERT INTO recommendations(user_id, paper_id, blog) VALUES(%s,%s,%s)",
            (user_id, paper_id, "Nice paper for testing."),
        )
        rec_id = res_r["lastrowid"]
        assert rec_id, "Failed to insert recommendation"
        print(f"    rec_id={rec_id}")

        print("[4/6] Querying joined result...")
        row = db.query_one(
            """
            SELECT r.id, u.username, p.title, r.blog
            FROM recommendations r
            JOIN users u ON r.user_id = u.user_id
            JOIN papers p ON r.paper_id = p.paper_id
            WHERE r.id = %s
            """,
            (rec_id,),
        )
        assert row and row["username"] == username and row["title"] == paper_title, "Joined query mismatch"
        print(f"    row: {row}")

        print("[5/6] Updating user's interest...")
        res_upd = db.execute(
            "UPDATE users SET interest=%s WHERE user_id=%s",
            ("NLP", user_id),
        )
        assert res_upd["rowcount"] == 1, "Update interest failed"
        row_user = db.query_one("SELECT interest FROM users WHERE user_id=%s", (user_id,))
        assert row_user and row_user["interest"] == "NLP", "Interest not updated"
        print("    Updated and verified")

        print("[OK] All checks passed.")

    finally:
        print("[6/6] Cleaning up test data...")
        if rec_id:
            db.execute("DELETE FROM recommendations WHERE id=%s", (rec_id,))
        if user_id:
            db.execute("DELETE FROM users WHERE user_id=%s", (user_id,))
        if paper_id:
            db.execute("DELETE FROM papers WHERE paper_id=%s", (paper_id,))
        print("    Cleanup done.")


if __name__ == "__main__":
    main()

