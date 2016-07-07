#!/bin/bash
set -o errexit

show_usage() {
    echo ""
    echo "Creates output with XAP grid information, that is up and running on specific AWS stack of VMs"
    echo 
    echo "Usage: $0 [--help] <stack-name>"
    echo
    echo "Optional parameters:"
    echo "  -d      The directory where the output file will be created"
    echo "  -p      The output file name prefix"
    echo 
}

parse_input() {
    if [[ $# -eq 1 && $1 == '--help' ]]; then
        show_usage; exit 0
    fi
    
    if [[ $# -eq 0 ]]; then
        echo "Invalid arguments encountered" >&2
        show_usage; exit 2
    fi

    stack_name=$1
    shift
    
    while [[ $# > 0 ]]; do
        case $1 in
        '-d')
            dest_dir="$2"
            if [[ ! -d "${dest_dir}" ]]; then
              echo "${dest_dir} does not exist" >&2
              exit 2
            fi
            shift 2 ;;
        '-p')
            file_name_prefix="$2"
            shift 2 ;;
        *)
            echo "Unknown option encountered: $1" >&2
            show_usage; exit 2
        esac
    done
    
    if [[ ! ${file_name_prefix} ]]; then
        file_name_prefix="grid"
    fi
    
    if [[ ! ${dest_dir} ]]; then
        dest_dir="./logs"
    fi
}

describe_stack() {
    readonly compute_group_name="ComputeNodesGroup"
    
    local instance_ids=()
    private_ips=()
    lookup_locators=()
  
    readonly output=$(aws ec2 describe-instances --filters Name=tag:aws:cloudformation:stack-name,Values=$stack_name \
       --query 'Reservations[*].Instances[*].[Tags[?Key==`aws:cloudformation:logical-id`] | [0].Value,InstanceId,PrivateIpAddress,State.Name]' --output text | grep running)
    
    if [[ -z "${output}" ]]; then
        echo "Invalid stack name ${stack_name}" >&2
        exit 2
    fi
       
    IFS=$'\n'
    for row in ${output[*]}
    do
        IFS=$'\t' read -r -a row_arr <<< "${row[i]}"
        instance_ids+=(${row_arr[1]})
        private_ips+=(${row_arr[2]})
       
        if [[ "${row_arr[0]}" != "$compute_group_name" ]]; then
           lookup_locators+=(${row_arr[2]})
        fi
    done
     
    IFS=','; echo "instance_ids=${instance_ids[*]}" | tee -a $1
}

describe_grid() {
    readonly grid_inspector_dir=tools/grid-inspector
   
    java -cp $grid_inspector_dir/lib/*:$grid_inspector_dir/grid-inspector.jar com.gigaspaces.gigapro.GridInspector ${#private_ips[@]} $(IFS=',';echo "${lookup_locators[*]}") | tee -a $1
}

find_webui() {
    local webui_urls=()
    for host in ${lookup_locators[*]}
    do
        readonly port=$(ssh ${host} ps aux | grep process.marker=webui-marker | sed -r 's/.*org.openspaces.launcher.port=([^ ]+).*/\1/')
        if [[ ${port} ]]; then
            webui_urls+=("http://${host}:${port}")
        fi
    done
    
    FS=','; echo "webui_urls=${webui_urls[*]}" | tee -a $1
}

main() {
    parse_input "$@"
    
    local output_file=${dest_dir}/${file_name_prefix}-output-$(date +%Y-%m-%d~%H.%M.%S)
    
    describe_stack $output_file
    find_webui $output_file
    describe_grid $output_file
}

main "$@"
