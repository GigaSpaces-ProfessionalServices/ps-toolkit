#!/bin/bash
set -o nounset
set -o errexit

sudo apt-get update

wget https://grafanarel.s3.amazonaws.com/builds/grafana_3.0.4-1464167696_amd64.deb
sudo apt-get install -y adduser libfontconfig zip
rm grafana_3.0.4-1464167696_amd64.deb
sudo dpkg -i grafana_3.0.4-1464167696_amd64.deb

exit 0