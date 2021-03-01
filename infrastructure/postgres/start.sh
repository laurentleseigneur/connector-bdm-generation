#!/usr/bin/env sh
./stop.sh
docker-compose -p PostgreSQL11 pull
docker-compose -p PostgreSQL11 build
docker-compose -p PostgreSQL11 up -d