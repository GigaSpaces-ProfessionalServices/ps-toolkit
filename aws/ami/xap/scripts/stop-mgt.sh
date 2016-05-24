#!/bin/bash

pid=`ps aux | grep -v grep | grep process.marker=management-agent-marker | awk '{print $2}'`
if [ -z $pid ]; then
    echo "Management nodes are not running"
    exit
fi
echo "Stopping gs agent (pid: $pid)..."
kill -SIGTERM $pid

TIMEOUT=60
while ps -p $pid > /dev/null; do
    if [ $TIMEOUT -le 0 ]; then
        break
    fi
    let "TIMEOUT--"
    sleep 1
done
echo "GS Agent stopped"


