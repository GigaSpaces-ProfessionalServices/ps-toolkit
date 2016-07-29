#!/bin/bash
set -o nounset
set -o errexit

readonly metrics_config_file=./resources/metrics.xml
readonly metrics_temp_file=/tmp/metrics.xml

function show_usage() {
    echo ""
    echo "Configures Grafana metrics file for the specified hostname"
    echo ""
    echo "Usage: $0 [--help]"
    echo "   Or: $0 [OPTIONS]..."
    echo ""
    echo "Options are from the following:"
    echo "  [-m, --use-metrics]"
    echo "  [-u, --influx-username <influx-db-username>]"
    echo "  [-p, --influx-password <influx-db-password>]"
    echo "  [-i, --influx-host <influx-db-hostname-or-ip-address>]"
    echo "  -g, --grafana-host <grafana-hostname-or-ip-address>"
    echo ""
    echo "To have XAP report metrics to InfluxDB, use -m and -i."
    echo "To use InfluxDB security, pass username and password."
    echo "To enable dashboard support in WEB-UI, pass the -g flag and"
    echo "a hostname or IP where Grafana is running/will run."
    echo ""
}

function parse_input() {
    if [[ $# -eq 1 && $1 == '--help' ]]; then
        show_usage; exit 0
    fi

    while [[ $# > 0 ]]; do
        key="$1";
        case ${key} in
        '-m' | '--use-metrics')
            use_metrics=1
            shift ;;
        '-u' | '--influx-username')
            influx_username="$2"
            use_security=1
            use_metrics=1
            shift; shift ;;
        '-p' | '--influx-password')
            influx_password="$2"
            shift; shift ;;
        '-i' | '--influx-host')
            influx_host="$2"
            shift; shift ;;
        '-g' | '--grafana-host')
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

    if [[ ${#grafana_host} -eq 0 ]]; then
        echo "No Grafana hostname or IP address was provided" >&2
        show_usage; exit 2
    fi
}

function update_security() {
  if [[ ${use_security} == 1 ]];
  then
      if [[ -f ${metrics_temp_file} ]];
      then
        sed /SSS/d ${metrics_config_file} > ${metrics_temp_file} ;
      else
        sed /SSS/d ${metrics_config_file} > ${metrics_temp_file} ;
      fi
      sed -i s/"{{influx_username}}"/${influx_username}/ ${metrics_temp_file} ;
      sed -i s/"{{influx_password}}"/${influx_password}/ ${metrics_temp_file} ;
  fi
}

function update_metrics() {
    if [[ ${use_metrics} == 1 ]];
    then
        if [[ -f ${metrics_temp_file} ]]
        then
            sed -i /MMM/d ${metrics_temp_file}
        else
            sed /MMM/d ${metrics_config_file} > ${metrics_temp_file}
        fi
        if [[ ${influx_host} != '' ]];
        then
            sed -i s/"{{influx_host}}"/${influx_host}/ ${metrics_temp_file}
        fi
    fi
}

function update_grafana() {
    if [[ ${use_grafana} == 1 ]];
    then
        if [[ -f ${metrics_temp_file} ]]
        then
            sed -i /GGG/d ${metrics_temp_file}
        else
            sed /GG/d ${metrics_config_file} > ${metrics_temp_file}
        fi
        sed -i s/"{{grafana_host}}"/${grafana_host}/ ${metrics_temp_file}
        # this next one is a little unexpected, but covers for the case where the user
        # uses -g <grafana_host> without giving -i <influx_host>
        if [[ ${influx_host} == '' ]];
        then
            sed -i s/"{{influx_host}}"/${grafana_host}/ ${metrics_temp_file}
        fi
    fi
}

function make_sure() {
    if [[ ${use_metrics} == 0 && ${use_security} == 0 && ${use_grafana} == 0 ]];
    then
        cp ${metrics_config_file} ${metrics_temp_file}
    fi
}

main() {
    influx_username=''
    influx_password=''
    use_security=0
    use_metrics=0
    use_grafana=0
    influx_host=''
    grafana_host=''

    parse_input $*
    update_security;
    update_metrics;
    update_grafana;
    make_sure;

    echo ""
    echo "The metrics.xml file has been updated and written to ${metrics_temp_file}.";
}

main "$@"
