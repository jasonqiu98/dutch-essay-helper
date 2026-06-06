#!/usr/bin/env bash
set -euo pipefail

API_URL="${API_URL:-http://localhost:8080/api/essays/correct}"
ESSAY_FILE="${ESSAY_FILE:-examples/aantrekkelijker-busvervoer.txt}"

TASK_TYPE="${TASK_TYPE:-formal_letter}"
WRITING_PROMPT="${WRITING_PROMPT:-A passenger writes to the manager of a bus company and gives feedback on a publicly announced plan to make bus transport more attractive. The passenger gives opinions and suggestions.}"

if [ ! -f "$ESSAY_FILE" ]; then
  echo "Essay file not found: $ESSAY_FILE" >&2
  echo "Create the file first, or run with:" >&2
  echo "  ESSAY_FILE=path/to/your-file.txt ./run.sh" >&2
  exit 1
fi

if ! command -v python3 >/dev/null 2>&1; then
  echo "python3 is required to safely build the JSON request body." >&2
  exit 1
fi

REQUEST_BODY="$(python3 - "$TASK_TYPE" "$WRITING_PROMPT" "$ESSAY_FILE" <<'PY'
import json
import sys
from pathlib import Path

task_type = sys.argv[1]
writing_prompt = sys.argv[2]
essay_file = Path(sys.argv[3])

essay = essay_file.read_text(encoding="utf-8")

payload = {
    "taskType": task_type,
    "prompt": writing_prompt,
    "essay": essay,
}

print(json.dumps(payload, ensure_ascii=False))
PY
)"

echo "Sending essay to: $API_URL"
echo "Essay file: $ESSAY_FILE"
echo

curl -sS -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d "$REQUEST_BODY"

echo
