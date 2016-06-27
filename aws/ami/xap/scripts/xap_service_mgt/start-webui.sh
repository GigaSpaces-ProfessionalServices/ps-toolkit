#!/bin/bash
set -o errexit

show_usage() {
    echo ""
    echo "Starts XAP web management console on current machine"
    echo ""
    echo "Usage: $0 [--help]"
    echo ""
}

parse_input() {
    if [[ $1 == '--help' ]]; then
        show_usage; exit 0
    fi

    if [[ -z "$JSHOMEDIR" ]]; then
        echo "Please set environment variable JSHOMEDIR"; exit 1
    fi

    if [[ $# -gt 0 ]]; then
        echo "Invalid arguments encountered for script $0" >&2
        show_usage; exit 2
    fi
}

start_webui() {
    export WEBUI_JAVA_OPTIONS="$WEBUI_JAVA_OPTIONS -Dprocess.marker=webui-marker"

    readonly log_dir="${JSHOMEDIR}/logs"
    mkdir -p ${log_dir}

    readonly log_file="${log_dir}/start-webui.log"

    nohup ${JSHOMEDIR}/bin/gs-webui.sh >${log_file} 2>&1 &
    echo "Starting XAP web management console... See ${log_file}"
}

main() {
    parse_input "$@"
    start_webui
}

main "$@"
