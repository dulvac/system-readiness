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
package org.apache.sling.systemreadiness.core.impl.monitor;

import java.util.*;

import org.apache.sling.systemreadiness.core.SystemReadinessCheck;
import org.apache.sling.systemreadiness.core.monitor.SystemReadinessMonitor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 */

@Component(
        name = "System Readiness Check Monitor",
        service = {SystemReadinessMonitor.class},
        configurationPid = "org.apache.sling.systemreadiness.core.impl.monitor.SystemReadinessMonitorImpl",
        immediate = true
)
public class SystemReadinessMonitorImpl implements SystemReadinessMonitor {



    private final Logger log = LoggerFactory.getLogger(getClass());
    private BundleContext bundleContext;
    private ServiceTracker startupCheckTracker;



    @Activate
    protected void activate(final BundleContext ctx, final Map<String, Object> properties) throws InterruptedException {
        this.bundleContext = ctx;

        this.startupCheckTracker = new ServiceTracker(bundleContext, SystemReadinessCheck.class, null);
        this.startupCheckTracker.open();
        log.info("Activated System Readiness Monitor");
    }

    @Deactivate
    protected void deactivate() throws InterruptedException {
        this.startupCheckTracker.close();
        this.startupCheckTracker = null;
        bundleContext = null;
    }


    /**
     * TODO
     */
    @Override
    public boolean isReady() {

        // Get reference to all {{StartupCheck}} services
        final ServiceReference<SystemReadinessCheck>[] startupChecks = this.startupCheckTracker.getServiceReferences();
        if (null == startupChecks) {
            return true;
        }

        boolean allStarted = true;
        for (ServiceReference<SystemReadinessCheck> ref : startupChecks) {
            SystemReadinessCheck sc = this.bundleContext.getService(ref);
            if (!sc.isReady()) {
                log.info("Found check that reported NOT READY: {} [{}]", sc.isReady(), sc);
                allStarted = false;
                break;
            }
        }
        return allStarted;
    }

}
