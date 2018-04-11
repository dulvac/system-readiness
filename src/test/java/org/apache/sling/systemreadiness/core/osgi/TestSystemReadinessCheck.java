package org.apache.sling.systemreadiness.core.osgi;

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
