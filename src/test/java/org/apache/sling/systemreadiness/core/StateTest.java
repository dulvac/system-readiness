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

import static org.apache.sling.systemreadiness.core.Status.State.GREEN;
import static org.apache.sling.systemreadiness.core.Status.State.RED;
import static org.apache.sling.systemreadiness.core.Status.State.YELLOW;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.apache.sling.systemreadiness.core.Status.State;
import org.junit.Test;

public class StateTest {
    
    @Test
    public void testCompare() {
        assertThat(worstOf(GREEN, YELLOW), equalTo(YELLOW));
        assertThat(worstOf(GREEN, YELLOW, RED), equalTo(RED));
    }

    private State worstOf(State...states) {
        return State.worstOf(Arrays.asList(states).stream());
    }
}
