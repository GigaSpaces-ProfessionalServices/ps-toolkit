#!/bin/bash
set -o errexit

readonly pu_templates="pu_configuration_templates"
readonly pu_template_path="$pu_templates/partitioned-sync-replication-with-mirror-topology.template"
readonly mirror_pu_template_path="$pu_templates/mirror.template"
readonly sla_template_path="$pu_templates/partitioned-schema.template"

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

readonly template="basic-async-persistency"
readonly space_name="space"
readonly conf_dest_dir="processor/src/main/resources/META-INF/spring"
readonly mirror_conf_dest_dir="mirror/src/main/resources/META-INF/spring"
readonly cluster_schema="partitioned-sync2backup"
readonly number_of_backups=1
readonly max_instances_per_vm=1

#mirror service params
readonly mirror_name="mirror-service"
readonly mirror_bulk_size=100
readonly mirror_interval_millis=2000
readonly mirror_interval_opers=100
readonly on_redo_log_capacity_exceeded="block-operations"
readonly redo_log_capacity=150000

#db settings
readonly db_path="~/hsqldb-catalogs/my-app-testDB"
readonly db_name="testDB"
readonly db_user="SA"
readonly db_password=""
readonly mapping_resources="<value>com.mycompany.app.common.Data</value>"

readonly sctipts_dir=$(dirname $0)

show_usage() {
    echo ""
    echo "Creates a basic SBA application with partitioned topology, synchronous backup and async database mirroring"
    echo "Starts HSQLDB server on requested host"
    echo ""
    echo "Usage: $0"
    echo "  [--help] <project-dir> <db-host>"
    echo ""
}

parse_input() {
    if [[ $# -eq 1 && $1 == '--help' ]]; then
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

    project_dir=$1
    db_host=$2
}

assemble_pu() {
    mv $1/pu.xml $1/pu_old.xml

    sed -e 's|{{imported_pu_xml}}|'"META-INF/spring/pu_old.xml"'|g' \
        -e 's|{{space_name}}|'"${space_name}"'|g' \
        -e 's|{{db_host}}|'"${db_host}"'|g' \
        -e 's|{{db_name}}|'"${db_name}"'|g' \
        -e 's|{{db_user}}|'"${db_user}"'|g' \
        -e 's|{{db_password}}|'"${db_password}"'|g' \
        -e 's|{{mapping_resources}}|'"${mapping_resources}"'|g' \
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
        -e 's|{{mirror_name}}|'"${mirror_name}"'|g' \
        -e 's|{{mirror_bulk_size}}|'"${mirror_bulk_size}"'|g' \
        -e 's|{{mirror_interval_millis}}|'"${mirror_interval_millis}"'|g' \
        -e 's|{{mirror_interval_opers}}|'"${mirror_interval_opers}"'|g' \
        -e 's|{{on_redo_log_capacity_exceeded}}|'"${on_redo_log_capacity_exceeded}"'|g' \
        -e 's|{{redo_log_capacity}}|'"${redo_log_capacity}"'|g' \
        $sctipts_dir/$pu_template_path > $1/pu.xml
}

assemble_sla() {
    sed -e 's|{{cluster_schema}}|'"${cluster_schema}"'|g' \
        -e 's|{{number_of_instances}}|'"${number_of_instances}"'|g' \
        -e 's|{{number_of_backups}}|'"${number_of_backups}"'|g' \
        -e 's|{{max_instances_per_vm}}|'"${max_instances_per_vm}"'|g' \
        $sctipts_dir/$sla_template_path > $1/sla.xml
}

assemble_mirror() {
    sed -e 's|{{db_path}}|'"${db_path}"'|g' \
        -e 's|{{db_name}}|'"${db_name}"'|g' \
        -e 's|{{db_host}}|'"${db_host}"'|g' \
        -e 's|{{db_user}}|'"${db_user}"'|g' \
        -e 's|{{db_password}}|'"${db_password}"'|g' \
        -e 's|{{mapping_resources}}|'"${mapping_resources}"'|g' \
        -e 's|{{mirror_name}}|'"${mirror_name}"'|g' \
        -e 's|{{mapping_resources}}|'"${mapping_resources}"'|g' \
        -e 's|{{space_name}}|'"${space_name}"'|g' \
        $sctipts_dir/$mirror_pu_template_path > ${1}/pu.xml
}

create_basic_project() {
    $sctipts_dir/customize-topology.sh -t $template -a $project_dir

    assemble_pu $project_dir/$conf_dest_dir
    assemble_sla $project_dir/$conf_dest_dir
    assemble_mirror $project_dir/$mirror_conf_dest_dir
}

start_hsqldb_server() {
    ssh $db_host <<ENDSSH
    wget -O /tmp/hsqldb.jar http://central.maven.org/maven2/org/hsqldb/hsqldb/2.3.2/hsqldb-2.3.2.jar
    mkdir -p ~/hsqldb-catalogs
    echo "server.remote_open=true" > ~/hsqldb-catalogs/server.properties
    echo "Starting HSQLDB server: database=${db_path}, alias=${db_name}..."
    nohup java -cp /tmp/hsqldb.jar org.hsqldb.server.Server --database.0 file:$db_path --dbname.0 $db_name --props ~/hsqldb-catalogs/server.properties >/dev/null 2>&1 &
    ENDSSH
}

main() {
    parse_input "$@"
    create_basic_project
    start_hsqldb_server
}

main "$@"
