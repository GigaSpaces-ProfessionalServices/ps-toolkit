#!/bin/bash
set -o errexit

usage() {
    echo "Usage $0 [HOST1] [GSCs count] [HOST2] [GSCs count]..." >&2;
}

if [[ "$#" -eq 0 ]] || [[ `expr $# % 2` -ne 0 ]]; then
    usage; exit 1
else
    readonly input_arr=( "$@" )
    host_addr=
    count=     

    for i in "${!input_arr[@]}"
    do
	if [[ `expr $i % 2` -eq 0 ]]; then
	   host_addr=${input_arr[$i]}
           if [[ -z "$host_addr" ]]; then
              usage; exit 1
           fi
           continue;
        else
           count=${input_arr[$i]}
           if [[ -z "$count" ]]; then
	      usage; exit 1  
           fi    
           ssh ${host_addr} /bin/bash -l ${JSHOMEDIR}/scripts/start-gscs.sh ${count}
	fi
    done  
fi
