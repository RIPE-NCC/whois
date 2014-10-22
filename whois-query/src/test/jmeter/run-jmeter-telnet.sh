#!/bin/bash
java -jar /export/jmeter/target/jmeter/bin/ApacheJMeter.jar -n -t /export/jmeter/src/test/jmeter/query_telnet.jmx -d /export/jmeter/target/jmeter -j /export/jmeter/target/jmeter/logs/query_telnet.jmx.log -Dsample_variables=command

