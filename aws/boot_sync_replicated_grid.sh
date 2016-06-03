#!/bin/bash
set -o nounset
set -o errexit

readonly stack_name="sync-replicated-grid"
readonly template_uri="file:///tmp/boot-grid.template"

aws cloudformation create-stack --stack-name ${stack_name} --template-body ${template_uri}
