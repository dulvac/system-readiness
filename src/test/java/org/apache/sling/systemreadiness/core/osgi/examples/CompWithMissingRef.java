package org.apache.sling.systemreadiness.core.osgi.examples;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "CompWithMissingRef")
public class CompWithMissingRef {
    @Reference
    CompWithMissingConfig other;

}
