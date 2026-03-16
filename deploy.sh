#!/bin/bash

set -e

# Generated using Claude Code

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log()     { echo -e "${GREEN}[bookshare]${NC} $1"; }
warn()    { echo -e "${YELLOW}[bookshare]${NC} $1"; }
error()   { echo -e "${RED}[bookshare]${NC} $1"; exit 1; }

# Checks
check_dependencies() {
  command -v docker &>/dev/null        || error "Docker is not installed."
  docker compose version &>/dev/null   || error "Docker Compose is not installed."
}

check_env_file() {
  if [ ! -f .env ]; then
    warn ".env file not found. Copying from .env.example..."
    [ -f .env.example ] || error ".env.example not found either. Cannot proceed."
    cp .env.example .env
    warn "Please update .env with your actual credentials before continuing."
    exit 1
  fi
}

# Commands
start() {
  log "Starting services..."
  docker compose up --build -d
  log "App is running at http://localhost:6001"
}

stop() {
  log "Stopping services..."
  docker compose down
}

# Help
usage() {
  echo ""
  echo "Usage: ./run.sh [command]"
  echo ""
  echo "Commands:"
  echo "  start     Build and start all services (default)"
  echo "  stop      Stop all services"
  echo ""
}

# Entry Point
check_dependencies
check_env_file

case "${1}" in
  start)  start ;;
  stop)   stop ;;
  *)      usage ;;
esac
