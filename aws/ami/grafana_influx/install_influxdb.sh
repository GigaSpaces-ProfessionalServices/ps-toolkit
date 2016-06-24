#!/bin/bash
set -o nounset
set -o errexit

sudo apt-get update

curl -sL https://repos.influxdata.com/influxdb.key | sudo apt-key add -
source /etc/lsb-release
echo "deb https://repos.influxdata.com/${DISTRIB_ID,,} ${DISTRIB_CODENAME} stable" | sudo tee /etc/apt/sources.list.d/influxdb.list
sudo apt-get update && sudo apt-get install -y influxdb zip
sudo service influxdb start
echo "CREATE DATABASE mydb" | influx
echo "CREATE DATABASE grafana" | influx

exit 0