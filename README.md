# OSGi system readiness check framework

In OSGi there is always the question when a system is fully operational after startup. This project provides a framework to configure and create so called system checks and signal the readiness of an OSGi based system. In addition to the framework we also provide some generic checks that give a solid basis like a a check for the startup of bundles to be finished as well as certain OSGi services being present as well as root cause analysis in case of error states. Custom checks can be created to provide in depth checks for your own functionality.

## Error reporting and root cause analysis

When a system fails to become ready after a certain time the follow up question is : "What is wrong?". So the framework also provides a way for each system check to report back information about errors.

One typical error is that a DS component does not come up because some mandatory dependency is not present. This dependency can be a configuration as well as a service reference. In many cases a trivial error like a missing configuration for a low level component can make a lot of other components to also not being ready. The root cause analysis allows to traverse the trees of components to narrow the reason for the system not being ready to the minimal number of errors that cause the failure state.

## Use cases

Typical use cases for the system readiness check ordered by the phase of the development cycle.

Development
* Coarse check of a pax exam based setup before letting the actual junit tests work on the system
* Determining the root cause why a pax exam based setup is not starting

QA
* Starting system tests as soon as a system is ready for the tests to proceed
* Determining the root cause why an OSGi based system does not start up

Deployment / Upgrades
* Signaling when a deployment or upgrade step finished and the next step can begin
* Signaling when a system is ready after an upgrade and can receive traffic in case of blue / green deployments

Production
* Cyclic checks if a system is still healthy and signaling of this state to monitoring systems

## Added value when using the system readiness framework

* Increased deployment resilience as deployments are checked before being released to users
* Faster deployments / upgrades by replacing fixed waits with readiness based waits
* Lower bug tracking efforts in tests as root causes are easier to find

