#!/usr/bin/bash

source /opt/xap/scripts/project-env-settings.sh

readonly LOGDIR="${BD}/logs"
readonly GCLOGDIR="${BD}/gclogs"
readonly LOG_ARCHIVE="xap-logs.$(date +%Y-%m-%d-%H-%M-%S).tgz"
readonly GCLOG_ARCHIVE="xap-gc-logs.$(date +%Y-%m-%d-%H-%M-%S).tgz"

cd ${LOGDIR}
readonly LOG_CNT="$( find . -name '*.log' | wc -l )"
if [ $LOG_CNT -gt 0 ];
then
  tar cvzf ${LOG_ARCHIVE} *.log
  find . -name "*.log" -exec rm {} \;
fi

cd ${GCLOGDIR}
readonly GCLOG_CNT="$( find . -name '*.log' | wc -l )"
if [ $GCLOG_CNT -gt 0 ];
then
  tar cvzf ${GCLOG_ARCHIVE} *.log
  find . -name "*.log" -exec rm {} \;
fi

sleep 1
