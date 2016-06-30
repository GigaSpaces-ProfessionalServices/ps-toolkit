#!/bin/bash
set -o nounset
set -o errexit
#set -x

readonly default_grafana_ini_path='/tmp/grafana.ini'
readonly config_dir=/etc/grafana/
readonly remote_user=ubuntu

function show_usage() {
    echo ""
    echo "Deploys Grafana initialization file to the specified destination machine"
    echo ""
    echo "Usage: $0 [--help]"
    echo "   Or: $0 [OPTIONS]... <grafana-hostname-or-ip-address>"
    echo ""
    echo "Options are from the following:"
    echo "  [-s, --source-file <local-grafana-ini-file-path>]"
    echo ""
    echo "A destination host is required."
    echo "Source file location will default to $config_dir if another path is not provided."
    echo "The destination file path is not configurable. Destination machines will:"
    echo ""
    echo "  1. Receive a copy of grafana.ini at ${config_dir}grafana.ini"
    echo "  2. If there was already a grafana.ini file there, a backup will be created."
    echo ""
}

function parse_input() {
    if [[ $# == 0 ]];
    then
        echo "No Grafana hostname or IP address provided" >&2
        show_usage; exit 2
    fi

    if [[ $# > 3 ]]; then
        echo "Invalid arguments encountered for script $0" >&2
        show_usage; exit 2
    fi

    if [[ $# -eq 1 && $1 == '--help' ]]; then
        show_usage; exit 0
    fi

    while [[ $# > 0 ]]; do
        case $1 in
        '-s' | '--source-file')
            local_grafana_ini_path="$2"
            shift 2 ;;
        *)
            if [[ "$1" == "-"* ]]; then
                echo "Unknown option encountered: $1" >&2
                show_usage; exit 2
            fi

            # required parameter
            destination_address="$1"
            shift ;;
        esac
    done

    if [[ ! -f ${local_grafana_ini_path} ]]; then
        echo "The source file is not available: ${local_grafana_ini_path}" >&2
        exit 1
    fi
}

function backup_existing_grafana_config() {
    remote_host="$1"
    echo ssh -t ${remote_user}@${remote_host} sudo cp -f ${config_dir}grafana.ini ${config_dir}grafana.$( date "+%s" ).ini
    ssh -t ${remote_user}@${remote_host} sudo cp -f ${config_dir}grafana.ini ${config_dir}grafana.$( date "+%s" ).ini
}

function copy_file_to_destination() {
    remote_host="$1"
    echo scp ${local_grafana_ini_path} ${remote_user}@${remote_host}:/tmp/grafana.ini
    scp ${local_grafana_ini_path} ${remote_user}@${remote_host}:/tmp/grafana.ini
    echo ssh -t ${remote_user}@${remote_host} sudo cp /tmp/grafana.ini ${config_dir}grafana.ini
    ssh -t ${remote_user}@${remote_host} sudo cp /tmp/grafana.ini ${config_dir}grafana.ini
}

function main() {
    local_grafana_ini_path=${default_grafana_ini_path}

    parse_input $*
    backup_existing_grafana_config ${destination_address}
    copy_file_to_destination ${destination_address}
}

main $*
