package org.apache.sling.systemreadiness.core.osgi.examples;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service=CompWithCyclicRef.class)
public class CompWithCyclicRef {
    @Reference
    CompWithCyclicRef ref;
}
