package org.apache.sling.systemreadiness.core.osgi.examples;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

@Component(
        service = CompWithMissingConfig.class,
        name="CompWithMissingConfig",
        configurationPolicy=ConfigurationPolicy.REQUIRE
        )
public class CompWithMissingConfig {

}
