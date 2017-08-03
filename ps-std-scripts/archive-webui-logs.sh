#!/usr/bin/bash

source /opt/xap/scripts/ccs-ITEST-settings.sh

readonly LOG_ARCHIVE="webui-logs.$(date +%Y-%m-%d-%H-%M-%S).tar"
readonly WDIR=$(pwd)

cd ${LOG_DIR}
readonly LOG_CNT="$( find . -name "*GSWebUI*" | wc -l )"
if [ $LOG_CNT -gt 0 ];
then
  echo "Archiving ${LOG_CNT} WEB UI log files to ${LOG_ARCHIVE}"
 # find . -name "*GSWebUI*" -print
  find . -name "*GSWebUI*" -exec tar rvf ${LOG_ARCHIVE} {} \;
  find . -name "*GSWebUI*" -exec rm {} \;
fi

cd $WDIR

sleep 1
exit 0
