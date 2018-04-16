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

import java.util.Collections;
import java.util.List;
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
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Component(
        name = "ComponentsCheck",
        configurationPolicy = ConfigurationPolicy.REQUIRE
)
@Designate(ocd=ComponentsCheck.Config.class)
public class ComponentsCheck implements SystemReadinessCheck {

    @ObjectClassDefinition(
            name="DS Components System Readiness Check",
            description="System readiness check that checks a list of DS components"
                + "and provides root cause analysis in case of errors"
    )
    public @interface Config {

        @AttributeDefinition(name = "Components list", description = "The components that need to come up before this check reports GREEN")
        String components_list();

    }

    private List<String> componentsList;
    
    DSRootCause analyzer;

    @Reference
    ServiceComponentRuntime scr;

    @Activate
    public void activate(final BundleContext ctx, final Config config) throws InterruptedException {
        this.analyzer = new DSRootCause(scr);
        componentsList = StringPlus.normalize(config.components_list());
    }

    @Deactivate
    protected void deactivate() {
    }


    @Override
    public String getName() {
        return "Components Check";
    }

    @Override
    public Status getStatus() {
        final Status.State state = State.GREEN;
        return new Status(state, getDetails());
    }

    private String getDetails() {
        List<String> missing = Collections.emptyList();
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
    }

}
