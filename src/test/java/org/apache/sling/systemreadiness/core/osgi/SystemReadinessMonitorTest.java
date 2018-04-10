package org.apache.sling.systemreadiness.core.osgi;

import org.apache.sling.systemreadiness.core.CheckStatus;
import org.apache.sling.systemreadiness.core.SystemReadinessCheck;
import org.apache.sling.systemreadiness.core.SystemReadinessMonitor;
import org.awaitility.core.ConditionFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;

import static org.apache.sling.systemreadiness.core.CheckStatus.State.*;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.cm.ConfigurationAdminOptions.newConfiguration;

@RunWith(PaxExam.class)
public class SystemReadinessMonitorTest {


    @Inject
    SystemReadinessMonitor monitor;

    @Inject
    BundleContext context;

    private final ConditionFactory wait = await().atMost(1000, TimeUnit.MILLISECONDS);



    @Configuration
    public Option[] configuration() throws MalformedURLException {
        return new Option[] {
                junitBundles(),
                mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.scr").version("2.0.14"),
                mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.configadmin").version("1.8.16"),
                mavenBundle().groupId("org.awaitility").artifactId("awaitility").version("3.1.0"),
                bundle("reference:file:target/classes/"),
                newConfiguration("SystemReadinessMonitor")
                        .put("frequency", 300)
                        .asOption()
        };
    }

    @Test
    public void test() throws InterruptedException {

        Assert.assertThat("Statuses should be empty", monitor.getStatuses().size(), is(0));
        wait.until(() -> monitor.isReady(), is(true));

        TestSystemReadinessCheck check = new TestSystemReadinessCheck();
        check.activate(context, null);
        context.registerService(SystemReadinessCheck.class, check, null);
        Assert.assertThat(check.getStatus().getState(), is(YELLOW));

        wait.until(() -> monitor.getStatuses().size(), is(1));
        wait.until(() -> monitor.isReady(), is(false));


        check.green();
        Assert.assertThat(check.getStatus().getState(), is(GREEN));

        wait.until(() -> monitor.getStatuses().size(), is(1));
        wait.until(() -> monitor.isReady(), is(true));


        check.exception();
        wait.until(() -> monitor.isReady(), is(false));
        wait.until(() -> monitor.getStatuses().size(), is(1));
        final CheckStatus status = monitor.getStatuses().entrySet().iterator().next().getValue();
        Assert.assertThat(status.getState(), is(RED));
        Assert.assertThat(status.getDetails(), containsString(check.getException().getMessage()));

        check.red();
        Assert.assertThat(check.getStatus().getState(), is(RED));
        wait.until(() -> monitor.getStatuses().size(), is(1));
        wait.until(() -> monitor.isReady(), is(false));


        TestSystemReadinessCheck check2 = new TestSystemReadinessCheck();
        check2.activate(context, null);
        context.registerService(SystemReadinessCheck.class, check2.yellow(), null);
        Assert.assertThat(check2.getStatus().getState(), is(YELLOW));

        // reference needs to be updated
        wait.until(() -> monitor.getStatuses().size(), is(2));
        wait.until(() -> monitor.isReady(), is(false));

        check2.green();
        wait.until(() -> monitor.getStatuses().size(), is(2));
        wait.until(() -> monitor.isReady(), is(false));

        check.green();
        wait.until(() -> monitor.getStatuses().size(), is(2));
        wait.until(() -> monitor.isReady(), is(true));

    }
}
