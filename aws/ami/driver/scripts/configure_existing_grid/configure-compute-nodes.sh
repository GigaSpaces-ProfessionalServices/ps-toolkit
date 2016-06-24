#!/bin/bash
set -o errexit

if [ "$#" -eq 0 ]; then
    echo "No connection details were provided. Usage $0 [HOST1] [HOST2] ..." >&2; exit 1
else
    readonly ip_addr=( "$@" ) 
    for host_dest in "${ip_addr[@]}"
    do
        ssh ${host_dest} ${JSHOMEDIR}/scripts/configure.sh --groups ${LOOKUPGROUPS}
    done
fi
