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

import static org.apache.sling.systemreadiness.core.Status.State.GREEN;
import static org.apache.sling.systemreadiness.core.Status.State.RED;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.sling.systemreadiness.core.CheckStatus;
import org.apache.sling.systemreadiness.core.Status;
import org.apache.sling.systemreadiness.core.Status.State;
import org.apache.sling.systemreadiness.core.SystemReadinessCheck;
import org.apache.sling.systemreadiness.core.SystemReadinessMonitor;
import org.apache.sling.systemreadiness.core.SystemReady;
import org.apache.sling.systemreadiness.core.SystemStatus;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
        name = "SystemReadinessMonitor"
)
@Designate(ocd = SystemReadinessMonitorImpl.Config.class)
public class SystemReadinessMonitorImpl implements SystemReadinessMonitor {

    @ObjectClassDefinition(
            name = "System Readiness Monitor",
            description = "System readiness monitor for System Readiness Checks"
    )
    public @interface Config {

        @AttributeDefinition(name = "Poll interval",
                description = "Number of milliseconds between subsequents updates of all the checks")
        long poll_interval() default 5000;

    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(policyOption = ReferencePolicyOption.GREEDY, policy = ReferencePolicy.DYNAMIC)
    private volatile List<SystemReadinessCheck> checks;

    private BundleContext context;

    private ServiceRegistration<SystemReady> sreg;

    private ScheduledExecutorService executor;
    
    private AtomicReference<SystemStatus> systemState;


    @Activate
    public void activate(BundleContext context, final Config config) {
        this.context = context;
        this.systemState = new AtomicReference<>(new SystemStatus(State.YELLOW, Collections.emptyList()));
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.executor.scheduleAtFixedRate(this::check, 0, config.poll_interval(), TimeUnit.MILLISECONDS);
        log.info("Activated");
    }

    @Deactivate
    public void deactivate() {
        executor.shutdown();
    }

    @Override
    /**
     * Returns whether the system is ready or not
     */
    public boolean isReady() {
        return systemState.get().getState() == GREEN;
    }

    @Override
    /**
     * Returns a map of the statuses of all the checks
     */
    public SystemStatus getStatus() {
        return systemState.get();
    }

    private void check() {
        Status.State prevState = systemState.get().getState();
        List<CheckStatus> statuses = evaluateAllChecks();
        Status.State currState = State.worstOf(statuses.stream().map(status -> status.getStatus().getState()));
        this.systemState.set(new SystemStatus(currState, statuses));
        if (currState != prevState) {
            manageMarkerService(currState);
        }
    }

    private List<CheckStatus> evaluateAllChecks() {
        return checks.stream()
                .map(SystemReadinessMonitorImpl::getStatus)
                .sorted(Comparator.comparing(CheckStatus::getCheckName))
                .collect(Collectors.toList());
    }
    
    private void manageMarkerService(Status.State currState) {
        if (currState == GREEN) {
            SystemReady readyService = new SystemReady() {
            };
            sreg = context.registerService(SystemReady.class, readyService, null);
        } else {
            sreg.unregister();
        }
    }

    private static final CheckStatus getStatus(SystemReadinessCheck c) {
        try {
            return new CheckStatus(c.getName(), c.getStatus());
        } catch (Throwable e) {
            return new CheckStatus(c.getClass().getName(), new Status(RED, e.getMessage()));
        }
    }

}
