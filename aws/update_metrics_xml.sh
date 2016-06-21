#!/bin/bash
set -o nounset
set -o errexit
set -x

readonly xap_username_default='admin';
readonly xap_password_default='admin';
readonly metrics_config_file=./resources/metrics.xml ;
readonly metrics_temp_file=/tmp/metrics.xml ;

use_security=0
influx_username="${xap_username_default}"
influx_password="${xap_password_default}"
use_metrics=0
influx_host=''

function update_security() {
  if [[ ${use_security} == 1 ]];
  then
      if [[ -f ${metrics_temp_file} ]];
      then
        sed /SSS/d ${metrics_config_file} > ${metrics_temp_file} ;
      else
        sed /SSS/d ${metrics_config_file} > ${metrics_temp_file} ;
      fi
      sed -i "" s/"{{influx_username}}"/${influx_username}/ ${metrics_temp_file} ;
      echo -e "username = $influx_username password = $influx_password"
  fi
}

function update_metrics() {
    if [[ ${use_metrics} == 1 ]];
    then
        if [[ -f ${metrics_temp_file} ]]
        then
            sed -i "" /XXX/d ${metrics_temp_file}
        else
            sed /XXX/d ${metrics_config_file} > ${metrics_temp_file}
        fi
        sed -i "" s/"{{influx_host}}"/${influx_host}/ ${metrics_temp_file}
    fi
}

function parse_input() {
    if [[ $# > 0 && -n "$1" ]];
    then
        test "$1" == "--help" && ( show_usage ; exit 3 )
    fi
    while [[ $# > 0 ]]; do
        key="$1";
        case ${key} in
          -u)
            influx_username="$2"
            shift; shift ;;
          -p)
            influx_password="$2"
            shift; shift ;;
          -m)
            use_metrics=1
            shift ;;
          -i)
            influx_host="$2"
            shift; shift ;;
          -s)
            use_security=1
            use_metrics=1
            shift ;;
          *)
            echo "ERROR: Illegal argument to script: $key"
            echo ""
            show_usage;
            exit 3
            shift ;;
        esac
    done
}

function show_usage() {
    echo "   Usage: $0 [--help]";
    echo "";
    echo "   [-s -u <XAP username> -p <XAP password>]";
    echo "   [-m -i <InfluxDB host IP or name>]";
    echo "";
    echo "   To use InfluxDB security, pass the -s flag and username and password.";
    echo "   To have XAP report metrics to InfluxDB, use -m and -i.";
    echo "";
}


function make_sure() {
    if [[ ${use_metrics} == 0 && ${use_security} == 0 ]];
    then
        cp ${metrics_config_file} ${metrics_temp_file}
    fi
}

parse_input $*
update_security;
update_metrics;
make_sure;

echo "metrics.xml file has been updated and written to ${metrics_temp_file}.";
