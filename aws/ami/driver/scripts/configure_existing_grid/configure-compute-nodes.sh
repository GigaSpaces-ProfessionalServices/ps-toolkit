#!/bin/bash
set -o errexit

show_usage() {
    echo ""
    echo "Configures XAP on remote grid machines specified on command line"
    echo ""
    echo "Usage: $0 [--help] <ip-address1> [<ip-address2>]..."
    echo ""
}

parse_input() {
    if [[ $# -eq 0 ]]; then
        echo "No connection details were provided" >&2
        show_usage; exit 2
    fi

    if [[ $# -eq 1 && $1 == '--help' ]]; then
        show_usage; exit 0
    fi
}

main() {
    parse_input "$@"

    readonly ip_addr=( "$@" )
    for host_dest in "${ip_addr[@]}"
    do
        ssh ${host_dest} ${JSHOMEDIR}/scripts/update_local_configuration/local-configure.sh --lookup-groups ${LOOKUPGROUPS}
    done
}

main "$@"
