#!/bin/bash
set -o errexit

readonly pu_source_path="resources/partitioned-sync-replication-topology.template"
readonly sla_source_path="resources/partitioned-schema.template"

#general replication params
readonly repl_policy_type="partial-replication"
readonly repl_find_timeout=5000
readonly repl_full_take=false
readonly replicate_notify_templates=true
readonly trigger_notify_templates=false

#sync replication params
readonly throttle_when_inactive=true
readonly max_throttle_tp_when_inactive=50000
readonly min_throttle_tp_when_active=1000
readonly multiple_opers_chunk_size=10000
readonly target_consume_timeout=10000

#async replication params in case of recovery process
readonly repl_chunk_size=500
readonly repl_interval_millis=3000

readonly template="basic"
readonly artifact_id="my-app"
readonly space_url="/./space"
readonly conf_dest_dir="$artifact_id/processor/src/main/resources/META-INF/spring"
readonly cluster_schema="partitioned-sync2backup"
readonly number_of_backups=1
readonly max_instances_per_vm=1

show_usage() {
    echo ""
    echo "Starts XAP grid with partitioned topology and synchronous backup"
    echo "on specified number of virtual machines"
    echo ""
    echo "Usage: $0"
    echo "  [--help] <number-of-gsc-partitions> <number-of-vms>"
    echo ""
}

parse_input() {
    if [[ $1 == '--help' ]]; then
        show_usage; exit 0
    fi

    if [[ $# -lt 2 ]]; then
        echo "No grid startup details were provided" >&2
        show_usage; exit 2
    fi

    if [[ $# -gt 2 ]]; then
        echo "Invalid arguments encountered for script $0" >&2
        show_usage; exit 2
    fi

    number_of_instances=$1
    vm_count=$2
}

assemble_pu() {
    mv $1/pu.xml $1/pu_old.xml

    sed -e 's|{{imported_pu_xml}}|'"META-INF/spring/pu_old.xml"'|g' \
        -e 's|{{space_url}}|'"${space_url}"'|g' \
        -e 's|{{repl_policy_type}}|'"${repl_policy_type}"'|g' \
        -e 's|{{repl_find_timeout}}|'"${repl_find_timeout}"'|g' \
        -e 's|{{repl_full_take}}|'"${repl_full_take}"'|g' \
        -e 's|{{replicate_notify_templates}}|'"${replicate_notify_templates}"'|g' \
        -e 's|{{trigger_notify_templates}}|'"${trigger_notify_templates}"'|g' \
        -e 's|{{throttle_when_inactive}}|'"${throttle_when_inactive}"'|' \
        -e 's|{{max_throttle_tp_when_inactive}}|'"${max_throttle_tp_when_inactive}"'|g' \
        -e 's|{{min_throttle_tp_when_active}}|'"${min_throttle_tp_when_active}"'|g' \
        -e 's|{{target_consume_timeout}}|'"${target_consume_timeout}"'|g' \
        -e 's|{{repl_chunk_size}}|'"${repl_chunk_size}"'|g' \
        -e 's|{{repl_interval_millis}}|'"${repl_interval_millis}"'|g' \
        -e 's|{{multiple_opers_chunk_size}}|'"${multiple_opers_chunk_size}"'|g' \
        ${pu_source_path} > $1/pu.xml
}

assemble_sla() {
    sed -e 's|{{cluster_schema}}|'"${cluster_schema}"'|g' \
        -e 's|{{number_of_instances}}|'"${number_of_instances}"'|g' \
        -e 's|{{number_of_backups}}|'"${number_of_backups}"'|g' \
        -e 's|{{max_instances_per_vm}}|'"${max_instances_per_vm}"'|g' \
        ${sla_source_path} > $1/sla.xml
}

create_basic_project() {
    ./customize-topology.sh -t $template -a $artifact_id

    assemble_pu $conf_dest_dir
    assemble_sla $conf_dest_dir
}

boot_grid() {
    ./boot-grid.sh --node-count $vm_count -s "partitioned-sync-replicated-grid" $artifact_id
}

main() {
    parse_input "$@"
    create_basic_project
    boot_grid
}

main "$@"
