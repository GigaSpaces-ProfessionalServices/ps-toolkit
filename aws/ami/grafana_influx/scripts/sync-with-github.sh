#!/bin/bash
set -o errexit
set -o nounset

readonly github_repo="https://github.com/GigaSpaces-ProfessionalServices/ps-toolkit.git"
readonly github_root="/var/github"
readonly ps_toolkit="ps-toolkit"

show_usage() {
    echo ""
    echo "Retrieves \"Grafana\" machine scripts from GitHub and deploys them"
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

pull_scripts() {
    if [[ ! -d $github_root ]]; then
        echo "The local source tree directory '$github_root' is missing" >&2
        exit 1
    fi

    cd $github_root

    git_version=$(git --version)
    if [[ ! $git_version == "git version"* ]]; then
        echo "Git is not installed on current machine, cannot continue" >&2
    fi

    if [[ ! -d "$ps_toolkit" ]]; then
        sudo git clone $github_repo $ps_toolkit
    fi

    cd $ps_toolkit
    sudo git fetch $github_repo
}

copy_grafana_scripts() {
    echo ""
}

main() {
    parse_input "$@"
    pull_scripts
    copy_grafana_scripts
}

main "$@"
