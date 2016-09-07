#!/bin/bash
set -o errexit

show_usage() {
    echo ""
    echo "Deploys application to XAP grid using XAP maven plugin"
    echo ""
    echo "Usage: $0 [--help] [OPTIONS]... <path-to-project-directory>"
    echo ""
    echo "Optional parameters:"
    echo "  -l    --lookup-locators     Lookup locators"
    echo "  -g,   --lookup-groups       Lookup groups"
    echo ""
}

parse_input() {
    if [[ $# -eq 0 ]]; then
        echo "No path to XAP project directory was provided" >&2
        show_usage; exit 2
    fi
    
    if [[ $# -eq 1 && $1 == '--help' ]]; then
        show_usage; exit 0
    fi
    
     while [[ $# > 0 ]]; do
        case $1 in
        '-g' | '--lookup-groups')
            lookup_groups="$2"
            shift 2 ;;
        '-l' | '--lookup-locators')
            lookup_locators="$2"
            shift 2 ;;
        *)
            if [[ "$1" == "-"* ]]; then
                echo "Unknown option encountered: $1" >&2
                show_usage; exit 2
            fi

            # required parameter
            project_dir="$1"
            shift ;;
        esac
    done

    if [[ ! -d "$project_dir" ]]; then
        echo "The directory ${project_dir} does not exist"; exit 1
    fi
}

deploy() {
    mvn -f ${project_dir}/pom.xml clean package

    local deploy_cmd="mvn -f ${project_dir}/pom.xml os:deploy"
    
    if [[ $lookup_locators ]]; then
        deploy_cmd+=" -Dlocators=$lookup_locators"
    fi
    
    if [[ $lookup_groups ]]; then
        deploy_cmd+=" -Dgroups=$lookup_groups"
    fi
    $deploy_cmd
}

main() {
    parse_input "$@"
    deploy
}

main "$@"