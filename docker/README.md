## XAP Dockerfile


This repository contains **Dockerfile** of [GigaSpaces XAP](http://www.gigaspaces.com/xap-real-time-transaction-processing/overview)


### Installation

1. Install [Docker](https://www.docker.com/).

2. Clone [ps-toolkit](https://github.com/GigaSpaces-ProfessionalServices/ps-toolkit.git)

3. cd ps-toolkit/docker 

4. Copy your valid xap-license.txt to ps-toolkit/docker 

5. Build an image from Dockerfile: `docker build -t gigaspaces/xap:12.0.1 .`


### Usage

#### Run XAP management node

    docker run --name xap-mgt-node -d -P gigaspaces/xap:12.0.1 gsa.global.lus 0 gsa.lus 1 gsa.global.gsm 0 gsa.gsm 1 gsa.gsc 0

#### Run XAP compute node

    docker run --name xap-node -d -P -e XAP_LOOKUP_LOCATORS=<LOOKUP_LOCATOR> gigaspaces/xap:12.0.1 gsa.global.lus 0 gsa.lus 0 gsa.global.gsm 0 gsa.gsm 0 gsa.gsc 1

[XAP_LOOKUP_LOCATORS](http://docs.gigaspaces.com/xap120adm/network-unicast-discovery.html) value should be set to HOST:4174. It can be found using the following command:

    docker inspect --format '{{ .NetworkSettings.IPAddress }}' xap-mgt-node
 
#### Run gs-webui
    docker run --name gs-webui -d -p 8099:8099 gigaspaces/xap:12.0.1 ./bin/gs-webui.sh
    






