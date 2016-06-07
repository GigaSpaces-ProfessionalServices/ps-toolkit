#!/bin/bash
set -o errexit

stack_name="xap-grid"
template_uri="file:///tmp/boot-grid.template"
lookup_groups="gigaspaces-xap-premium-10.2.1"
lookup_locators=
project_dir=

create_vms() {
   local stack_id
   if ! stack_id=$(aws cloudformation create-stack --stack-name ${stack_name} --template-body ${template_uri} --parameters ParameterKey=LookupGroups,ParameterValue=$lookup_groups --query 'StackId' --output text); then
     exit $?
   fi
   echo "Creating stack ${stack_id}..."

   aws cloudformation wait stack-create-complete --stack-name ${stack_name}

   lookup_locators=$(aws cloudformation describe-stacks --stack-name ${stack_name} --query 'Stacks[*].Outputs[?OutputKey==`MgtPrivateIP`].OutputValue[]' --output text)
}
deploy() {
   cd ${project_dir}
   mvn clean package
   mvn os:deploy -Dgroups=$lookup_groups -Dlocators=$lookup_locators 
}
usage() { 
   echo "Usage: $0 path-to-project-dir"
   echo "              --stack_name   | stack name"
   echo "              --groups       | lookup groups"
   echo "              --template_uri | template uri"
   echo "              --help         | usage"
   exit 1
}
parse_input() {
   if [[ "$#" -eq 0 ]] ; then
      usage
   fi
   project_dir="$1"
   shift
  
   if [[ ! -e "$project_dir" ]]; then
      echo "${project_dir} does not exist"; exit 1 
   fi

   while [[ -n $1 ]]
   do
      case $1 in
      "--groups")
          shift
          lookup_groups="$1"
          ;;
      "--template_url")
          shift
          template_uri="$1"
          ;;
      "--stack_name")
          shift
          stack_name="$1"
          ;;
      "--help")
          usage
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

