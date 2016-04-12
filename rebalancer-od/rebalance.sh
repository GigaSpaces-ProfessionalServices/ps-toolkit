#!/bin/bash

# Current Directory
SCRIPT_INSTALL="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# export machine-options.sh
source $SCRIPT_INSTALL/setAppEnv.sh
echo "SCRIPT_INSTALL:$SCRIPT_INSTALL"

export JAVA_OPTIONS="${XAP_LOGGING_OPTIONS}"
echo "JAVA_OPTIONS:$JAVA_OPTIONS"

export APP_CP="$GS_HOME/lib/required/*:$SCRIPT_INSTALL/rebalancer/lib/*:$SCRIPT_INSTALL/rebalancer/rebalancer-od-1.0-SNAPSHOT.jar"
echo "APP_CP:$APP_CP"

java -cp "$APP_CP" com.gigaspaces.gigapro.rebalancer.Program -g group -p puName -m 5