The contents of this directory perform the hard work of starting VMs and deploying a properly configured XAP PU or PUs to satisfy a given topology.

Since much XAP configuration resides inside of a zipped up jar archive, the xap_topology_customize.sh script builds PUs using the [XAP maven plugin](http://docs.gigaspaces.com/xap110/installation-maven.html#using-available-project-templates) before replacing some of the configuration with topology-appropriate config.

Each script should provide a description of its behavior in its help output: `./script.sh --help`