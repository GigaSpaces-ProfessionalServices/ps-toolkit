#!/bin/bash

source /opt/xap/scripts/project-env-settings.sh

readonly LOG_ARCHIVE="webui-logs.$(date +%Y-%m-%d-%H-%M-%S).tar"
readonly WDIR=$(pwd)

cd ${LOG_DIR}
readonly LOG_CNT="$( find . -name "*GSWebUI*" -or -name "gs-webui*" | wc -l )"
if [ $LOG_CNT -gt 0 ];
then
  echo "Archiving ${LOG_CNT} WEB UI log files to ${LOG_ARCHIVE}"
  #find . -name "*GSWebUI*" -or -name "gs-webui*" -print
  find . -name "*GSWebUI*" -or -name "gs-webui*" -exec tar rvf ${LOG_ARCHIVE} {} \;
  find . -name "*GSWebUI*" -or -name "gs-webui*" -exec rm {} \;
fi

cd $WDIR

sleep 1
