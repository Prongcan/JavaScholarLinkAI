"""
Test script for Embedding class moved from service/embedding.py __main__.

Usage:
  - From project root (preferred):
      python -m backend.test.test_embedding "Hello, ScholarLinkAI!"
  - Or run directly:
      python backend/test/test_embedding.py "Hello, ScholarLinkAI!"
"""
from __future__ import annotations

import os
import sys
import argparse

# Ensure we can import from backend/service when run directly
CURRENT_DIR = os.path.dirname(__file__)
BACKEND_DIR = os.path.abspath(os.path.join(CURRENT_DIR, ".."))
if BACKEND_DIR not in sys.path:
    sys.path.insert(0, BACKEND_DIR)

from service.embedding import Embedding  # noqa: E402


def main():
    parser = argparse.ArgumentParser(description="Test OpenAI Embedding")
    parser.add_argument("text", nargs=argparse.REMAINDER, help="Text to embed")
    args = parser.parse_args()

    text = " ".join(args.text).strip() or "Hello, ScholarLinkAI!"

    emb = Embedding()

    # Prepare example texts
    ex1 = "hello, Scholar"
    ex2 = "hello, sir"

    # Batch embed to reduce API calls and keep vectors aligned
    v_input, v_ex1, v_ex2 = emb.embed_texts([text, ex1, ex2], normalize=True)

    def cosine(a, b):
        return sum(x * y for x, y in zip(a, b))

    # Print basic info
    print("Embeddings info:")
    print(f"  input:  '{text}'\n    len={len(v_input)} first10={v_input[:10]}")
    print(f"  ex1  :  '{ex1}'\n    len={len(v_ex1)} first10={v_ex1[:10]}")
    print(f"  ex2  :  '{ex2}'\n    len={len(v_ex2)} first10={v_ex2[:10]}")

    # Cosine similarities (since vectors are normalized, dot == cosine)
    sim_input_ex1 = cosine(v_input, v_ex1)
    sim_input_ex2 = cosine(v_input, v_ex2)
    sim_ex1_ex2   = cosine(v_ex1, v_ex2)

    print("\nCosine similarities (higher => more similar):")
    print(f"  cos(input, ex1='hello, Scholar') = {sim_input_ex1:.4f}")
    print(f"  cos(input, ex2='hello, sir')     = {sim_input_ex2:.4f}")
    print(f"  cos(ex1, ex2)                    = {sim_ex1_ex2:.4f}")


if __name__ == "__main__":
    main()

