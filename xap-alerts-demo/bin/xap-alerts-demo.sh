#!/bin/bash
set -o errexit

readonly stack_name="xap-alerts-demo"
readonly monitor_tool_path="$(pwd)/.."
readonly template_uri="file:///$(pwd)/boot-grid.template"
readonly lookup_groups="xap-alerts-demo-group"
lookup_locators=

readonly project_dir="/tmp"
readonly project_name="basic-xap-project"
readonly project_template="basic"

readonly sla_file="${project_dir}/sla.xml"
readonly cluster_schema="partitioned-sync2backup"
readonly number_of_instances=4
readonly number_of_backups=1

create_project() {
  cd $project_dir
  mvn os:create -DartifactId=$project_name -Dtemplate=$project_template > /dev/null
  echo "${project_dir}/${project_name} project has been created using $project_template template"
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
   echo "Deploying ${project_name} application with ${number_of_instances},${number_of_backups} topology"
   mvn package > /dev/null
   mvn os:deploy -Dgroups=$lookup_groups -Dlocators=$lookup_locators -Dsla=$sla_file > /dev/null
   echo "${project_name} has been deployed"
}
run_monitor_tool() {
  nohup java -jar $monitor_tool_path/xap-alerts-demo.jar "$lookup_groups" "$lookup_locators" -Dprocess.marker=monitor-tool-marker > /dev/null 2>&1 &
}
stop_monitor_tool() {
  if pid=$(ps aux | grep -v grep | grep process.marker=monitor-tool-marker | awk '{print $2}'); then
      kill -SIGTERM $pid
  fi; 
}
failover_demo() {
  local compute_node_host=$(aws cloudformation describe-stacks --stack-name ${stack_name} --query 'Stacks[*].Outputs[?OutputKey==`ComputeNode1PrivateIP`].OutputValue[]' --output text 2> /dev/null )
  if [[ -z $compute_node_host ]]; then
     echo "${stack_name} stack does not exist."; exit 1
  fi

  ssh -oStrictHostKeyChecking=no $compute_node_host <<EOF
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
cpu_load_demo() {
  local compute_node_host=$(aws cloudformation describe-stacks --stack-name ${stack_name} --query 'Stacks[*].Outputs[?OutputKey==`ComputeNode2PrivateIP`].OutputValue[]' --output text 2> /dev/null )
  if [[ -z $compute_node_host ]]; then
     echo "${stack_name} stack does not exist."; exit 1
  fi
  echo "Producing CPU load on ${compute_node_host} during 80 seconds..."
  ssh -oStrictHostKeyChecking=no $compute_node_host stress --cpu 1 --timeout 80
}
teardown() {
  echo "Deleting stack ${stack_name}. Please wait..."
  aws cloudformation delete-stack --stack-name ${stack_name}
  aws cloudformation wait stack-delete-complete --stack-name ${stack_name}
  echo "${stack_name} has been deleted." 

  rm -rf ${project_dir}/${project_name}
  rm -rf $sla_file
  
  stop_monitor_tool
}
case "$1" in
  setup)
    create_project
    create_sla
    create_vms
    deploy
    run_monitor_tool
    ;;
  demo)
    failover_demo 
    cpu_load_demo
    ;;
  teardown)
    teardown
    ;;
  *)
    echo "Usage: $0 {setup|demo|teardown}" >&2
    exit 1
    ;;
esac
