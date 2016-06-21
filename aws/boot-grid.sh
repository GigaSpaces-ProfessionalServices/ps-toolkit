#!/bin/bash
set -o errexit

stack_name="xap-grid"
template_uri="file://resources/boot-grid.template"

# required parameters
project_dir=

# optional parameters
mgt_node_type=
mgt_node_size=

compute_node_type=
compute_node_size=
compute_node_count=

lookup_groups=
lookup_locators=

create_vms() {
   local create_stack_cmd="aws cloudformation create-stack --stack-name ${stack_name} \
      --template-body ${template_uri} --query 'StackId' --output text"

   local parameters=
   if [[ $lookup_groups ]]; then
      parameters+=" ParameterKey=LookupGroups,ParameterValue=$lookup_groups"
   fi

   if [[ $mgt_node_type ]]; then
      parameters+=" ParameterKey=MgtNodeInstanceType,ParameterValue=$mgt_node_type"
   fi

   if [[ $mgt_node_size ]]; then
      parameters+=" ParameterKey=MgtNodeSize,ParameterValue=$mgt_node_size"
   fi

   if [[ $compute_node_type ]]; then
      parameters+=" ParameterKey=ComputeNodeInstanceType,ParameterValue=$compute_node_type"
   fi

   if [[ $compute_node_size ]]; then
      parameters+=" ParameterKey=ComputeNodeSize,ParameterValue=$compute_node_size"
   fi

   if [[ $compute_node_count ]]; then
      parameters+=" ParameterKey=ComputeNodesCount,ParameterValue=$compute_node_count"
   fi

   if [[ $parameters ]]; then
      create_stack_cmd+=" --parameters$parameters"
   fi

   local stack_id
   if ! stack_id=$(eval $create_stack_cmd); then
     exit $?
   fi
   echo "Creating stack ${stack_id}..."

   aws cloudformation wait stack-create-complete --stack-name ${stack_name}

   lookup_locators=$(aws cloudformation describe-stacks --stack-name ${stack_name} \
      --query 'Stacks[*].Outputs[?OutputKey==`MgtPrivateIP`].OutputValue[]' --output text)
}
deploy() {
   cd ${project_dir}
   mvn clean package

   local deploy_cmd="mvn os:deploy -Dlocators=$lookup_locators"
   if [[ $lookup_groups ]]; then
      deploy_cmd+=" -Dgroups=$lookup_groups"
   fi
   $deploy_cmd
}
show_usage() {
   echo "Usage: $0 path-to-project-dir"
   echo "              -s  | --stack_name        | stack name"
   echo "              -t  | --template_uri      | template uri"
   echo "              -mt | --mgt_node_type     | EC2 instance type of VM with global GSA"
   echo "              -ms | --mgt_node_size     | size of EBS volume in GiB"
   echo "              -ct | --compute_node_type | EC2 instance type of VM with GSC"
   echo "              -cs | --compute_node_size | size of EBS volume in GiB"
   echo "              --count                   | count of compute nodes"
   echo "              -g  | --groups            | lookup groups"
   echo "              --help                    | usage"
}
parse_input() {
   if [[ "$#" -eq 0 ]] ; then
      show_usage; exit 1
   fi
   project_dir="$1"
   shift

   if [[ ! -e "$project_dir" ]]; then
      echo "${project_dir} does not exist"; exit 1 
   fi

   while [[ -n $1 ]]
   do
      case $1 in
      "-g" | "--groups")
          shift
          lookup_groups="$1"
          ;;
      "-t" | "--template_uri")
          shift
          template_uri="$1"
          ;;
      "-s" | "--stack_name")
          shift
          stack_name="$1"
          ;;
      "-mt" | "--mgt_node_type")
          shift
          mgt_node_type="$1"
          ;;
      "-ms" | "--mgt_node_size")
          shift
          mgt_node_size="$1"
          ;;
      "-ct" | "--compute_node_type")
          shift
          compute_node_type="$1"
          ;;
      "-cs" | "--compute_node_size")
          shift
          compute_node_size="$1"
          ;;
      "--count")
          shift
          compute_node_count="$1"
          ;;
      "--help")
          show_usage; exit 0
          ;;
     esac 
     shift
   done
}
main() {
   parse_input "$@"
   create_vms
   deploy
}
main "$@"
