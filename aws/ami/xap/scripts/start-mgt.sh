#!/bin/bash
set -o errexit

if [[ -z "$JSHOMEDIR" ]]; then
    echo "Please set JSHOMEDIR."
    exit 1
fi

if [[ -z "$JAVA_HOME" ]]; then
    echo "Please set JAVA_HOME."
    exit 1
fi

export GS_HOME=${JSHOMEDIR}
export PATH=${JSHOMEDIR}/bin:${PATH}
export GSA_JAVA_OPTIONS="$GSA_JAVA_OPTIONS -Dprocess.marker=management-agent-marker"

readonly log_dir="${JSHOMEDIR}/logs"
mkdir -p ${log_dir}

readonly log_file="${log_dir}/start-mgt.log"
if [[ -e "${log_file}" ]]; then
    readonly script_mod_date=$(date +%Y-%m-%d~%H.%M.%S -r ${log_file})
    readonly log_file_zip="${log_dir}/${script_mod_date}-start-mgt.zip"

    zip ${log_file_zip} ${log_file}
fi

nohup ${JSHOMEDIR}/bin/gs-agent.sh gsa.global.lus 0 gsa.lus 1 gsa.global.gsm 0 gsa.gsm 1 gsa.gsc 0 >${log_file} 2>&1 &
echo "Starting gs agent... See ${log_file}"
