#!/bin/sh
set -e
echo "=== notes-api startup ==="
echo "SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-<unset>}"
echo "DB_HOST=${DB_HOST:-<unset>} (use internal host, e.g. dpg-xxxxx-a)"
echo "DATABASE_URL=${DATABASE_URL:+<set — remove if external; prefer DB_HOST>}${DATABASE_URL:-<not set>}"
echo "DB_PORT=${DB_PORT:-<unset>}"
echo "DB_NAME=${DB_NAME:-<unset>}"
echo "DB_USER=${DB_USER:-<unset>}"
if [ -z "$DATABASE_URL" ] && [ -z "$DB_HOST" ]; then
  echo "ERROR: Link notes-db to this service in Render (Environment -> Add from database)"
fi
exec java -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar
