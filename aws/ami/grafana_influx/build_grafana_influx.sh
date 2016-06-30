#!/bin/bash
set -o nounset
set -o errexit

show_usage() {
    echo ""
    echo "Installs and configures InfluxDB and Grafana on the current machine"
    echo ""
    echo "Usage: $0 [--help]"
    echo ""
}

parse_input() {
    if [[ $# == 1 && $1 == '--help' ]]; then
        show_usage; exit 0
    fi

    if [[ $# -gt 0 ]]; then
        echo "Invalid arguments encountered for script $0" >&2
        show_usage; exit 2
    fi
}

main() {
    parse_input "$@"

    ./install_influxdb.sh
    ./install_grafana.sh
}

main "$@"
