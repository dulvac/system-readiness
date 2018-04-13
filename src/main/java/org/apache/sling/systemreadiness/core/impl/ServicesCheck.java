/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.systemreadiness.core.impl;

import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.sling.systemreadiness.core.Status;
import org.apache.sling.systemreadiness.core.Status.State;
import org.apache.sling.systemreadiness.core.SystemReadinessCheck;
import org.apache.sling.systemreadiness.rootcause.DSComp;
import org.apache.sling.systemreadiness.rootcause.DSRootCause;
import org.apache.sling.systemreadiness.rootcause.RootCausePrinter;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Component(
        name = "ServicesCheck",
        configurationPolicy = ConfigurationPolicy.REQUIRE
)
@Designate(ocd=ServicesCheck.Config.class)
public class ServicesCheck implements SystemReadinessCheck {

    @ObjectClassDefinition(
            name="Services Registered System Readiness Check",
            description="System readiness check that waits for a list of services to be registered"
    )
    public @interface Config {

        @AttributeDefinition(name = "Services list", description = "The services that need to be registered for the check to pass")
        String[] services_list() default {};

    }

    private Map<String, Tracker> trackers;
    
    /**
     * Will only be preset if DS Runtime is available
     */
    @Reference(cardinality=ReferenceCardinality.OPTIONAL)
    DSRootCause analyzer;

    @Activate
    public void activate(final BundleContext ctx, final Config config) throws InterruptedException {
        trackers = new HashMap<>();
        for (String serviceName : config.services_list()) {
            Tracker tracker = new Tracker(ctx, serviceName);
            trackers.put(serviceName, tracker); 
        }
    }

    @Deactivate
    protected void deactivate() {
        trackers.values().stream().forEach(tracker -> tracker.close());
        trackers.clear();
    }


    @Override
    public String getName() {
        return "Services Check";
    }

    @Override
    public Status getStatus() {
        boolean allPresent = trackers.values().stream().allMatch(tracker -> tracker.present());
        // TODO: RED on timeouts
        final Status.State state = allPresent ? State.GREEN : State.YELLOW;
        return new Status(state, getDetails()); // TODO: out of sync? do we care?
    }

    private String getDetails() {
        List<String> missing = getMissing();
        if (analyzer != null) {
            StringBuilder missingSt = new StringBuilder();
            RootCausePrinter printer = new RootCausePrinter(st -> missingSt.append(st + "\n"));
            for (String iface : missing) {
                Optional<DSComp> rootCause = analyzer.getRootCause(iface);
                if (rootCause.isPresent()) {
                    printer.print(rootCause.get());
                } else {
                    missingSt.append("Missing service without matching DS component: " + iface);
                }
            }
            return missingSt.toString();
        } else {
            String missingSt = String.join(", ", missing);
            return missing.size() > 0 
                ? "Missing services : " + missingSt 
                : "";
        }
    }

    private List<String> getMissing() {
        List<String> missing = trackers.entrySet().stream()
                .filter(entry -> !entry.getValue().present())
                .map(entry -> entry.getKey())
                .collect(toList());
        return missing;
    }

}
