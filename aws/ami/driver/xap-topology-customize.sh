#!/bin/bash
set -o errexit

pu_xml_path=
sla_xml_path=
group_id=
artifact_id=

function show_usage() {
    echo ""
    echo "Usage: $0 [--help]"
    echo "    [-pu <pu-xml-path>]"
    echo "    [-sla <sla-xml-path>]"
    echo "    [-g|--groupId <package-name>]"
    echo "    [-a|--artifactId <project-name>]"
    echo "    -t|--template <xap-template>"
    echo ""
}
function parse_input() {
    if [[ $1 == "--help" ]]; then
        show_usage; exit 0
    fi

    while [[ $# > 1 ]]; do
        key="$1"
        case $key in
        -pu)
            pu_xml_path="$2"
            shift ;;
        -sla)
            sla_xml_path="$2"
            shift ;;
        -g|--groupId)
            group_id="$2"
            shift ;;
        -a|--artifactId)
            artifact_id="$2"
            shift ;;
        -t|--template)
            xap_template="$2"
            shift ;;
        *)
            ;; # unknown option
        esac
        shift
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

    if [[ $pu_xml_path && ! -f $sla_xml_path ]]; then
        echo "Cannot locate SLA configuration: ${sla_xml_path}" >&2
        exit 1
    fi

    if [[ -z $xap_template ]]; then
        echo "The project template has not been provided"
        show_usage; exit 1
    fi
}
function copy_config() {
   if [[ $2 && -f $2 ]]; then
     find . -name "$1" | xargs -L1 cp -rf $2
   fi
}
function create_project() {
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
function main() {
    parse_input "$@"

    create_project

    copy_config "pu.xml" ${pu_xml_path}
    copy_config "sla.xml" ${sla_xml_path}
}
main "$@"
