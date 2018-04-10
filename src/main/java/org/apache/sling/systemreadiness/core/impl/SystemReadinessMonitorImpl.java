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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.sling.systemreadiness.core.CheckStatus;
import org.apache.sling.systemreadiness.core.SystemReadinessCheck;
import org.apache.sling.systemreadiness.core.SystemReadinessMonitor;
import org.apache.sling.systemreadiness.core.SystemReady;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.sling.systemreadiness.core.CheckStatus.State.GREEN;
import static org.apache.sling.systemreadiness.core.CheckStatus.State.RED;
import static org.apache.sling.systemreadiness.core.CheckStatus.State.YELLOW;

@Component(
        name = "SystemReadinessMonitor"
)
@Designate(ocd=SystemReadinessMonitorImpl.Config.class)
public class SystemReadinessMonitorImpl implements SystemReadinessMonitor {

    @ObjectClassDefinition(
            name="System Readiness Monitor",
            description="System readiness monitor for System Readiness Checks"
    )
    public @interface Config {

        @AttributeDefinition(name = "Update frequency",
                description = "Number of milliseconds between subsequents updates from all the checks")
        long frequency() default 5000;

    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(policyOption = ReferencePolicyOption.GREEDY, policy = ReferencePolicy.DYNAMIC)
    private volatile List<SystemReadinessCheck> checks;

    private AtomicReference<CheckStatus.State> state;

    private Map<String, CheckStatus> statuses;

    private BundleContext context;

    private ServiceRegistration<SystemReady> sreg;

    private ScheduledExecutorService executor;


    @Activate
    public void activate(BundleContext context, final Config config) {
        this.context = context;
        this.state = new AtomicReference<>(YELLOW);
        this.statuses = Collections.emptyMap();
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::check, 0, config.frequency(), TimeUnit.MILLISECONDS);
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
        return state.get() == GREEN;
    }

    @Override
    /**
     * Returns a map of the statuses of all the checks
     */
    public Map<String, CheckStatus> getStatuses() {
        return this.statuses;
    }

    private void check() {
        try {
            if (context.getBundle(0).getState() != Bundle.ACTIVE) {
                // Only do the actual checks once the framework is started
                this.state.set(YELLOW);
                return;
            }
            CheckStatus.State currState = CheckStatus.State.fromBoolean(
                    checks.stream().allMatch(c -> this.getStatus(c).getState() == GREEN));
            if (checks.stream().anyMatch(c -> this.getStatus(c).getState() == RED)) {
                currState = RED;
            }
            CheckStatus.State prevState = this.state.getAndSet(currState);

            // get statuses
            // TODO: key
            this.statuses = checks.stream().collect(Collectors.toMap(c -> c.toString(), this::getStatus));

            if (currState == prevState) {
                return;
            }

            if (currState == RED) {
                // TODO: do we allow it to change state from red? For now, yes.
            }

            if (currState == GREEN) {
                SystemReady readyService = new SystemReady() {
                };
                sreg = context.registerService(SystemReady.class, readyService, null);
            } else {
                sreg.unregister();
            }
        } catch (Throwable e) {
            this.state.set(RED);
            log.error("failed to monitor", e);
        }
    }

    private final CheckStatus getStatus(SystemReadinessCheck c) {
        try {
            return c.getStatus();
        } catch (Exception e) {
            return new CheckStatus(RED, e.getMessage());
        }

    }
}
