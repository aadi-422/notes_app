#!/bin/sh
set -e
echo "=== notes-api startup ==="
echo "SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-<unset>}"
echo "DB_HOST=${DB_HOST:-<not set>}"
echo "DB_PORT=${DB_PORT:-<not set>}"
echo "DB_NAME=${DB_NAME:-<not set>}"
echo "DB_USER=${DB_USER:-<not set>}"
if [ -n "$DATABASE_URL" ]; then
  echo "DATABASE_URL=<set>"
else
  echo "DATABASE_URL=<not set>"
fi

if [ -z "$DATABASE_URL" ] && [ -z "$DB_HOST" ]; then
  echo ""
  echo "FATAL: No database credentials on this service."
  echo "  1. Render Dashboard -> notes-db -> Connect -> choose notes-api -> Connect"
  echo "  OR"
  echo "  2. notes-api -> Environment -> Add from database -> notes-db -> Save"
  echo "  OR"
  echo "  3. Blueprints -> Sync (if using render.yaml)"
  echo ""
  exit 1
fi

exec java -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar
