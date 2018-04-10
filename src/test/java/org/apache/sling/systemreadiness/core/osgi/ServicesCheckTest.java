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
package org.apache.sling.systemreadiness.core.osgi;

import static org.ops4j.pax.exam.cm.ConfigurationAdminOptions.newConfiguration;

import javax.inject.Inject;

import org.apache.sling.systemreadiness.core.CheckStatus;
import org.apache.sling.systemreadiness.core.CheckStatus.State;
import org.apache.sling.systemreadiness.core.SystemReadinessCheck;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.util.Filter;

@RunWith(PaxExam.class)
public class ServicesCheckTest extends BaseTest {

    @Inject
    @Filter("(component.name=ServicesCheck)")
    SystemReadinessCheck check;

    @Configuration
    public Option[] configuration() {
        return new Option[] {
                baseConfiguration(),
                newConfiguration("ServicesCheck")
                        .put("services.list", Runnable.class.getName())
                        .asOption()
        };
    }

    @Test
    public void test() {
        CheckStatus status = check.getStatus();
        Assert.assertEquals(State.YELLOW, status.getState());
        Assert.assertEquals("Missing service without matching component: java.lang.Runnable", status.getDetails());
        context.registerService(Runnable.class, () -> {}, null);
        CheckStatus status2 = check.getStatus();
        Assert.assertEquals(State.GREEN, status2.getState());
        Assert.assertEquals("", status2.getDetails());
    }
}
