Over the last year, we've evolved a design for customer XAP upgrades where the XAP installation directory ($XAP_HOME) sits in a BASE_DIR that can contain the current and any future XAP distributions.

Dixson and I finally got it well-designed. Reference scripts are attached.

### Benefits

#### Three step upgrades (assuming no configuration changes in the product, which still must be dealt with on a case by case basis):

1. Unzip into BASE_DIR
2. Copy in xap-license.txt
3. `cd $XAP_HOME ; mv config config.factory ; ln -s ../config . ; mv logs logs.factory ; ln -s ../logs .` # all configuration must be moved to  BASE_DIR at time of upgrade

### Environment portability (ops teams frequently copy the previous teams' installation directory to a new environment or set of environments belonging to the 'new' team as XAP is expanded in the account)

### One stop shopping (all configuration is in $XAP_HOME/scripts/setenv-overrides.sh and $XAP_HOME/scripts/gs-agent.sh)

### Limitations

1. We have not added grafana montoring to this yet
2.Upgrades still require careful analysis for things like gs.properties, config/log/xap_logging_ext.properties and many others)
3. Reference implementations not provided for starting WEBUI, fat client UI, or UGM. 

### Detailed description/upgrade algorithm

```bash
% mkdir $BASE_DIR # conventionally /opt/xap
% cd $BASE_DIR
% mkdir deploy work security scripts
# unzip new XAP version (upgrade to)
% cd [new xap location // directory created by unzip] 
% cp [license file loc] .
% mv config config.factory
% mv logs logs.factory
% ln -s ../logs .
% cp -r config ..
% ln -s ../config .
% cd $BASE_DIR/scripts
# copy reference scripts to $BASE_DIR/scripts
% vi setenv-overrides.sh
# change LOOKUP settings
% cd $BASE_DIR
% ln -s $XAP_HOME current
% ./start-xap.sh
# debug...```
