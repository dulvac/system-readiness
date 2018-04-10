package org.apache.sling.systemreadiness.core.osgi.examples;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
        name = "CompWithMissingRef2",
        service = CompWithMissingRef2.class
        )
public class CompWithMissingRef2 {
    @Reference
    CompWithMissingRef other;
}
