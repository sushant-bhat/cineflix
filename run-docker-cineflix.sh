#!/bin/zsh

# Run docker-compose in the foreground
echo "Starting Docker Compose..."
docker compose up --build
echo "After docker compose"
exec docker compose down
