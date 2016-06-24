#!/bin/bash
set -o nounset
set -o errexit

./install_influxdb.sh
./install_grafana.sh

exit 0