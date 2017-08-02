#!/usr/bin/bash 

source ./project-env-settings.sh

LOG_DIR="${BASE_DIR}/logs"

nohup ${XAP_HOME}/bin/gs-agent.sh gsa.gsc 2 gsa.gsm 1 gsa.lus 1 &> ${LOG_DIR}/gs-agent-console-log.$(date +%Y-%m-%d-%H-%M-%S).log &
sleep 1
[gsadmin@htappd00790 scripts]$ cat setenv-overrides.sh 
