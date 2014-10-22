#!/bin/bash
java -jar /export/jmeter/target/jmeter/bin/ApacheJMeter.jar -n -t /export/jmeter/src/test/jmeter/query_rest.jmx -d /export/jmeter/target/jmeter -j /export/jmeter/target/jmeter/logs/query_rest.jmx.log

