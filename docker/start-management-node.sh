#!/bin/bash
set -e

readonly dir="$(cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd)"

echo $1 > $dir/xap-license.txt

docker build -t gigaspaces/xap:12.0.1 $dir

docker run --name xap-mgt-node -d -P -p 4174:4174 gigaspaces/xap:12.0.1 gsa.global.lus 0 gsa.lus 1 gsa.global.gsm 0 gsa.gsm 1 gsa.gsc 0

docker run --name gs-webui -d -p 8099:8099 gigaspaces/xap:12.0.1 ./bin/gs-webui.sh