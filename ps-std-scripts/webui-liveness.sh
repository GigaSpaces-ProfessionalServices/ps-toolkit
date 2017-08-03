#!/bin/bash 

readonly STATUS_LOG="/tmp/webui-status.$(date +%Y-%m-%d-%H-%M-%S).log"

while [ 1 ]
do

UPORDOWN=$(netstat -ano | grep 8099)
NOW=$(date)

  echo "${NOW} ::: ${UPORDOWN}" >> ${STATUS_LOG}
  sleep 5

done
