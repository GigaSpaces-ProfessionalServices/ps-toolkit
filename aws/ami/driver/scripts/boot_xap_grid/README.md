> The patterns and source code available in this repository are provided as is. While most of them are used in real life use cases and productions environments, and GigaSpaces is making its best effort to keep them up to date, they should not be considered as fully productized artifacts, and you should test them in your own environment before using them.

The Apache v2 license applies. Here's [the fine print](../license.txt).

The contents of this directory perform the hard work of starting VMs and deploying a properly configured XAP PU or PUs to satisfy a given topology.

Since much XAP configuration resides inside of a zipped up jar archive, customize_topology.sh builds PUs using the [XAP maven plugin](http://docs.gigaspaces.com/xap110/installation-maven.html#using-available-project-templates) before replacing some of the configuration with topology-appropriate config.

Each script should provide a description of its behavior in its help output: `./script.sh --help`
