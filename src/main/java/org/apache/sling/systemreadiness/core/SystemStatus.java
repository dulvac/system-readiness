package org.apache.sling.systemreadiness.core;

import java.util.Collection;

import org.apache.sling.systemreadiness.core.Status.State;

public class SystemStatus {
    State state;
    Collection<CheckStatus> checkStates;
    
    public SystemStatus(State state, Collection<CheckStatus> checkStates) {
        super();
        this.state = state;
        this.checkStates = checkStates;
    }

    public State getState() {
        return state;
    }
    
    public Collection<CheckStatus> getCheckStates() {
        return checkStates;
    }
}
