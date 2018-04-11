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
package org.apache.sling.systemreadiness.core.osgi.examples;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.sling.systemreadiness.core.CheckStatus;
import org.apache.sling.systemreadiness.core.CheckStatus.State;
import org.apache.sling.systemreadiness.core.SystemReadinessCheck;

public class TestSystemReadinessCheck implements SystemReadinessCheck {

    private State state;
    private AtomicReference<RuntimeException> ex = new AtomicReference<>(null);

    public TestSystemReadinessCheck() {
        this.state = State.YELLOW;
    }

    @Override
    public CheckStatus getStatus() {
        if (null == ex.get()) {
            return new CheckStatus(state, state.name());
        } else {
            throw ex.get();
        }
    }

    public void setState(State state) {
        this.ex.set(null);
        this.state = state;
    }

    public void exception() {
        this.ex.set(new RuntimeException("Failure"));
        this.state = State.RED;
    }

}
