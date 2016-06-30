#!/bin/bash
set -o errexit

show_usage() {
    echo ""
    echo "Stops XAP management (GSM) and network discovery (LUS) components"
    echo ""
    echo "Usage: $0 [--help]"
    echo ""
}

parse_input() {
    if [[ $# -eq 1 && $1 == '--help' ]]; then
        show_usage; exit 0
    fi

    if [[ $# -gt 0 ]]; then
        echo "Invalid arguments encountered for script $0" >&2
        show_usage; exit 2
    fi
}

stop_mgt() {
    readonly pid=$(ps aux | grep -v grep | grep process.marker=management-agent-marker | awk '{print $2}')
    if [[ -z $pid ]] ; then
        echo "XAP management components are not running"
        exit
    fi
    echo "Stopping GS Agent (pid: $pid)..."
    kill -SIGTERM $pid

    TIMEOUT=60
    while ps -p $pid > /dev/null; do
        if [[ $TIMEOUT -le 0 ]] ; then
            echo "GS Agent has not been stopped within $TIMEOUT seconds" >&2
            exit 1
        fi
        let "TIMEOUT--"
        sleep 1
    done

    echo "GS Agent stopped"
}

main() {
    parse_input "$@"
    stop_mgt
}

main "$@"
