#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SCHEMA_SQL="$ROOT_DIR/db/kingbase_schema.sql"
BRIDGE_SQL="$ROOT_DIR/db/kingbase_backend_bridge.sql"
SAMPLE_SQL="$ROOT_DIR/db/campus_sample_data.sql"

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-54321}"
DB_NAME="${DB_NAME:-student_service_platform}"
DB_USER="${DB_USER:-postgres}"
DB_PASSWORD="${DB_PASSWORD:-postgres}"
DB_SCHEMA="${DB_SCHEMA:-campus}"
LOAD_SAMPLE_DATA="${LOAD_SAMPLE_DATA:-true}"

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "missing required command: $1" >&2
    exit 1
  fi
}

require_cmd psql

if [[ ! -f "$SCHEMA_SQL" ]]; then
  echo "schema sql not found: $SCHEMA_SQL" >&2
  exit 1
fi

if [[ ! -f "$BRIDGE_SQL" ]]; then
  echo "bridge sql not found: $BRIDGE_SQL" >&2
  exit 1
fi

export PGPASSWORD="$DB_PASSWORD"

PSQL_BASE=(
  psql
  -h "$DB_HOST"
  -p "$DB_PORT"
  -U "$DB_USER"
  -d "$DB_NAME"
  -v ON_ERROR_STOP=1
)

echo "[1/4] ensure schema $DB_SCHEMA"
"${PSQL_BASE[@]}" -c "CREATE SCHEMA IF NOT EXISTS $DB_SCHEMA;"

echo "[2/4] apply latest schema"
"${PSQL_BASE[@]}" -f "$SCHEMA_SQL"

if [[ "$LOAD_SAMPLE_DATA" == "true" ]]; then
  if [[ ! -f "$SAMPLE_SQL" ]]; then
    echo "sample sql not found: $SAMPLE_SQL" >&2
    exit 1
  fi
  echo "[3/5] load sample data"
  "${PSQL_BASE[@]}" -f "$SAMPLE_SQL"
else
  echo "[3/5] skip sample data"
fi

echo "[4/5] apply backend bridge"
"${PSQL_BASE[@]}" -f "$BRIDGE_SQL"

echo "[5/5] done"
echo "database bridge initialized for schema: $DB_SCHEMA"
