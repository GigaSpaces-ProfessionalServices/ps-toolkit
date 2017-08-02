#!/usr/bin/bash

source /opt/xap/scripts/project-env-settings.sh

LOG_DIR="${BASE_DIR}/logs"
export USER_NAME_MANDATORY=true

   nohup ${XAP_HOME}/bin/gs-webui.sh &> ${LOG_DIR}/gs-webui-console-log.$(date +%Y-%m-%d-%H-%M-%S).log &
   nohup ./webui-liveness.sh &

sleep 1
