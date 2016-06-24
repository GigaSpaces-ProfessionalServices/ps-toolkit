#!/bin/bash
set -o errexit

show_usage() {
    echo ""
    echo "Configures the startup environment for running XAP EC2 grid"
    echo ""
    echo "Usage $0 [--help] [OPTIONS]..."
    echo ""
    echo "Optional parameters:"
    echo "  -h,   --xap-home          <xap-home-dir>" 
    echo "  -g,   --lookup-groups     <lookup-groups>"
    echo "  -l,   --lookup-locators   <lookup-locators>"
    echo "  -nic, --nic-address       <nic-ip-address>"
    echo "  -lic, --xap-license       <path-to-gslicense-xml>"
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
    if [[ ${jshomedir} ]]; then
        grep -q -F "JSHOMEDIR" $bash_profile || echo "export JSHOMEDIR=\"${jshomedir}\"" >>$bash_profile
    fi

    if [[ ${java_home} ]]; then
        grep -q -F "JAVA_HOME" $bash_profile || echo "export JAVA_HOME=\"${java_home}\" PATH=\"${PATH}:${java_home}/bin\"" >>$bash_profile
    fi

    if [[ ${lookup_groups} ]]; then
        grep -q -F "LOOKUPGROUPS" $bash_profile || echo "export LOOKUPGROUPS=\"${lookup_groups}\"" >>$bash_profile
    fi

    if [[ ${lookup_locators} ]]; then
        grep -q -F "LOOKUPLOCATORS" $bash_profile || echo "export LOOKUPLOCATORS=\"${lookup_locators}\"" >>$bash_profile
    fi

    if [[ ${nic_address} ]]; then
        grep -q -F "NIC_ADDR" $bash_profile || echo "export NIC_ADDR=\"${nic_address}\"" >>$bash_profile
    fi

    source ${bash_profile}

    if [[ ${gs_license} ]]; then
        if [[ -f "${gs_license}" ]]; then
            cp -rf ${gs_license} ${JSHOMEDIR}
        else
            echo "License ${gs_license} does not exist. No license was installed." 1>&2
        fi
    fi
}

main() {
    readonly java_home="/opt/java/jdk1.7.0_79"
    readonly ip_address=$(/sbin/ifconfig eth0 2>/dev/null|awk '/inet addr:/ {print $2}'|sed 's/addr://')
    readonly bash_profile=~/.profile

    jshomedir="/opt/gigaspaces/current"
    lookup_locators=$ip_address
    nic_address=$ip_address
    gs_license=/tmp/gslicense.xml

    parse_input "$@"
    configure
}

main "$@"
