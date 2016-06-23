#!/bin/bash
set -o errexit

show_usage() {
    echo "Usage $0 <host1> <count-of-GSCs> [<host2> <count-of-GSCs>]..."
}

if [[ "$#" -eq 0 ]] || [[ `expr $# % 2` -ne 0 ]]; then
    echo "Wrong number of parameters provided to $0" >&2;
    show_usage; exit 1
else
    readonly input_arr=( "$@" )

    host_addr=
    count=

    for i in "${!input_arr[@]}"
    do
        if [[ `expr $i % 2` -eq 0 ]]; then
            host_addr=${input_arr[$i]}
            if [[ -z "$host_addr" ]]; then
                echo "Cannot parse target host address parameter" >&2;
                show_usage; exit 1
            fi
            continue;
        else
            count=${input_arr[$i]}
            if [[ -z "$count" ]]; then
                echo "Cannot parse the number of GSCs to be started" >&2;
                show_usage; exit 1
            fi
            ssh ${host_addr} /bin/bash -l ${JSHOMEDIR}/scripts/start-gscs.sh ${count}
        fi
    done
fi
