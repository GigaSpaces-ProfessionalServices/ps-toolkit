#!/bin/bash
set -o errexit

show_usage() {
    echo ""
    echo "Starts XAP data containers (GSCs) on remote machine(s)"
    echo ""
    echo "Usage: $0 [--help] <host1> <number1-of-GSCs> [<host2> <number2-of-GSCs>]..."
    echo ""
}

parse_input() {
    if [[ $# -eq 0 ]]; then
        echo "The hosts and numbers of remote GSCs are not specified" >&2
        show_usage; exit 2
    fi

    if [[ $# -eq 1 && $1 == '--help' ]]; then
        show_usage; exit 0
    fi

    if [[ $(expr $# % 2) -ne 0 ]]; then
        echo "The script $0 expects even number of parameters" >&2;
        show_usage; exit 2
    fi
}

start_remote_gscs() {
    readonly input_arr=( "$@" )

    for i in "${!input_arr[@]}"
    do
        if [[ $(expr $i % 2) -eq 0 ]]; then
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
            ssh ${host_addr} /bin/bash -l ${JSHOMEDIR}/scripts/xap_service_mgt/start-gscs.sh ${count}
        fi
    done
}

main() {
    parse_input "$@"
    start_remote_gscs "$@"
}

main "$@"
