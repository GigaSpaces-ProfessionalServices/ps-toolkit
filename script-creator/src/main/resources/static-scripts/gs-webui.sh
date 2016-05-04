#!/bin/bash

GRID_INSTALL="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

source $GRID_INSTALL/setAppEnv.sh

export WEBUI_OUT="$INSTALL_LOGS_DIR/$(date +"%m%d%Y~%H.%M.%S-xap-webui.log")"

echo "Creating log file: $WEBUI_OUT..."
nohup $GS_HOME/bin/gs-webui.sh > $WEBUI_OUT 2>&1 &