#!/bin/bash
set -o nounset
set -o errexit
#set -x

readonly influx_username_default='';
readonly influx_password_default='';
readonly metrics_config_file=./resources/metrics.xml ;
readonly metrics_temp_file=/tmp/metrics.xml ;

influx_username="${influx_username_default}"
influx_password="${influx_password_default}"
use_security=0
use_metrics=0
use_grafana=0
influx_host=''
grafana_host=''

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
      sed -i "" s/"{{influx_password}}"/${influx_password}/ ${metrics_temp_file} ;
  fi
}

function update_metrics() {
    if [[ ${use_metrics} == 1 ]];
    then
        if [[ -f ${metrics_temp_file} ]]
        then
            sed -i "" /MMM/d ${metrics_temp_file}
        else
            sed /MMM/d ${metrics_config_file} > ${metrics_temp_file}
        fi
        if [[ ${influx_host} != '' ]];
        then
            sed -i "" s/"{{influx_host}}"/${influx_host}/ ${metrics_temp_file}
        fi
    fi
}

function update_grafana() {
    if [[ ${use_grafana} == 1 ]];
    then
        if [[ -f ${metrics_temp_file} ]]
        then
            sed -i "" /GGG/d ${metrics_temp_file}
        else
            sed /GG/d ${metrics_config_file} > ${metrics_temp_file}
        fi
        sed -i "" s/"{{grafana_host}}"/${grafana_host}/ ${metrics_temp_file}
        # this next one is a little unexpected, but covers for the case where the user
        # uses -g <grafana_host> without giving -i <influx_host>
        if [[ ${influx_host} == '' ]];
        then
            sed -i "" s/"{{influx_host}}"/${grafana_host}/ ${metrics_temp_file}
        fi
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
          -g)
            echo "XXX"
            echo "parsing g, host = $2"
            echo "XXX"
            use_grafana=1
            use_metrics=1
            grafana_host="$2"
            shift; shift;;
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
    echo "";
    echo "   Usage: $0 [--help]";
    echo "";
    echo "      Or: $0 [<options>]";
    echo "";
    echo "   Options are from the following:"
    echo "";
    echo "   [-m -i <InfluxDB host IP or name>]";
    echo "   [-s -u <InfluxDB username> -p <InfluxDB password>]";
    echo "   [-g <grafana hostname or ip>]"
    echo "";
    echo "   To have XAP report metrics to InfluxDB, use -m and -i.";
    echo "   To use InfluxDB security, pass the -s flag and username and password.";
    echo "   To enable dashboard support in WEB-UI, pass the -g flag and a hostname or ip where Grafana is running/will run.";
    echo "";
}


function make_sure() {
    if [[ ${use_metrics} == 0 && ${use_security} == 0 && ${use_grafana} == 0 ]];
    then
        cp ${metrics_config_file} ${metrics_temp_file}
    fi
}

parse_input $*
update_security;
update_metrics;
update_grafana;
make_sure;

echo ""
echo "The metrics.xml file has been updated and written to ${metrics_temp_file}.";
