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

import static org.apache.sling.systemreadiness.core.Status.State.GREEN;
import static org.apache.sling.systemreadiness.core.Status.State.RED;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.cm.ConfigurationAdminOptions.newConfiguration;

import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.sling.systemreadiness.core.CheckStatus;
import org.apache.sling.systemreadiness.core.SystemReadinessCheck;
import org.apache.sling.systemreadiness.core.SystemReadinessMonitor;
import org.apache.sling.systemreadiness.core.osgi.examples.TestSystemReadinessCheck;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;

@RunWith(PaxExam.class)
public class SystemReadinessMonitorTest extends BaseTest {


    @Inject
    SystemReadinessMonitor monitor;

    private final ConditionFactory wait = await().atMost(1000, TimeUnit.MILLISECONDS);

    @Configuration
    public Option[] configuration() throws MalformedURLException {
        return new Option[] {
                baseConfiguration(),
                newConfiguration("SystemReadinessMonitor")
                        .put("frequency", 50)
                        .asOption()
        };
    }

    @Test
    public void test() throws InterruptedException {
        Awaitility.setDefaultPollDelay(0, TimeUnit.MILLISECONDS);
        assertNumChecks(0);
        wait.until(monitor::isReady, is(true));

        TestSystemReadinessCheck check = new TestSystemReadinessCheck();
        context.registerService(SystemReadinessCheck.class, check, null);
        assertNumChecks(1);
        wait.until(monitor::isReady, is(false));

        // make the status green
        check.setInternalState(GREEN);
        wait.until(monitor::isReady, is(true));

        // make the status fail and check that the monitor handles that
        check.exception();
        wait.until(monitor::isReady, is(false));
        assertNumChecks(1);
        final CheckStatus status = monitor.getStatus().getCheckStates().iterator().next();
        assertThat(status.getCheckName(), is(check.getClass().getName()));
        assertThat(status.getStatus().getState(), is(RED));
        assertThat(status.getStatus().getDetails(), containsString("Failure"));

        check.setInternalState(RED);
        assertNumChecks(1);
        wait.until(monitor::isReady, is(false));


        // register a second check
        TestSystemReadinessCheck check2 = new TestSystemReadinessCheck();
        context.registerService(SystemReadinessCheck.class, check2, null);
        assertNumChecks(2);
        wait.until(monitor::isReady, is(false));

        check2.setInternalState(GREEN);
        wait.until(monitor::isReady, is(false));

        check.setInternalState(GREEN);
        wait.until(monitor::isReady, is(true));

    }

    private void assertNumChecks(int expectedNum) {
        wait.until(() -> monitor.getStatus().getCheckStates().size(), is(expectedNum));
    }
}
