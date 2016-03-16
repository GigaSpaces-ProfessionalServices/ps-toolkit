#!/usr/bin/env bash

GRID_INSTALL="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

source $GRID_INSTALL/setAppEnv.sh


export GS_LOGGING_CONFIG_FILE="$INSTALL_CONFIG_DIR/gs_cli_logging.properties"
export JAVA_OPTIONS="${XAP_LOGGING_OPTIONS}"

$GS_HOME/bin/gs.sh $*