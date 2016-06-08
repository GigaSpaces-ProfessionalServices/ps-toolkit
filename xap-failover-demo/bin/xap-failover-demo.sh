#!/bin/bash
set -o errexit

readonly stack_name="xap-failover-demo"
readonly template_uri="file:///tmp/boot-grid.template"
readonly lookup_groups="xap-failover-demo-group"
lookup_locators=

readonly project_dir="/tmp"
readonly project_name="test-xap-failover"
readonly project_template="basic"

readonly monitor_tool_path="/tmp/monitor-tool"

readonly sla_file="/tmp/sla.xml"
readonly cluster_schema="partitioned-sync2backup"
readonly number_of_instances=4
readonly number_of_backups=1

create_project() {
  cd $project_dir
  mvn os:create -DartifactId=$project_name -Dtemplate=$project_template
}
create_sla() {
  {
    echo '<?xml version="1.0" encoding="UTF-8"?>'
    echo '<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:os-sla="http://www.openspaces.org/schema/sla" xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.2.xsd http://www.openspaces.org/schema/sla http://www.openspaces.org/schema/10.0/sla/openspaces-sla.xsd">'
    echo '<os-sla:sla cluster-schema="'"$cluster_schema"'" number-of-instances="'"$number_of_instances"'" number-of-backups="'"$number_of_backups"'" max-instances-per-vm="1" />'
    echo '</beans>'
  } >$sla_file
}
create_vms() {
   local stack_id
   if ! stack_id=$(aws cloudformation create-stack --stack-name ${stack_name} --template-body ${template_uri} --parameters ParameterKey=LookupGroups,ParameterValue=$lookup_groups --query 'StackId' --output text); then
     exit $?
   fi
   echo "Creating stack ${stack_id}. Please wait..."

   aws cloudformation wait stack-create-complete --stack-name ${stack_name}

   lookup_locators=$(aws cloudformation describe-stacks --stack-name ${stack_name} --query 'Stacks[*].Outputs[?OutputKey==`MgtPrivateIP`].OutputValue[]' --output text)
   echo "${stack_name} has been created. Use http://${lookup_locators}:8099 to reach Web Management Console."
}
deploy() {
   cd ${project_dir}/${project_name}
   mvn package
   mvn os:deploy -Dgroups=$lookup_groups -Dlocators=$lookup_locators -Dsla=$sla_file
}
setup() {
  create_project
  create_sla
  create_vms
  deploy
}
failover_demo() {
  lookup_locators=$(aws cloudformation describe-stacks --stack-name ${stack_name} --query 'Stacks[*].Outputs[?OutputKey==`MgtPrivateIP`].OutputValue[]' --output text)
  nohup java -cp $monitor_tool_path/xap-failover-demo.jar:$monitor_tool_path/lib/* com.gigaspaces.gigapro.alert.FailoverDemo "$lookup_groups" "$lookup_locators" >$monitor_tool_path/output.log 2>&1 &

  local compute_node_host=$(aws cloudformation describe-stacks --stack-name ${stack_name} --query 'Stacks[*].Outputs[?OutputKey==`ComputeNode1PrivateIP`].OutputValue[]' --output text 2> /dev/null )
  if [[ -z $compute_node_host ]]; then
     echo "${stack_name} stack does not exist."; exit 1
  fi

  ssh $compute_node_host <<EOF
     readonly pids=\$(ps aux | grep -v grep | grep GSC | awk '{print \$2}')
     if [[ -z \$pids ]]; then
        echo "No gscs are running on ${compute_node_host}."; exit 1
     fi
     IFS=' ' read -r -a pid_arr <<< \$pids
     echo "Demonstrating failover by killing GSC (pid: \${pid_arr[0]}) on ${compute_node_host}..."
     kill -9 \${pid_arr[0]}

     TIMEOUT=60
     while ps -p \${pid_arr[0]}> /dev/null; do
       if [[ \$TIMEOUT -le 0 ]]; then
          echo "GSC \${pid_arr[0]} has not been stopped within \$TIMEOUT seconds"
     	  exit 1
       fi
       let "TIMEOUT--"
       sleep 1
     done
     echo "GSC \${pid_arr[0]} has been killed."
EOF
}
teardown() {
  echo "Deleting stack ${stack_name}. Please wait..."
  aws cloudformation delete-stack --stack-name ${stack_name}
  aws cloudformation wait stack-delete-complete --stack-name ${stack_name}
  echo "${stack_name} has been deleted." 

  rm -rf ${project_dir}/${project_name}
  rm -rf $sla_file
}
case "$1" in
  setup)
    setup
    ;;
  demo)
    failover_demo 
    ;;
  teardown)
    teardown
    ;;
  *)
    echo "Usage: $0 {setup|demo|teardown}" >&2
    exit 1
    ;;
esac
