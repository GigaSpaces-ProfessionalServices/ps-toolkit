#!/bin/bash

JSHOMEDIR=/opt/gigaspaces/gigaspaces-xap-premium-10.2.1-ga
JAVA_HOME=/opt/java/jdk1.7.0_79
IP_ADDR=`/sbin/ifconfig eth0 2>/dev/null|awk '/inet addr:/ {print $2}'|sed 's/addr://'`
NIC_ADDR=$IP_ADDR
LOOKUPLOCATORS=$IP_ADDR:4174

bash_profile=~/.bash_profile
gs_license=/tmp/gslicense.xml

main() {
   if [ ${JSHOMEDIR} ] ; then
      grep -q -F "JSHOMEDIR" $bash_profile || echo "export JSHOMEDIR=\"${JSHOMEDIR}\"" >>$bash_profile
   fi

   if [ ${JAVA_HOME} ] ; then
      grep -q -F "JAVA_HOME" $bash_profile || echo "export JAVA_HOME=\"${JAVA_HOME}\" PATH=\"${PATH}:${JAVA_HOME}/bin\"" >>$bash_profile 
   fi

   if [ ${NIC_ADDR} ] ; then
      grep -q -F "NIC_ADDR" $bash_profile || echo "export NIC_ADDR=\"${NIC_ADDR}\"" >>$bash_profile
   fi

   if [ ${LOOKUPGROUPS} ] ; then
      grep -q -F "LOOKUPGROUPS" $bash_profile || echo "export LOOKUPGROUPS=\"${LOOKUPGROUPS}\"" >>$bash_profile
   fi

   if [ ${LOOKUPLOCATORS} ] ; then
      grep -q -F "LOOKUPLOCATORS" $bash_profile || echo "export LOOKUPLOCATORS=\"${LOOKUPLOCATORS}\"" >>$bash_profile
   fi

   source ${bash_profile}

   if [ -e "${gs_license}" ]; then
      cp -rf ${gs_license} ${JSHOMEDIR}
   else 
      echo "License ${gs_license} does not exist. No license was installed."
   fi
}

usage() {
   echo "Usage $0 [optional parameters]"
   echo "          -g, --group | sets lookup groups"
   echo "              --help  | displays usage"
   exit 1
}

parse_input() {
   while [ "$1" != "" ]
   do
      case $1 in
      "-g" | "--group")
          shift
          LOOKUPGROUPS=$1
          ;;
      "--help")
	  shift
          usage
          ;;
     esac 
     shift
   done
}

parse_input "$@"
main
