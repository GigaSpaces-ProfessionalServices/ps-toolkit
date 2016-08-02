#!/bin/bash
set -o errexit

readonly pu_templates="pu_configuration_templates"
readonly pu_source_path="$pu_templates/sync-replication-topology.template"
readonly sla_source_path="$pu_templates/replicated-schema.template"

#general replication params
readonly repl_policy_type="partial-replication"
readonly repl_find_timeout=5000
readonly repl_full_take=false
readonly replicate_notify_templates=false
readonly trigger_notify_templates=false
readonly on_conflicting_packets="ignore"

#sync replication params
readonly throttle_when_inactive=false
readonly max_throttle_tp_when_inactive=50000
readonly min_throttle_tp_when_active=1000
readonly multiple_opers_chunk_size=10000
readonly target_consume_timeout=10000

#async replication params in case of recovery process
readonly repl_chunk_size=500
readonly repl_interval_millis=3000

readonly template="basic"
readonly space_url="/./space"
readonly conf_dest_dir="processor/src/main/resources/META-INF/spring"
readonly cluster_schema="sync_replicated"
readonly max_instances_per_vm=1

readonly sctipts_dir=$(dirname $0)

show_usage() {
    echo ""
    echo "Creates a basic SBA application with synchronous data replication topology "
    echo ""
    echo "Usage: $0"
    echo "  [--help] <project-dir>"
    echo ""
}

parse_input() {
    if [[ $# -eq 1 && $1 == '--help' ]]; then
        show_usage; exit 0
    fi

    if [[ $# -eq 0 ]]; then
        echo "No grid startup details were provided" >&2
        show_usage; exit 2
    fi

    if [[ $# -gt 1 ]]; then
        echo "Invalid arguments encountered for script $0" >&2
        show_usage; exit 2
    fi

    project_dir=$1    
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
        -e 's|{{on_conflicting_packets}}|'"${on_conflicting_packets}"'|g' \
        -e 's|{{throttle_when_inactive}}|'"${throttle_when_inactive}"'|' \
        -e 's|{{max_throttle_tp_when_inactive}}|'"${max_throttle_tp_when_inactive}"'|g' \
        -e 's|{{min_throttle_tp_when_active}}|'"${min_throttle_tp_when_active}"'|g' \
        -e 's|{{target_consume_timeout}}|'"${target_consume_timeout}"'|g' \
        -e 's|{{repl_chunk_size}}|'"${repl_chunk_size}"'|g' \
        -e 's|{{repl_interval_millis}}|'"${repl_interval_millis}"'|g' \
        -e 's|{{multiple_opers_chunk_size}}|'"${multiple_opers_chunk_size}"'|g' \
        $sctipts_dir/$pu_source_path > $1/pu.xml
}

assemble_sla() {
    sed -e 's|{{cluster_schema}}|'"${cluster_schema}"'|g' \
        -e 's|{{number_of_instances}}|'"${number_of_instances}"'|g' \
        -e 's|{{max_instances_per_vm}}|'"${max_instances_per_vm}"'|g' \
        $sctipts_dir/$sla_source_path > $1/sla.xml
}

create_basic_project() {
    $sctipts_dir/customize-topology.sh -t $template -a $project_dir

    assemble_pu $project_dir/$conf_dest_dir
    assemble_sla $project_dir/$conf_dest_dir
}

main() {
    parse_input "$@"
    create_basic_project
}

main "$@"
