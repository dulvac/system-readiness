package org.apache.sling.systemreadiness.core.osgi;

import static org.apache.sling.systemreadiness.core.CheckStatus.State.GREEN;
import static org.apache.sling.systemreadiness.core.CheckStatus.State.RED;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.cm.ConfigurationAdminOptions.newConfiguration;

import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.sling.systemreadiness.core.CheckStatus;
import org.apache.sling.systemreadiness.core.SystemReadinessCheck;
import org.apache.sling.systemreadiness.core.SystemReadinessMonitor;
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
        assertThat("Statuses should be empty", monitor.getStatuses().size(), is(0));
        wait.until(monitor::isReady, is(true));

        TestSystemReadinessCheck check = new TestSystemReadinessCheck();
        context.registerService(SystemReadinessCheck.class, check, null);
        wait.until(() -> monitor.getStatuses().size(), is(1));
        wait.until(monitor::isReady, is(false));

        check.setState(GREEN);
        wait.until(monitor::isReady, is(true));

        check.exception();
        wait.until(monitor::isReady, is(false));
        final CheckStatus status = monitor.getStatuses().values().iterator().next();
        assertThat(status.getState(), is(RED));
        assertThat(status.getDetails(), containsString("Failure"));

        check.setState(RED);
        wait.until(() -> monitor.getStatuses().size(), is(1));
        wait.until(monitor::isReady, is(false));


        TestSystemReadinessCheck check2 = new TestSystemReadinessCheck();
        context.registerService(SystemReadinessCheck.class, check2, null);
        // reference needs to be updated
        wait.until(() -> monitor.getStatuses().size(), is(2));
        wait.until(monitor::isReady, is(false));

        check2.setState(GREEN);
        wait.until(monitor::isReady, is(false));

        check.setState(GREEN);
        wait.until(monitor::isReady, is(true));

    }
}
