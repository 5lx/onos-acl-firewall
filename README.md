# Test Environment
## Prerequisites
* Mininet (and bridge-utils):
```bash
sudo apt-get update
sudo apt-get install mininet bridge-utils
```
* Java 8:
```bash
sudo apt-get install software-properties-common -y
sudo add-apt-repository ppa:webupd8team/java -y
sudo apt-get update
echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | sudo debconf-set-selections
sudo apt-get install oracle-java8-installer oracle-java8-set-default -y
```
* Git:
```bash
sudo apt-get install git
sudo apt-get install git-review
```

## Build ONOS 1.8.4
Under home dir, run:
```bash
git clone https://github.com/opennetworkinglab/onos
cd onos
git checkout tags/1.8.4 -b 1.8.4
tools/build/onos-buck build onos
```
## Running ONOS with onos.py
```bash
cd ~/onos/tools/dev/mininet
sudo mn --custom onos.py --controller onos,1 --topo tree,2,2
```
open: http://192.168.123.1:8181/onos/ui

# Build Application
```bash
git clone git@github.com:siriulx/onos-acl-firewall.git
cd onos-acl-firewall
mvn compile
mvn install
```
Load oar file to ONOS

open: http://192.168.123.1:8181/onos/onos-acl-firewall/sample

# Creating an application with a REST interface.
```bash
onos-create-app rest org.foo.app foo-app 1.0.0
```

# Test Commands
```bash
curl -sSL --user karaf:karaf -X POST -H 'Content-Type:application/json' http://192.168.123.1:8181/onos/onos-acl-firewall/sample -d '{"action": "deny", "srcIp":"10.0.0.1/32", "dstIp":"10.0.0.2/32"}'
curl -sSL --user karaf:karaf -X DELETE http://192.168.123.1:8181/onos/onos-acl-firewall/sample
curl -sSL --user karaf:karaf -X GET http://192.168.123.1:8181/onos/onos-acl-firewall/sample
```
