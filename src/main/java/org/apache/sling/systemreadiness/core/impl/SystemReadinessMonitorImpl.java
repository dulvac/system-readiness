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

import static org.apache.sling.systemreadiness.core.CheckStatus.State.GREEN;
import static org.apache.sling.systemreadiness.core.CheckStatus.State.RED;
import static org.apache.sling.systemreadiness.core.CheckStatus.State.YELLOW;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.sling.systemreadiness.core.*;
import org.osgi.framework.Bundle;
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
@Designate(ocd=SystemReadinessMonitorImpl.Config.class)
public class SystemReadinessMonitorImpl implements SystemReadinessMonitor {

    @ObjectClassDefinition(
            name="System Readiness Monitor",
            description="System readiness monitor for System Readiness Checks"
    )
    public @interface Config {

        @AttributeDefinition(name = "Update frequency",
                description = "Number of milliseconds between subsequents updates of all the checks")
        long frequency() default 5000;

    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(policyOption = ReferencePolicyOption.GREEDY, policy = ReferencePolicy.DYNAMIC)
    private volatile List<SystemReadinessCheck> checks;

    private AtomicReference<CheckStatus.State> state;

    private Map<SystemReadinessCheck.Id, CheckStatus> statuses;

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
    public Map<SystemReadinessCheck.Id, CheckStatus> getStatuses() {
        return this.statuses;
    }

    private void check() {
        try {

            // If there is no {{FrameworkStartCheck}}, only do the actual checks once the framework is started
            if ((checks.stream().noneMatch(c-> c.getClass().equals(FrameworkStartCheck.class)))
                && context.getBundle(0).getState() != Bundle.ACTIVE) {
                this.state.set(YELLOW);
                return;
            }

            CheckStatus.State currState = checks.stream().anyMatch(hasState(RED))
                    ? RED
                    : CheckStatus.State.fromBoolean(checks.stream().allMatch(hasState(GREEN)));
            CheckStatus.State prevState = this.state.getAndSet(currState);

            // get statuses
            this.statuses = checks.stream().collect(Collectors.toMap(SystemReadinessCheck.Id::new, SystemReadinessMonitorImpl::getStatus));

            if (currState == prevState) {
                return;
            }

            if (currState == RED) {
                // TODO: do we allow it to change state from red? For now, yes.
            }

            if (currState == GREEN) {
                SystemReady readyService = new SystemReady() {};
                sreg = context.registerService(SystemReady.class, readyService, null);
            } else {
                sreg.unregister();
            }
        } catch (Throwable e) {
            this.state.set(RED);
            log.error("Failed to monitor", e);
        }
    }

    private static final CheckStatus getStatus(SystemReadinessCheck c) {
        try {
            return c.getStatus();
        } catch (Throwable e) {
            return new CheckStatus(RED, e.getMessage());
        }

    }

    private static Predicate<SystemReadinessCheck> hasState(CheckStatus.State state) {
        return c -> getStatus(c).getState() == state;
    }


}
