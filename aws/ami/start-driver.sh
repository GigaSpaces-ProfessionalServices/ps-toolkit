#!/bin/bash
set -o nounset
set -o errexit

readonly instance_name="Driver VM"
readonly ami_id="ami-12d5117f"
readonly instance_count=1
readonly instance_type="t2.micro"
readonly key_name="fe-shared"

# 'default' security group
readonly security_group_id="sg-78022a1c"

# private subnet
readonly subnet_id="subnet-ef88edb6"
readonly root_volume_size=8
readonly root_device="/dev/sda1"

show_usage() {
    echo ""
    echo "Launches XAP driver EC2 virtual machine with name tag 'Driver VM'"
    echo ""
    echo "Usage: $0 [--help]"
    echo ""
}

parse_input() {
    if [[ $1 == '--help' ]]; then
        show_usage; exit 0
    fi

    if [[ $# -gt 0 ]]; then
        echo "Invalid arguments encountered for script $0" >&2
        show_usage; exit 2
    fi
}

start_driver() {
    readonly instance_id=$(aws ec2 run-instances --image-id $ami_id --count $instance_count \
        --instance-type $instance_type --key-name $key_name --security-group-ids $security_group_id \
        --subnet-id $subnet_id --block-device-mappings \
        "[{\"DeviceName\":\"${root_device}\",\"Ebs\":{\"VolumeSize\":${root_volume_size},\"DeleteOnTermination\":true}}]" \
        --query 'Instances[0].InstanceId' --output text)

    aws ec2 create-tags --resources ${instance_id} --tags Key=Name,Value="${instance_name}"
    echo "Driver VM has been started, instance id: ${instance_id}"
}

main() {
    parse_input "$@"
    start_driver
}

main "$@"
