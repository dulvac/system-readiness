package org.apache.sling.systemreadiness.core.osgi;

import org.apache.sling.systemreadiness.core.CheckStatus;
import org.apache.sling.systemreadiness.core.SystemReadinessCheck;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.sling.systemreadiness.core.CheckStatus.State.*;

@Component(
        name = "TestSystemReadinessCheck"
)
public class TestSystemReadinessCheck implements SystemReadinessCheck {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private BundleContext bundleContext;
    private CheckStatus state;

    public static final CheckStatus green = new CheckStatus(GREEN, "Green");
    public static final CheckStatus yellow = new CheckStatus(YELLOW, "Yellow");
    public static final CheckStatus red = new CheckStatus(RED, "Red");
    private AtomicReference<RuntimeException> ex = new AtomicReference<>(null);

    @Activate
    protected void activate(final BundleContext ctx, final Map<String, Object> properties) throws InterruptedException {
        this.bundleContext = ctx;
        this.state = yellow;
        log.info("Activated");
    }

    @Deactivate
    protected void deactivate() throws InterruptedException {
        this.bundleContext = null;
    }

    @Override
    public CheckStatus getStatus() {
        if (null == ex.get()) {
            return this.state;
        } else {
            throw ex.get();
        }
    }

    public synchronized SystemReadinessCheck green() {
        this.ex.set(null);
        this.state = green;
        return this;
    }

    public synchronized SystemReadinessCheck yellow() {
        this.ex.set(null);
        this.state = yellow;
        return this;
    }

    public synchronized SystemReadinessCheck red() {
        this.ex.set(null);
        this.state = red;
        return this;
    }

    public synchronized SystemReadinessCheck exception() {
        this.ex.set(new RuntimeException("Failure"));
        this.state = red;
        return this;
    }

    public Exception getException() {
        return ex.get();
    }
}
