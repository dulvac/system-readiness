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

import org.apache.commons.lang.StringUtils;
import org.apache.sling.systemreadiness.core.ServicesRegisteredCheck;
import org.apache.sling.systemreadiness.core.SystemReadinessCheck;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 */

@Component(
        name = "Services Registered System Readiness Check",
        service = {ServicesRegisteredCheck.class, SystemReadinessCheck.class},
        configurationPid = "org.apache.sling.systemreadiness.core.impl.ServicesRegisteredCheckImpl",
        immediate = true
)
@Designate(ocd=ServicesRegisteredCheckImpl.Config.class)
public class ServicesRegisteredCheckImpl implements ServicesRegisteredCheck {

    private List<String> servicesList;

    @ObjectClassDefinition(
            name="Services Registered System Readiness Check",
            description="System readiness check that waits for a list of services to be registered"
    )
    public @interface Config {

        @AttributeDefinition(name = "Services list", description = "The services that need to be registered")
        String[] services_list() default {};

    }
    private final Logger log = LoggerFactory.getLogger(getClass());
    private BundleContext bundleContext;

    private String status = "Not ready";
    private boolean ready = false;
    private Timer timer = new Timer();


    @Activate
    protected void activate(final BundleContext ctx, final Map<String, Object> properties, final Config config) throws InterruptedException {
        this.bundleContext = ctx;
        this.servicesList = Arrays.asList(config.services_list());

        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                List<String> missingServices = getMissingServices();
                if (null != missingServices && missingServices.size() == 0) {
                    ready = true;
                    status = "Ready";
                } else {
                    ready = false;
                    status = "Not ready. Missing services: " + missingServices.toString();
                }
            }
        };
        this.timer.schedule(task, 1000, 1000);

        log.info("Activated");
    }

    @Deactivate
    protected void deactivate() throws InterruptedException {
        this.timer.cancel();
        this.bundleContext = null;

    }

    @Override
    public boolean isReady() {
        return this.ready;
    }

    @Override
    public String getStatus() {
        return this.status;
    }


    private List<String> getMissingServices() {
        final List<String> missing = new ArrayList<String>(this.servicesList.size()) {
            @Override
            public String toString() {
                return new StringBuilder().append('[')
                        .append(StringUtils.join(this, ','))
                        .append(']')
                        .toString();
            }
        };

        for (String service : this.servicesList) {
            if (!isServiceRegistered(this.bundleContext, service)) {
                missing.add(service);
            }
        }
        return missing;
    }

    private static boolean isServiceRegistered(BundleContext ctx, String name) {
        // Get the service ref
        final ServiceReference<?> sr = ctx.getServiceReference(name);
        if (null == sr) {
            return false;
        }
        // Get the service
        final Object service = ctx.getService(sr);
        if (null == service) {
            return false;
        }
        // Release the service object reference
        ctx.ungetService(sr);

        return true;
    }
}
