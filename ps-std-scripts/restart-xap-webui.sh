#!/bin/bash

source /opt/xap/scripts/project-env-settings.sh

readonly TCP_TIMEOUT=63
readonly BASHPID=$$

WEBUIPID=$(ps ax | grep "GS Web UI" | head -1 | awk '{ print $1 }')

if [ "${BASHPID}" -ne "${WEBUIPID}" ];
then
  echo "Killing WEBUI with PID ${WEBUIPID}."
  kill -9 $WEBUIPID
  echo "Success: $?"
fi


echo "Waiting for RHEL to release the 8099 TCP port."
sleep $TCP_TIMEOUT

echo "Starting XAP webui."
bash /opt/ccs/xap/scripts/start-xap-webui.sh
