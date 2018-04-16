# Reference Documentation

This project provides a framework to configure and create so called _system readiness checks_ and report the _readiness_ of an application on top of an OSGi system.

## Requirements:

* Configuration Admin Service
* Service Component Runtime

See below for a hands on example in Apache Karaf.

## Services Check

Readiness check that is shipped in the core bundle and checks for the presence of listed services by interface name or filter.

Mandatory configuration with pid: `ServicesCheck`

* `services.list=<List of service interfaces or filters to check>`

The check reports GREEN when all services are currently present and YELLOW if at least one service is missing.

In the details the check reports all missing services. If a service is backed by a DS component then automatically a root cause analysis is executed. If such a service is missing then unresolved references are shown in a tree with detailed information about each component. At the leafs of the tree the root causes can be found.

## Providing additional custom checks

Implement the org.apache.sling.systemreadiness.core.SystemReadinessCheck interface and register
your instance as a service. The SystemReadinessMonitor will pick up your service automatically.

Your service should avoid references to other services that come up late, as a late appearing check could
make the aggregated state oscilate during startup. One option to avoid this is to refer to a late service using an optional dependency and return a YELLOW state until it is up.

## System Readiness Monitor service

The service org.apache.sling.systemreadiness.core.SystemReadinessMonitor tracks all SystemReadinessCheck services and periodically checks them. It creates an aggregated status and detailed report of the status of the system.

This report can be queried by calling the service. Additionally the system readiness servlet can provide this status over http.

For an example see the [test case](../src/test/java/org/apache/sling/systemreadiness/core/osgi/SystemReadinessMonitorTest.java).

## Readiness servlet

The Readiness servlet provides the aggregated state of the system over http in json format.
It is registered on the path `/system/console/ready`.

This is an example of a ready system with just the services check.
```
{
  "systemStatus": "GREEN", 
  "checks": [
    { "check": "Services Check", "status": "GREEN", "details": "" }, 
  ]
}
```

## Root cause command

For quickly checking for a root cause of a problem with a declarative services component there is also a handy command.

`rootcause <ds-compoment-name>`

It prints the top level status of a DS component as well as the tree of DS unsatisfied components it depends on together with their status.

This is a sample output from the DSRootCause tests. It shows the cause of a component that depends on some other components. The root cause of CompWithMissingRef2 not being satisfied is that CompWithMissingConfig is missing its mandatory config.

```
Component CompWithMissingRef2 unsatisfied references
  ref other interface org.apache.sling.systemreadiness.core.osgi.examples.CompWithMissingRef 
    Component CompWithMissingRef unsatisfied references
      ref other interface org.apache.sling.systemreadiness.core.osgi.examples.CompWithMissingConfig 
        Component CompWithMissingConfig missing config on pid [CompWithMissingConfig]
```

## Example of using the system readiness service framework in Apache Karaf

Download, install and run Apache Karaf 4.1.x. Inside the karaf shell execute this:

```
feature:install scr http-whiteboard
config:property-set --pid ServicesCheck services.list org.osgi.service.log.LogService
install -s mvn:org.apache/org.apache.sling.systemreadiness/0.1.0-SNAPSHOT
```

Point your browser to http://localhost:8181/system/console/ready .

Check the status of a DS component:

```
rootcause SystemReadinessMonitor
 Component SystemReadinessMonitor statisfied
```

Try this with some of your own components.
