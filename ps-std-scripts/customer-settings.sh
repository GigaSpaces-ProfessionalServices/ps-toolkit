#!/usr/bin/bash

export JAVA_HOME="/usr/java/latest"

export XAP_HOME="/opt/xap/current"
export BASE_DIR="${XAP_HOME}/.."

export XAP_NIC_ADDRESS="${hostname}"
export UNICAST_PORT="9001"

export XAP_LOOKUP_GROUPS="testgroup"
export XAP_LOOKUP_LOCATORS="host1:${UNICAST_PORT},host2:${UNICAST_PORT}" 

export COMMON_XAP_OPTIONS="-XX:+UseCompressedOops -XX:+ExplicitGCInvokesConcurrent -Dsun.rmi.dgc.client.gcInterval=36000000 -Dsun.rmi.dgc.server.gcInterval=36000000 -Djava.rmi.server.hostname=${XAP_NIC_ADDRESS} -Dcom.gs.multicast.enabled=false -Dcom.gs.multicast.discoveryPort=${UNICAST_PORT} -Dcom.gigaspaces.start.httpPort=9003 -Dcom.gigaspaces.system.registryPort=9004 -Dcom.gs.transport_protocol.lrmi.bind-port="9000-10000" -Dcom.sun.jini.reggie.initialUnicastDiscoveryPort=${UNICAST_PORT} -Dcom.gs.work=${BASE_DIR}/work -Dcom.gs.deploy=${BASE_DIR}/deploy -Dcom.gs.security.fs.file-service.file-path=${BASE_DIR}/security/gs-directory.fsm "

export XAP_GSM_OPTIONS="${COMMON_XAP_OPTIONS} -Xmx1g -Xms1g "
export XAP_LUS_OPTIONS="${COMMON_XAP_OPTIONS} -Xmx1g -Xms1g "
export XAP_GSA_OPTIONS="${COMMON_XAP_OPTIONS} -Xmx1g -Xms1g "
export XAP_GSC_OPTIONS="${COMMON_XAP_OPTIONS} -Xmx11g -Xms11g "
