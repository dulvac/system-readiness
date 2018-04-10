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
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.sling.systemreadiness.core.CheckStatus;
import org.apache.sling.systemreadiness.core.SystemReadinessCheck;
import org.apache.sling.systemreadiness.core.SystemReadinessMonitor;
import org.apache.sling.systemreadiness.core.SystemReady;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.sling.systemreadiness.core.CheckStatus.State.RED;
import static org.apache.sling.systemreadiness.core.CheckStatus.State.YELLOW;

@Component(
        name = "SystemReadinessMonitor"
)
public class SystemReadinessMonitorImpl implements SystemReadinessMonitor {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Reference(policyOption = ReferencePolicyOption.GREEDY)
    private List<SystemReadinessCheck> checks;

    private AtomicReference<CheckStatus.State> state; // TODO: Why atomic?

    private BundleContext context;

    private ServiceRegistration<SystemReady> sreg;

    private ScheduledExecutorService executor;

    @Activate
    public void activate(BundleContext context) {
        this.context = context;
        this.state = new AtomicReference<>(YELLOW);
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::check, 0, 5, TimeUnit.SECONDS);
        log.info("Activated");
    }
    
    @Deactivate
    public void deactivate() {
        executor.shutdown();
    }
    
    private void check() {
        if (context.getBundle(0).getState() != Bundle.ACTIVE) {
            // Only do the actual checks once the framework is started
            this.state.set(YELLOW);
            return;
        }
        CheckStatus.State currState = CheckStatus.State.fromBoolean(
                checks.stream().allMatch(c -> c.getStatus().getState().isReady()));
        if (checks.stream().anyMatch(c -> c.getStatus().getState() == RED)) {
            currState = RED;
        }
        CheckStatus.State prevState = this.state.getAndSet(currState);

        if (currState == prevState) {
            return;
        }

        if (currState == RED) {
            // TODO: do we allow it to change state from red? For now, yes.
        }

        if (currState.isReady()) {
            SystemReady readyService = new SystemReady() {}; // TODO: still not convinced I like this
            sreg = context.registerService(SystemReady.class, readyService, null);
        } else {
            sreg.unregister();
        }
    }

    private String getDetails() {
        // TODO
        return "";
    }

    @Override
    public boolean isReady() {
        return state.get().isReady();
    }

    @Override
    public Map<String, CheckStatus> getStatuses() {
        return null;
        // TODO
    }
}
