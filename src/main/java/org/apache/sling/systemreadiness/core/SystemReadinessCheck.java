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
package org.apache.sling.systemreadiness.core;

import java.util.UUID;

/**
 * Readiness check services provide custom logic for signalling
 * that particular criteria are met for when an instance is considered "ready".
 *
 * Examples: An asyncronous integration with another instance or a third-party service
 *
 * {@see SystemReadinessMonitor}
 *
 */
public interface SystemReadinessCheck {

    /**
     *
     * @return the name of this check. E.g. component name
     */
    String getName();

    /**
     *
     * @return the state of the system
     */
    CheckStatus getStatus();

    /**
     * Holds a unique id and the name of a System readiness check
     */
    class Id {
        UUID id;
        String name;

        public Id(UUID id, String name) {
            this.id = id;
            this.name = name;
        }

        public Id(SystemReadinessCheck check) {
            this(UUID.randomUUID(), check.getName());
        }

        public UUID getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
