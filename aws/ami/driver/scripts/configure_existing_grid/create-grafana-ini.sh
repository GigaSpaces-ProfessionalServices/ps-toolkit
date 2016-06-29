#!/bin/bash
set -o nounset
set -o errexit
#set -x

readonly grafana_config_file=./resources/grafana.ini
readonly grafana_temp_file=/tmp/grafana.ini
readonly grafana_hostname_default='localhost'

function show_usage() {
    echo ""
    echo "Configures Grafana initialization file for the host specified"
    echo ""
    echo "Usage: $0 [--help]"
    echo "   Or: $0 [OPTIONS]..."
    echo ""
    echo "Options are from the following:"
    echo "  [-g, --grafana-host <grafana-hostname-or-ip-address>]";
    echo ""
    echo "If no hostname is provided, Grafana will use localhost in its URLs"
    echo ""
}

function parse_input() {
    if [[ $1 == '--help' ]]; then
        show_usage; exit 0
    fi

    test $# -gt 2 && show_usage && exit 3
    while [[ $# > 0 ]]; do
        key="$1";
        case ${key} in
        '-g' | '--grafana-host')
            use_grafana=1
            grafana_hostname="$2"
            if [[ -z $2 ]];
            then
                error=1
                show_usage;
                exit 3;
            fi
            shift; shift;;
        *)
            echo ""
            echo "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
            echo "X ERROR: Illegal argument to script: $key   X"
            echo "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
            echo ""
            show_usage;
            shift ;;
        esac
    done
}

function update_grafana() {
    if [[ -f ${grafana_temp_file} ]]
    then
        echo ""
        echo "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
        echo "XXX WARNING: ${grafana_temp_file} already exists. XXX"
        echo "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
        echo ""
        sed -i s/"{{domain_name}}"/${grafana_hostname}/ ${grafana_temp_file}
    else
        sed s/"{{domain_name}}"/${grafana_hostname}/ ${grafana_config_file} > ${grafana_temp_file}
    fi
}

main() {
    grafana_hostname=${grafana_hostname_default}
    error=0

    parse_input $*
    update_grafana

    if [[ ${error} -eq 0 ]];
    then
        echo ""
        echo "The grafana config file has been updated and/or written to ${grafana_temp_file}.";
        echo ""
        exit 0
    else
        exit 3
    fi
}

main "$@"
