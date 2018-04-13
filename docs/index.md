# Reference Documentation

## Requirements:

* Configuration Admin
* Service Component Runtime

See below for a hands on example in Apache Karaf.

## Services Check

Readyness check that is shipped in the core bundle and checks for the presence of listed services by interface name or filter.

Mandatory configuration with pid: `ServicesCheck`

* `services.list=<List of service interfaces or filters to check>`

The check reports GREEN when all services are currently present and YELLOW if at least one service is missing.

In the details the check reports all missing services. If a service is backed by a DS component then automatically a root cause analysis is executed. If such a service is missing then unresolved references are shown in a tree with detailed information about each component. At the leafs of the tree the root causes can be found.

## Providing additional custom checks

Implement the org.apache.sling.systemreadiness.core.SystemReadinessCheck interface and register
your instance as a service. The SystemReadinessMonitor will pick up your service automatically.

Your service should avoid references to other services that come up late as a late appearing check could
make the aggregated state oscilate during startup. One option to avoid this is to refer to a late service using an optional dependency and return a YELLOW state until it is up.

## System Readiness Monitor service

The service org.apache.sling.systemreadiness.core.SystemReadinessMonitor tracks all SystemReadinessCheck services and periodically checks them. It creates an aggregated status and detailed report of the status of the system.

This report can be queried by calling the service. Additionally the system readiness servlet can provide this status over http.

For an example see the [test case](../src/test/java/org/apache/sling/systemreadiness/core/osgi/SystemReadinessMonitorTest.java).

## Readiness servlet



## Example of using the readyness service framework in Apache Karaf

Download, install and run Apache Karaf 4.1.x. Inside the karaf shell execute this:

```
feature:install scr http-whiteboard
config:property-set --pid ServicesCheck services.list org.osgi.service.log.LogService
install -s mvn:org.apache/org.apache.sling.systemreadiness/0.1.0-SNAPSHOT
```

Point your browser to http://localhost:8181/system/console/ready .

