#!/bin/bash
set -o errexit
set -o nounset

readonly github_root="/var/github/ps-toolkit"

show_usage() {
    echo ""
    echo "Retrieves \"Driver\" machine scripts from GitHub and deploys them"
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

pull_driver_scripts() {
    if [[ ! -d $github_root ]]; then
        echo "The local source tree directory '$github_root' is missing" >&2
        exit 1
    fi
}

main() {
    parse_input "$@"
    pull_driver_scripts
}

main "$@"
