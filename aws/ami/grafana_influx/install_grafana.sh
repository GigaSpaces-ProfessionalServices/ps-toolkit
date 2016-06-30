#!/bin/bash
set -o nounset
set -o errexit

show_usage() {
    echo ""
    echo "Downloads and installs Grafana distribution package for Debian-based systems"
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

install_grafana() {
    sudo apt-get update

    wget https://grafanarel.s3.amazonaws.com/builds/grafana_3.0.4-1464167696_amd64.deb
    sudo apt-get install -y adduser libfontconfig zip
    rm grafana_3.0.4-1464167696_amd64.deb
    sudo dpkg -i grafana_3.0.4-1464167696_amd64.deb
}

main() {
    parse_input "$@"
    install_grafana
}

main "$@"
