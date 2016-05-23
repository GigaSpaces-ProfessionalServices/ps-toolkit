#!/bin/bash

usage() {
    echo "Usage $0 [user1@IP_ADDRESS1] [GSCs count] [user2@IP_ADDRESS2] [GSCs count]..." >&2;
}

if [ \( "$#" -eq 0 \) -o \( `expr $# % 2` -ne 0 \) ]; then
    usage; exit 1
else
    input_arr=( "$@" )
    host_addr=

    for i in "${!input_arr[@]}"
    do
	if [ `expr $i % 2` -eq 0 ]; then
	   host_addr=${input_arr[$i]}
           if [ -z "$host_addr" ]; then
              usage; exit 1
           fi
           continue;
        else
           count=${input_arr[$i]}
           if [ -z "$count" ]; then
	      usage; exit 1  
           fi    
           ssh ${host_addr} /bin/bash -l ${JSHOMEDIR}/scripts/start-gscs.sh ${count}
	fi
    done  
fi
