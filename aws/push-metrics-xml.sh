#!/bin/bash
set -o nounset
set -o errexit
#set -x

readonly default_metrics_xml_path='/tmp/metrics.xml'

local_metrics_xml_path=${default_metrics_xml_path}
destination_xap_installation=/opt/gigaspaces/current/
config_dir=${destination_xap_installation}config/metrics/
key_path=~/.ssh/fe-shared.pem
remote_user=ubuntu
destination_address=

function show_usage() {
    echo ""
    echo "   Usage: $0 [metrics.xml file path] <comma-delimited list of destination ips & hostnames>"
    echo ""
    echo "   A list of destination hosts is required, even if it's single destination."
    echo "   File path will default to $default_metrics_xml_path if another path is not provided."
    echo "   The destination file path is not configurable. All destination machines will:"
    echo ""
    echo "     1. Receive a copy of metrics.xml at ${config_dir}metrics.xml"
    echo "     2. If there was already a metrics.xml file there, a backup will be created."
    echo ""
    exit 3
}

function backup_existing_metrics_config() {
    destination_address=$1
    echo ssh -i ${key_path} ${remote_user}@${destination_address} cp -f ${config_dir}metrics.xml ${config_dir}metrics.$( date "+%s" ).xml
}

function copy_file_to_destination() {
    destination_address=$1
    echo scp -i ${key_path} ${local_metrics_xml_path} ${remote_user}@${destination_address}:${config_dir}metrics.xml
}

address_list=''
addresses=()

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
            addresses[$cnt]=${head}
            address_list=${tail}
            cnt=$((${cnt}+1))
        done
        addresses[$cnt]=${tail}
    fi
}

function parse_input() {
    if [[ $# == 0 ]];
    then
        show_usage;
    fi
    if [[ $# > 2 ]];
    then
        show_usage;
    fi
    if [[ $# == 1 ]];
    then
        if [[ $1 == "--help" ]];
        then
            show_usage;
        else
            address_list=$1
        fi
    fi
    if [[ $# == 2 ]];
    then
        address_list=$2
        local_metrics_xml_file_path=$1
        test ! -f ${local_metrics_xml_file_path} || echo "Bad file path: ${local_metrics_xml_file_path} ."; show_usage;
    fi
    update_addresses;
}

function main() {
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