#!/bin/bash
set -o errexit

show_usage() {
    echo ""
    echo "Creates XAP project tree with custom topology specified in XML files"
    echo ""
    echo "Usage: $0 [--help] [OPTIONS]..."
    echo ""
    echo "Mandatory parameters:"
    echo "  -t, --template     <xap-template>"
    echo ""
    echo "Optional parameters:"
    echo "  -pu                <pu-xml-path>"
    echo "  -sla               <sla-xml-path>"
    echo "  -g, --groupId      <package-name>"
    echo "  -a, --artifactId   <project-name>"
    echo ""
}

parse_input() {
    if [[ $# -eq 0 ]]; then
        show_usage; exit 2
    fi

    if [[ $1 == '--help' ]]; then
        show_usage; exit 0
    fi

    while [[ $# > 0 ]]; do
        case $1 in
        '-pu')
            pu_xml_path="$2"
            shift 2 ;;
        '-sla')
            sla_xml_path="$2"
            shift 2 ;;
        '-g' | '--groupId')
            group_id="$2"
            shift 2 ;;
        '-a' | '--artifactId')
            artifact_id="$2"
            shift 2 ;;
        '-t' | '--template')
            xap_template="$2"
            shift 2 ;;
        *)
            if [[ "$1" == "-"* ]]; then
                echo "Unknown option encountered: $1" >&2
            else
                echo "Unknown operand encountered: $1" >&2
            fi
            show_usage; exit 2
        esac
    done

    if [[ $pu_xml_path && -d $pu_xml_path ]]; then
        pu_xml_path="${pu_xml_path}/pu.xml"
    fi

    if [[ $sla_xml_path && -d $sla_xml_path ]]; then
        sla_xml_path="${sla_xml_path}/sla.xml"
    fi

    if [[ $pu_xml_path && ! -f $pu_xml_path ]]; then
        echo "Cannot locate PU configuration: ${pu_xml_path}" >&2
        exit 1
    fi

    if [[ $sla_xml_path && ! -f $sla_xml_path ]]; then
        echo "Cannot locate SLA configuration: ${sla_xml_path}" >&2
        exit 1
    fi

    if [[ -z $xap_template ]]; then
        echo "The project template has not been provided"
        show_usage; exit 1
    fi
}

copy_config() {
    if [[ $2 && -f $2 ]]; then
        find . -name "$1" | xargs -L1 cp -rf $2
    fi
}

create_project() {
    echo ""
    echo "=> Using the following configuration"
    echo "PU XML path: ${pu_xml_path}"
    echo "SLA XML path: ${sla_xml_path}"
    echo "Package name: ${group_id}"
    echo "Project name: ${artifact_id}"
    echo "XAP template: ${xap_template}"
    echo ""

    local cmd="mvn os:create -Dtemplate=$xap_template"

    if [[ $group_id ]]; then
        cmd+=" -DgroupId=$group_id"
    fi

    if [[ $artifact_id ]]; then
        cmd+=" -DartifactId=$artifact_id"
    fi

    $cmd
}

main() {
    parse_input "$@"
    create_project

    copy_config "pu.xml" ${pu_xml_path}
    copy_config "sla.xml" ${sla_xml_path}
}

main "$@"
