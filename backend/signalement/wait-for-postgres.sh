#!/bin/bash
set -e

HOST=${POSTGRES_HOST:-postgres}
PORT=${POSTGRES_PORT:-5432}

echo "Waiting for PostgreSQL at $HOST:$PORT..."
# wait until /dev/tcp is open
while ! bash -c "</dev/tcp/$HOST/$PORT" >/dev/null 2>&1; do
  printf '.'; sleep 1
done

echo "\nPostgres is available - starting application"
exec java -jar app.jar
