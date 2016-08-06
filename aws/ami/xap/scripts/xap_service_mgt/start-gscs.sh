#!/bin/bash
set -o errexit

show_usage() {
    echo ""
    echo "Starts XAP data containers (GSCs) on current machine"
    echo ""
    echo "Usage: $0 [--help] <number-of-GSCs>"
    echo ""
}

parse_input() {
    if [[ $# -eq 0 ]]; then
        echo "The number of GSCs to be started is not specified" >&2
        show_usage; exit 2
    fi

    if [[ $# -eq 1 && $1 == '--help' ]]; then
        show_usage; exit 0
    fi

    if [[ -z "$JSHOMEDIR" ]]; then
        echo "Please set environment variable JSHOMEDIR"
        exit 1
    fi

    if [[ -z "$JAVA_HOME" ]]; then
        echo "Please set environment variable JAVA_HOME"
        exit 1
    fi

    if [[ $# -gt 1 ]]; then
        echo "Invalid arguments encountered for script $0" >&2
        show_usage; exit 2
    fi

    if [[ "$1" -lt 1 ]] 2>/dev/null; then
        echo "The number of GSCs to be started is invalid: $1" >&2
        show_usage; exit 2
    fi
}

start_gscs() {
    export GS_HOME=${JSHOMEDIR}
    export PATH=${JSHOMEDIR}/bin:${PATH}
    export GSA_JAVA_OPTIONS="$GSA_JAVA_OPTIONS -Dprocess.marker=computing-agent-marker"

    readonly log_dir="${JSHOMEDIR}/logs"
    mkdir -p ${log_dir}

    readonly log_file="${log_dir}/start-gscs.log"

    if [[ -e "${log_file}" ]]; then
        readonly script_mod_date=$(date +%Y-%m-%d~%H.%M.%S -r ${log_file})
        readonly log_file_zip="${log_dir}/${script_mod_date}-start-gscs.zip"

        zip ${log_file_zip} ${log_file}
    fi

    nohup ${JSHOMEDIR}/bin/gs-agent.sh gsa.global.lus 0 gsa.lus 0 \
        gsa.global.gsm 0 gsa.gsm 0 gsa.gsc $1 >${log_file} 2>&1 &
    echo "Starting $1 GSC(s)... See ${log_file}"
}

main() {
    . $(dirname $0)/setenv.sh
    parse_input "$@"
    start_gscs "$1"
}

main "$@"
