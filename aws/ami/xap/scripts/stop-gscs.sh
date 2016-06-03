#!/bin/bash

readonly pid=$(ps aux | grep -v grep | grep process.marker=computing-agent-marker | awk '{print $2}')
if [[ -z $pid ]]; then
    echo "Computing nodes are not running"
    exit
fi
echo "Stopping gs agent (pid: $pid)..."
kill -SIGTERM $pid

TIMEOUT=60
while ps -p $pid > /dev/null; do
    if [[ $TIMEOUT -le 0 ]]; then
        echo "GS Agent has not been stopped within $TIMEOUT seconds"
	exit 1
    fi
    let "TIMEOUT--"
    sleep 1
done
echo "GS Agent stopped"

