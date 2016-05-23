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
export GSA_JAVA_OPTIONS="$GSA_JAVA_OPTIONS -Dprocess.marker=computing-agent-marker"

if [ "$1" -ge 1 ] 2>/dev/null; then
    log_file="${JSHOMEDIR}/logs/start-gscs.log"
    if [ -e "${log_file}" ]; then
       script_mod_date=`date +%Y-%m-%d~%H.%M.%S -r ${log_file}`
       log_file_zip="${JSHOMEDIR}/logs/${script_mod_date}-start-gscs.zip"

       zip ${log_file_zip} ${log_file}
    fi 

    nohup ${JSHOMEDIR}/bin/gs-agent.sh gsa.gsc $1 gsa.global.gsm 0 gsa.global.lus 0 >${log_file} 2>&1 &
    echo "Starting $1 GSC(s)..."
else
    echo "Invalid number of GSCs:$1. Usage $0 [count of GSCs]" >&2; exit 1
fi
