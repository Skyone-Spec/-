#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
USERNAME="${SMOKE_USERNAME:-admin}"
PASSWORD="${SMOKE_PASSWORD:-123456}"

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "missing required command: $1" >&2
    exit 1
  fi
}

require_cmd curl
require_cmd python3

echo "[1/6] health"
curl -fsS "$BASE_URL/actuator/health" >/tmp/student_service_health.json
cat /tmp/student_service_health.json
echo

echo "[2/6] login"
LOGIN_RESPONSE="$(curl -fsS -H 'Content-Type: application/json' \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}" \
  "$BASE_URL/api/v1/auth/login")"
echo "$LOGIN_RESPONSE"
TOKEN="$(printf '%s' "$LOGIN_RESPONSE" | python3 -c 'import json,sys; print(json.load(sys.stdin)["data"]["token"])')"

AUTH_HEADER="Authorization: Bearer $TOKEN"

echo "[3/6] current user"
curl -fsS -H "$AUTH_HEADER" "$BASE_URL/api/v1/auth/me"
echo

echo "[4/6] knowledge search"
curl -fsS -H "$AUTH_HEADER" "$BASE_URL/api/v1/knowledge/search?keyword=%E5%85%9A%E5%91%98"
echo

echo "[5/6] admin stats"
curl -fsS -H "$AUTH_HEADER" "$BASE_URL/api/v1/admin/stats"
echo

echo "[6/6] student notices"
curl -fsS -H "$AUTH_HEADER" "$BASE_URL/api/v1/notices/student/10001"
echo

echo "smoke test passed"
