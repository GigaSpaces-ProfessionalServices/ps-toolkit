#!/bin/bash

if [ -z "$JSHOMEDIR" ]; then
    echo "Please set JSHOMEDIR."
    exit 1
fi

if [ -z "$JAVA_HOME" ]; then
    echo "Please set JAVA_HOME."
    exit 1
fi

export GS_HOME=${JSHOMEDIR}
export PATH=${JSHOMEDIR}/bin:${PATH}
export GSA_JAVA_OPTIONS="$GSA_JAVA_OPTIONS -Dprocess.marker=management-agent-marker"

log_file="${JSHOMEDIR}/logs/start-mgt.log"
if [ -e "${log_file}" ]; then
    script_mod_date=`date +%Y-%m-%d~%H.%M.%S -r ${log_file}`
    log_file_zip="${JSHOMEDIR}/logs/${script_mod_date}-start-mgt.zip"

    zip ${log_file_zip} ${log_file}
fi

nohup ${JSHOMEDIR}/bin/gs-agent.sh gsa.gsc 0 gsa.lus 1 gsa.gsm 1 >${log_file} 2>&1 &
echo "Starting gs agent..."
