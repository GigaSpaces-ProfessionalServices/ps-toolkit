#!/bin/bash
set -o errexit

show_usage() {
    echo ""
    echo "Configures the startup environment for running XAP EC2 grid"
    echo ""
    echo "Usage $0 [--help] [OPTIONS]..."
    echo ""
    echo "Optional parameters:"
    echo "  -j    --java-home         <java-home-dir>"
    echo "  -h,   --xap-home          <xap-home-dir>" 
    echo "  -g,   --lookup-groups     <lookup-groups>"
    echo "  -l,   --lookup-locators   <lookup-locators>"
    echo "  -nic, --nic-address       <nic-ip-address>"
    echo "  -lic, --xap-license       <path-to-gslicense-xml>"
    echo ""
}

parse_input() {
    if [[ $# -eq 1 && $1 == '--help' ]]; then
        show_usage; exit 0
    fi

    while [[ $# > 0 ]]; do
        case $1 in
        '-j' | 'java-home')
            java_home="$2"
            shift 2 ;;
        '-h' | '--xap-home')  
            jshomedir="$2"  
            shift 2 ;;
        '-g' | '--lookup-groups')
            lookup_groups="$2"
            shift 2 ;;
        '-l' | '--lookup-locators')
            lookup_locators="$2"
            shift 2 ;;
        '-nic' | '--nic-address')
            nic_address="$2"
            shift 2 ;;
        '-lic' | '--xap-license')
            gs_license="$2"
            shift 2 ;;
        *)
            if [[ "$1" == "-"* ]]; then
                echo "Unknown option encountered: $1" >&2
            else
                echo "Unknown operand encountered: $1" >&2
            fi
            show_usage; exit 2
        esac
        shift
    done
}

configure() {
    local content;
    if [[ ${jshomedir} ]]; then
        content+="export JSHOMEDIR=\"${jshomedir}\"\n"
    fi

    if [[ ${java_home} ]]; then
        content+="export JAVA_HOME=\"${java_home}\"\nexport PATH=\"${PATH}:${java_home}/bin\"\n"
    fi

    if [[ ${lookup_groups} ]]; then
        content+="export LOOKUPGROUPS=\"${lookup_groups}\"\n"
    fi

    if [[ ${lookup_locators} ]]; then
        content+="export LOOKUPLOCATORS=\"${lookup_locators}\"\n"
    fi

    if [[ ${nic_address} ]]; then
        content+="export NIC_ADDR=\"${nic_address}\"\n"
    fi
    
    content+="# XAP configured at [$(date +'%Y-%b-%d %H:%M:%S')]. [$(readlink -f /opt/gigaspaces/current)]"

    echo ""
    echo -e $content | tee $config_file && chmod +x $config_file
    echo ""

    if [[ ${gs_license} ]]; then
        if [[ -f "${gs_license}" ]]; then
            cp -rf ${gs_license} ${JSHOMEDIR}
        else
            echo "License ${gs_license} does not exist. No license was installed." 1>&2
        fi
    fi
}

main() {
    readonly ip_address=$(/sbin/ifconfig eth0 2>/dev/null|awk '/inet addr:/ {print $2}'|sed 's/addr://')

    java_home="/opt/java/jdk1.7.0_79"
    jshomedir="/opt/gigaspaces/current"
    lookup_locators=$ip_address
    nic_address=$ip_address
    gs_license=/tmp/gslicense.xml

    config_file=$(dirname $0)/setenv.sh
    
    parse_input "$@"
    configure
}

main "$@"
