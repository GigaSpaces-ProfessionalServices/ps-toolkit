#!/bin/bash
set -o errexit

if [[ -z "$JSHOMEDIR" ]]; then
    echo "Please set JSHOMEDIR."
    exit 1
fi

export WEBUI_JAVA_OPTIONS="$WEBUI_JAVA_OPTIONS -Dprocess.marker=webui-marker"
nohup ${JSHOMEDIR}/bin/gs-webui.sh >/dev/null 2>&1 &
echo "Starting XAP web management console..."
