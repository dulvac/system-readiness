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
package org.apache.sling.systemreadiness.core.impl.servlet;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.sling.systemreadiness.core.CheckStatus;
import org.apache.sling.systemreadiness.core.Status.State;
import org.apache.sling.systemreadiness.core.SystemStatus;

public class StatusWriterJson {

    private PrintWriter writer;

    public StatusWriterJson(PrintWriter writer) {
        this.writer = writer;
    }
    
    public void write(SystemStatus systemState) {
        writer.println("{");
        writer.println(String.format("  \"systemStatus\": \"%s\", ", systemState.getState().name()));
        writer.println("  \"checks\": [");
        for (CheckStatus checkStatus : systemState.getCheckStates()) {
            write(checkStatus);
        }
        writer.println("  ]");
        writer.println("}");
    }

    private void write(CheckStatus status) {
        writer.println(String.format(
                "    { \"check\": \"%s\", \"status\": \"%s\", \"details\": \"%s\" }, ", 
                status.getCheckName(),
                status.getStatus().getState().name(), 
                status.getStatus().getDetails()));
    }

}
