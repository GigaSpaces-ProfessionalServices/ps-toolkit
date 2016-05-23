#!/bin/bash

if [ "$#" -eq 0 ]; then
    echo "No connection details were provided. Usage $0 [user1@IP_ADDRESS1] [user2@IP_ADDRESS2] ..." >&2; exit 1
else
    ip_addr=( "$@" ) 
    for host_dest in "${ip_addr[@]}"
    do      
        ssh ${host_dest} ${JSHOMEDIR}/scripts/configure.sh ${LOOKUPGROUPS}
    done
fi
