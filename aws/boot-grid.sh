#!/bin/bash
set -o errexit

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
    echo ""
    echo "Creates a stack of EC2 virtual machines and starts XAP grid"
    echo "on these boxes using XAP Maven plugin"
    echo ""
    echo "Usage: $0 [--help] [OPTIONS]... <path-to-project-dir>"
    echo ""
    echo "Mandatory parameters:"
    echo "  -s,   --stack-name          EC2 stack name"
    echo "  -t,   --template-uri        Template URI"
    echo ""
    echo "Optional parameters:"
    echo "  -c,   --node-count          The number of compute nodes"
    echo "  -g,   --lookup-groups       Lookup groups"
    echo "  -mnt, --mgt-node-type       EC2 instance type of VM with global GSA"
    echo "  -mns, --mgt-node-size       Size of EBS volume in GiB"
    echo "  -cnt, --compute-node-type   EC2 instance type of VM with GSC"
    echo "  -cns, --compute-node-size   Size of EBS volume in GiB"
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
        '-s' | '--stack-name')
            stack_name="$2"
            shift 2 ;;
        '-t' | '--template-uri')
            template_uri="$2"
            shift 2 ;;
        '-c' | '--node-count')
            compute_node_count="$2"
            shift 2 ;;
        '-g' | '--lookup-groups')
            lookup_groups="$2"
            shift 2 ;;
        '-mnt' | '--mgt-node-type')
            mgt_node_type="$2"
            shift 2 ;;
        '-mns' | '--mgt-node-size')
            mgt_node_size="$2"
            shift 2 ;;
        '-cnt' | '--compute-node-type')
            compute_node_type="$2"
            shift 2 ;;
        '-cns' | '--compute-node-size')
            compute_node_size="$2"
            shift 2 ;;
        *)
            if [[ "$1" == "-"* ]]; then
                echo "Unknown option encountered: $1" >&2
                show_usage; exit 2
            fi

            # required parameter
            project_dir="$1"
            shift ;;
        esac
    done

    if [[ ! -d "$project_dir" ]]; then
        echo "The directory ${project_dir} does not exist"; exit 1
    fi
}

main() {
    stack_name="xap-grid"
    template_uri="file://resources/boot-grid.template"

    parse_input "$@"
    create_vms
    deploy
}

main "$@"
