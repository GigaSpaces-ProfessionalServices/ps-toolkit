#!/bin/bash
set -o errexit

readonly jshomedir="/opt/gigaspaces/gigaspaces-xap-premium-10.2.1-ga"
readonly java_home="/opt/java/jdk1.7.0_79"
readonly ip_addr=$(/sbin/ifconfig eth0 2>/dev/null|awk '/inet addr:/ {print $2}'|sed 's/addr://')
readonly nic_addr=$ip_addr
readonly lookuplocators=$ip_addr:4174
lookupgroups=

readonly bash_profile=~/.bash_profile
readonly gs_license=/tmp/gslicense.xml

main() {
   if [[ ${jshomedir} ]] ; then
      grep -q -F "JSHOMEDIR" $bash_profile || echo "export JSHOMEDIR=\"${jshomedir}\"" >>$bash_profile
   fi

   if [[ ${java_home} ]] ; then
      grep -q -F "JAVA_HOME" $bash_profile || echo "export JAVA_HOME=\"${java_home}\" PATH=\"${PATH}:${java_home}/bin\"" >>$bash_profile 
   fi

   if [[ ${nic_addr} ]] ; then
      grep -q -F "NIC_ADDR" $bash_profile || echo "export NIC_ADDR=\"${nic_addr}\"" >>$bash_profile
   fi

   if [[ ${lookupgroups} ]] ; then
      grep -q -F "LOOKUPGROUPS" $bash_profile || echo "export LOOKUPGROUPS=\"${lookupgroups}\"" >>$bash_profile
   fi

   if [[ ${lookuplocators} ]] ; then
      grep -q -F "LOOKUPLOCATORS" $bash_profile || echo "export LOOKUPLOCATORS=\"${lookuplocators}\"" >>$bash_profile
   fi

   source ${bash_profile}

   if [[ -e "${gs_license}" ]]; then
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
   if [[ "$#" -eq 0 ]] ; then
      return 0
   fi
   while [[ -n $1 ]]
   do
      case $1 in
      "-g" | "--group")
          shift
          lookupgroups="$1"
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
