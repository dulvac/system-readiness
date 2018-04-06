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

public class CheckStatus {
    public enum State { GREEN(true), YELLOW(false), RED(false);
        private boolean ready;

        State(boolean ready) {
            this.ready = ready;
        }

        public boolean isReady() {
            return this.ready;
        }

        /**
         * returns {{GREEN}} for {{true}} and {{YELLOW}} for {{false}}
         */
        public static State fromBoolean(boolean ready) {
            return (ready) ? State.GREEN : State.YELLOW;
        }
    }
    
    private State state;
    
    private String details;
    
    public CheckStatus(State state, String details) {
        this.state = state;
        this.details = details;
    }
    
    public State getState() {
        return state;
    }
    
    public String getDetails() {
        return details;
    }
}
