#!/bin/bash
set -o errexit

show_usage() {
    echo ""
    echo "Creates a stack of EC2 virtual machines and starts XAP grid on these boxes"
    echo ""
    echo "Usage: $0 [--help] [OPTIONS]"
    echo ""
    echo "Mandatory parameters:"
    echo "  -s,   --stack-name          EC2 stack name"
    echo "  -t,   --template-uri        Template file URI"
    echo ""
    echo "Optional parameters:"
    echo "  -c,   --node-count          The number of compute nodes"
    echo "  -g,   --lookup-groups       Lookup groups"
    echo "  -i,   --image-id            Image id of existing AMI"
    echo "  -v,   --xap-version         XAP home directory name"
    echo "  -mnt, --mgt-node-type       EC2 instance type of VM with global GSA"
    echo "  -mns, --mgt-node-size       Size of EBS volume in GiB"
    echo "  -cnt, --compute-node-type   EC2 instance type of VM with GSC"
    echo "  -cns, --compute-node-size   Size of EBS volume in GiB"
    echo "  -gsc, --gsc-count           The number of GSCs per compute node"
    echo "  -dr,  --disable-rollback    Specify to disable rollback for troubleshooting stack creation"
    echo ""
}

parse_input() {
    if [[ $# -eq 1 && $1 == '--help' ]]; then
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
        '-i' | '--image-id')
            image_id="$2"
            shift 2 ;;
        '-v' | '--xap-version')
            xap_version="$2"
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
        '-gsc' | '--gsc-count')
            gsc_count="$2"
            shift 2 ;;
        '-dr' | '--disable-rollback')
            disable_rollback=true
            shift 1 ;;
        *)
            if [[ "$1" == "-"* ]]; then
                echo "Unknown option encountered: $1" >&2
                show_usage; exit 2
            fi
        esac
    done
}

create_vms() {
    local create_stack_cmd="aws cloudformation create-stack --stack-name ${stack_name} \
        --template-body ${template_uri} --query 'StackId' --output text"

    local parameters=
    if [[ $lookup_groups ]]; then
        parameters+=" ParameterKey=LookupGroups,ParameterValue=$lookup_groups"
    fi

    if [[ $image_id ]]; then
        parameters+=" ParameterKey=ImageId,ParameterValue=$image_id"
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
   
    if [[ $gsc_count ]]; then
        parameters+=" ParameterKey=GscCount,ParameterValue=$gsc_count"
    fi
 
    if [[ $xap_version ]]; then
        parameters+=" ParameterKey=XapVersion,ParameterValue=$xap_version"
    fi

    if [[ $parameters ]]; then
        create_stack_cmd+=" --parameters$parameters"
    fi

    if [[ $disable_rollback ]]; then
	    create_stack_cmd+=" --disable-rollback"
    fi 

    local stack_id
    if ! stack_id=$(eval $create_stack_cmd); then
        exit $?
    fi
    echo "Creating stack ${stack_id}..."

    aws cloudformation wait stack-create-complete --stack-name ${stack_name}

    echo "Completed."
    echo ""
    echo "lookup locators: $(aws cloudformation describe-stacks --stack-name ${stack_name} \
        --query 'Stacks[*].Outputs[?OutputKey==`MgtPrivateIP`].OutputValue[]' --output text)"
}

main() {
    stack_name="xap-grid"
    template_uri="file://cloud_formation_templates/boot-grid.template"

    parse_input "$@"
    create_vms
}

main "$@"
