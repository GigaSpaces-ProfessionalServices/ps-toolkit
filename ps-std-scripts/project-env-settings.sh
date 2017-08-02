#!/usr/bin/bash

export JAVA_HOME="/usr/java/latest"

export XAP_HOME="/opt/xap/current"
export BASE_DIR="${XAP_HOME}/.."

export XAP_NIC_ADDRESS="$(hostname)"
export UNICAST_PORT="7101"
export XAP_LOOKUP_GROUPS="xapgrid-test"
export XAP_LOOKUP_LOCATORS="somehost.com:${UNICAST_PORT},someotherhost.com:${UNICAST_PORT}"
export LRMI="-Djava.rmi.server.hostname=${XAP_NIC_ADDRESS} -Dcom.gs.multicast.enabled=false -Dcom.gs.multicast.discoveryPort=${UNICAST_PORT} -Dcom.gigaspaces.start.httpPort=7003 -Dcom.gigaspaces.system.registryPort=7004 -Dcom.gs.transport_protocol.lrmi.bind-port="7110-10000" -Dcom.sun.jini.reggie.initialUnicastDiscoveryPort=${UNICAST_PORT}"

export MGT_HEAP="-Xmx1g -Xms1g"
export WEBUI_HEAP="-Xms2g -Xmx2g"
export GSC_HEAP="-Xmx11g -Xms11g -XX:+UseCompressedOops -XX:+HeapDumpOnOutOfMemoryError"

export GC="-XX:+UseG1GC -XX:+PrintGC -XX:+ExplicitGCInvokesConcurrent -Xloggc:${BASE_DIR}/gclogs/gc_%p.log"
# GC_DEBUG provides verbose logging to gc log files and heap dumps for "should never happen" situations. Should only be necessary for GSCs.
export GC_DEBUG="-XX:+PrintGC -XX:+PrintGCCause -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCApplicationConcurrentTime -XX:+PrintAdaptiveSizePolicy -XX:+PrintTenuringDistribution -XX:-PrintReferenceGC -XX:+PrintClassHistogramBeforeFullGC -XX:+PrintClassHistogramAfterFullGC -XX:+HeapDumpBeforeFullGC -XX:+HeapDumpAfterFullGC -XX:+PrintParallelOldGCPhaseTimes"
export SECURITY="-Dcom.gs.security.enabled=true -Dcom.gs.security.fs.file-service.file-path=${BASE_DIR}/security/gs-directory.fsm -Dcom.gigaspaces.logger.RollingFileHandler.filename-pattern.homedir=${BASE_DIR}"
export AGENT="-agentpath:/opt/DT/libdtagent.so=name=PROJ_AppSvr,server=someserver.com:9998,logpath=/opt/DT/log" # Dynatrace syntax YMMV

export COMMON_XAP_OPTIONS="-XX:+UnlockExperimentalVMOptions -XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal -Dcom.gs.deploy=${BASE_DIR}/deploy ${LRMI} ${GC} ${SECURITY}"
# WORK is pulled out of the WEB UI JVM options b/c/o the way that the start-xap-webui.sh script is implemented
export WORK="-Dcom.gs.work=${BASE_DIR}/work"
export COMMON_XAP_OPTIONS_WORK="${COMMON_XAP_OPTIONS} ${WORK}"

export XAP_GSM_OPTIONS="${MGT_HEAP} ${COMMON_XAP_OPTIONS_WORK}"
export XAP_LUS_OPTIONS="${MGT_HEAP} ${COMMON_XAP_OPTIONS_WORK}"
export XAP_GSA_OPTIONS="${MGT_HEAP} ${COMMON_XAP_OPTIONS_WORK}"
export XAP_GSC_OPTIONS="${GSC_HEAP} ${COMMON_XAP_OPTIONS_WORK} ${GC_DEBUG}" # AGENT INFO SHOULD BE ADDED HERE
export XAP_WEBUI_OPTIONS="${WEBUI_HEAP} ${COMMON_XAP_OPTIONS} ${SECURITY} -Dcom.gs.transport_protocol.lrmi.request_timeout=30000"                                                                                                                                      
