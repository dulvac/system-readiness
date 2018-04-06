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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.sling.systemreadiness.core.SystemReadinessCheck;
import org.apache.sling.systemreadiness.core.SystemReadinessMonitor;
import org.apache.sling.systemreadiness.core.SystemReady;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
        name = "SystemReadinessMonitor"
)
public class SystemReadinessMonitorImpl implements SystemReadinessMonitor {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Reference(policyOption = ReferencePolicyOption.GREEDY)
    private List<SystemReadinessCheck> checks;

    private AtomicBoolean ready;

    private BundleContext context;

    private ServiceRegistration<SystemReady> sreg;

    private ScheduledExecutorService executor;

    @Activate
    public void activate(BundleContext context) {
        this.context = context;
        this.ready = new AtomicBoolean();
        log.info("Activated");
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::check, 0, 5, TimeUnit.SECONDS);
    }
    
    @Deactivate
    public void deactivate() {
        executor.shutdown();
    }
    
    private void check() {
        boolean allReady = checks.stream().allMatch(check -> check.isReady());
        boolean prevAllReady = this.ready.getAndSet(allReady);
        if (prevAllReady == allReady) {
            return;
        }
        if (allReady) {
            SystemReady readyService = new SystemReady() {};
            sreg = context.registerService(SystemReady.class, readyService, null);
        } else {
            sreg.unregister();
        }
    }

    @Override
    public boolean isReady() {
        return this.ready.get();
    }

}
