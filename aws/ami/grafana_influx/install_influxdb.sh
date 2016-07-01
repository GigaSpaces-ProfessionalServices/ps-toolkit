#!/bin/bash
set -o nounset
set -o errexit

show_usage() {
    echo ""
    echo "Installs InfluxDB on current machine, starts the corresponding service and"
    echo "creates required InfluxDB database instances"
    echo ""
    echo "Usage: $0 [--help]"
    echo ""
}

parse_input() {
    if [[ $# -eq 1 && $1 == '--help' ]]; then
        show_usage; exit 0
    fi

    if [[ $# -gt 0 ]]; then
        echo "Invalid arguments encountered for script $0" >&2
        show_usage; exit 2
    fi
}

install_influxdb() {
    sudo apt-get update

    curl -sL https://repos.influxdata.com/influxdb.key | sudo apt-key add -
    source /etc/lsb-release
    echo "deb https://repos.influxdata.com/${DISTRIB_ID,,} ${DISTRIB_CODENAME} stable" | \
        sudo tee /etc/apt/sources.list.d/influxdb.list
    sudo apt-get update && sudo apt-get install -y influxdb zip
    sudo service influxdb start
    echo "CREATE DATABASE mydb" | influx
    echo "CREATE DATABASE grafana" | influx
}

main() {
    parse_input "$@"
    install_influxdb
}

main "$@"
