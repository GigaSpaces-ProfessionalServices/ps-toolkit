#!/bin/bash
set -o errexit

if [[ -z "$JSHOMEDIR" ]]; then
    echo "Please set JSHOMEDIR."; exit 1
fi

export WEBUI_JAVA_OPTIONS="$WEBUI_JAVA_OPTIONS -Dprocess.marker=webui-marker"

readonly log_dir="${JSHOMEDIR}/logs"
mkdir -p ${log_dir}

readonly log_file="${log_dir}/start-webui.log"

nohup ${JSHOMEDIR}/bin/gs-webui.sh >${log_file} 2>&1 &
echo "Starting XAP web management console... See ${log_file}"
