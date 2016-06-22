#!/bin/bash
set -o nounset
set -o errexit
#set -x

readonly default_grafana_ini_path='/tmp/grafana.ini'
readonly config_dir=/etc/grafana/
readonly key_path=~/.ssh/fe-shared.pem
readonly remote_user=ubuntu

local_grafana_ini_path=${default_grafana_ini_path}
destination_address=

function show_usage() {
    echo ""
    echo "   Usage: $0 [local grafana.ini file path] <hostname or ip of grafana installation>"
    echo ""
    echo "          $0 [--help] # to print this usage output"
    echo ""
    echo "   A destination host is required."
    echo "   File path will default to $config_dir if another path is not provided."
    echo "   The destination file path is not configurable. Destination machines will:"
    echo ""
    echo "     1. Receive a copy of grafana.ini at ${config_dir}grafana.ini"
    echo "     2. If there was already a grafana.ini file there, a backup will be created."
    echo ""
    exit 3
}

function backup_existing_grafana_config() {
    remote_host="$1"
    echo ssh -t -i ${key_path} ${remote_user}@${remote_host} sudo cp -f ${config_dir}grafana.ini ${config_dir}grafana.$( date "+%s" ).ini
    ssh -t -i ${key_path} ${remote_user}@${remote_host} sudo cp -f ${config_dir}grafana.ini ${config_dir}grafana.$( date "+%s" ).ini
}

function copy_file_to_destination() {
    remote_host="$1"
    echo scp -i ${key_path} ${local_grafana_ini_path} ${remote_user}@${remote_host}:/tmp/grafana.ini
    scp -i ${key_path} ${local_grafana_ini_path} ${remote_user}@${remote_host}:/tmp/grafana.ini
    echo ssh -t -i ${key_path} ${remote_user}@${remote_host} sudo cp /tmp/grafana.ini ${config_dir}grafana.ini
    ssh -t -i ${key_path} ${remote_user}@${remote_host} sudo cp /tmp/grafana.ini ${config_dir}grafana.ini
}

function parse_input() {
    if [[ $# == 0 ]];
    then
        show_usage;
        exit 3
    fi
    if [[ $# > 3 ]];
    then
        show_usage;
        exit 3
    fi
    if [[ $# == 1 ]];
    then
        if [[ $1 == "--help" ]];
        then
            show_usage;
            exit 3
	else
	    destination_address=$1
            test ! -f ${local_grafana_ini_path} &&  ( echo "Bad file path: ${local_grafana_ini_path} ."; show_usage; exit 3 )
        fi
    fi
    if [[ $# == 2 ]];
    then
	if [[ "$1" == "-g" ]];
	then
            destination_address=$2
            test ! -f ${local_grafana_ini_path} &&  ( echo "Bad file path: ${local_grafana_ini_path} ."; show_usage; exit 3 )
        fi
    fi
    if [[ $# == 3 ]];
    then
	if [[ "$2" == "-g" ]];
	then
	    destination_address=$3
	    local_grafana_ini_path=$1
            test ! -f ${local_grafana_ini_path} &&  ( echo "Bad file path: ${local_grafana_ini_path} ."; show_usage; exit 3 )
	fi
    fi
}

function main() {
    parse_input $*
    backup_existing_grafana_config ${destination_address}
    copy_file_to_destination ${destination_address}
}

main $*
