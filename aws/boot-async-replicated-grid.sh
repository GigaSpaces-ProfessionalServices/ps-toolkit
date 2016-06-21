#!/bin/bash
set -o errexit

readonly pu_source_path="resources/async-replication-topology.template"
readonly sla_source_path="resources/replicated-schema.template"

#general replication params
readonly repl_policy_type="partial-replication"
readonly repl_find_timeout=5000
readonly repl_full_take=false
readonly replicate_notify_templates=false
readonly trigger_notify_templates=false
readonly on_conflicting_packets="ignore"

#async replication params
readonly repl_chunk_size=500
readonly repl_interval_millis=3000
readonly repl_interval_opers=500
readonly async_channel_shutdown_timeout=300000

readonly template="basic"
readonly artifact_id="my-app"
readonly conf_dest_dir="$artifact_id/processor/src/main/resources/META-INF/spring"
readonly space_url="/./space"
readonly cluster_schema="async_replicated"
readonly max_instances_per_vm=1

number_of_instances=
vm_count=

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
        -e 's|{{repl_chunk_size}}|'"${repl_chunk_size}"'|g' \
        -e 's|{{repl_interval_millis}}|'"${repl_interval_millis}"'|g' \
        -e 's|{{repl_interval_opers}}|'"${repl_interval_opers}"'|g' \
        -e 's|{{async_channel_shutdown_timeout}}|'"${async_channel_shutdown_timeout}"'|g' \
        ${pu_source_path} > $1/pu.xml
}
assemble_sla() {
    sed -e 's|{{cluster_schema}}|'"${cluster_schema}"'|g' \
        -e 's|{{number_of_instances}}|'"${number_of_instances}"'|g' \
        -e 's|{{max_instances_per_vm}}|'"${max_instances_per_vm}"'|g' \
        ${sla_source_path} > $1/sla.xml
}
create_basic_project() {
   ./xap-topology-customize.sh -t $template -a $artifact_id
   
   assemble_pu $conf_dest_dir
   assemble_sla $conf_dest_dir
}
boot_grid() {
   ./boot-grid.sh $artifact_id --count $vm_count -s "async-replicated-grid"
}
show_usage() {
   echo "Usage: $0 <number-of-instances> <vm-count>"
}
main() {
   if [[ "$#" -ne 2 ]] ; then
      show_usage; exit 1
   else
      number_of_instances=$1
      vm_count=$2
   fi
   create_basic_project
   boot_grid
}
main "$@"
