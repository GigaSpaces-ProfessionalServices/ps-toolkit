#!/bin/bash
set -o nounset
set -o errexit
#set -x

readonly default_metrics_xml_path='/tmp/metrics.xml'

function show_usage() {
    echo ""
    echo "Deploys Grafana metrics file to the specified destination machines"
    echo ""
    echo "Usage: $0 [--help]"
    echo "   Or: $0 [OPTIONS]... <comma-delimited-hostname-list>"
    echo ""
    echo "Options are from the following:"
    echo "  [-s, --source-file <local-metrics-xml-file-path>]"
    echo ""
    echo "A list of destination hosts is required, even if it is a single target machine."
    echo "Destination IP addresses can be used in place of hostnames interchangeably."
    echo "File path will default to $default_metrics_xml_path if another path is not provided."
    echo "The destination file path is not configurable. All destination machines will:"
    echo ""
    echo "  1. Receive a copy of metrics.xml at ${config_dir}metrics.xml"
    echo "  2. If there was already a metrics.xml file there, a backup will be created."
    echo ""
}

function parse_input() {
    if [[ $# == 0 ]]; then
        echo "No destination hostnames or IP addresses provided" >&2
        show_usage; exit 2
    fi

    if [[ $# > 3 ]]; then
        echo "Invalid arguments encountered for script $0" >&2
        show_usage; exit 2
    fi

    if [[ $1 == '--help' ]]; then
        show_usage; exit 0
    fi

    while [[ $# > 0 ]]; do
        case $1 in
        '-s' | '--source-file')
            local_metrics_xml_path="$2"
            shift 2 ;;
        *)
            if [[ "$1" == "-"* ]]; then
                echo "Unknown option encountered: $1" >&2
                show_usage; exit 2
            fi

            # required parameter
            address_list="$1"
            shift ;;
        esac
    done

    if [[ ! -f ${local_metrics_xml_path} ]]; then
        echo "The source file is not available: ${local_metrics_xml_path}" >&2
        exit 1
    fi

    update_addresses;
}

function backup_existing_metrics_config() {
    destination_address=$1
    echo ssh ${remote_user}@${destination_address} cp -f ${config_dir}metrics.xml ${config_dir}metrics.$( date "+%s" ).xml
    ssh ${remote_user}@${destination_address} cp -f ${config_dir}metrics.xml ${config_dir}metrics.$( date "+%s" ).xml
}

function copy_file_to_destination() {
    destination_address=$1
    echo scp ${local_metrics_xml_path} ${remote_user}@${destination_address}:${config_dir}metrics.xml
    scp ${local_metrics_xml_path} ${remote_user}@${destination_address}:${config_dir}metrics.xml
}

function has_comma() {
    commas=$(echo ${address_list} | grep "," | wc -l | tr -d " " )
    echo "$commas"
}

function update_addresses() {
    tail=''
    cnt=0
    if [[ $( has_comma ) == 0 ]];
    then
        addresses[0]=${address_list}
    else
        while [[ $( has_comma ) == 1 ]];
        do
            len=${#address_list}
            tail=$(echo $address_list | sed 's#^\([^,]*\),\([^a-zA-Z0-9]*\)#\2#')
            head_len=$((${len}-${#tail}-1))
            head=${address_list:0:${head_len}}

            if [[ -z "$head" ]]; then
                echo "Empty hostname is encountered at index $cnt" >&2
                exit 1
            fi

            addresses[$cnt]=${head}
            address_list=${tail}
            cnt=$((${cnt}+1))
        done

        if [[ -z "$tail" ]]; then
            echo "Empty hostname is encountered at index $cnt" >&2
            exit 1
        fi

        addresses[$cnt]=${tail}
    fi
}

function main() {
    local_metrics_xml_path=${default_metrics_xml_path}
    destination_xap_installation=/opt/gigaspaces/current/
    config_dir=${destination_xap_installation}config/metrics/
    remote_user=ubuntu
    addresses=()

    parse_input $*
    len=${#addresses[*]}
    count=0

    while [[ ${len} > ${count} ]];
    do
        local destination=${addresses[$count]}
        backup_existing_metrics_config ${destination}
        copy_file_to_destination ${destination}
        count=$((${count}+1))
    done
}

main $*
