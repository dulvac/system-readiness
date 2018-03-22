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

import org.apache.sling.systemreadiness.core.OsgiInstallerCheck;
import org.apache.sling.systemreadiness.core.SystemReadinessCheck;
import org.osgi.framework.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 *
 */

@Component(
        name = "OSGI Installation System Readiness Check",
        service = {OsgiInstallerCheck.class, SystemReadinessCheck.class},
        configurationPid = "org.apache.sling.systemreadiness.core.impl.OsgiInstallerCheckImpl",
        immediate = true
)
public class OsgiInstallerCheckImpl implements OsgiInstallerCheck, FrameworkListener {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private BundleContext bundleContext;

    private int count = 0;
    private String status = "Starting";
    private boolean ready = false;


    @Activate
    protected void activate(final BundleContext ctx, final Map<String, Object> properties) throws InterruptedException {
        this.bundleContext = ctx;
        this.bundleContext.addFrameworkListener(this);

        log.info("Activated");
    }

    @Deactivate
    protected void deactivate() throws InterruptedException {
        this.bundleContext = null;
    }

    /**
     *
     */
    @Override
    public boolean isReady() {
        return this.ready;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void frameworkEvent(FrameworkEvent event) {
        if (event.getType() == FrameworkEvent.STARTLEVEL_CHANGED) {
            this.count ++;
            this.ready = false;
            this.status = "Received " + count + " startlevel changes so far";
        } else if (event.getType() == FrameworkEvent.STARTED) {
            this.status = "Osgi installer finished";
            this.ready = true;
        }
    }
}
