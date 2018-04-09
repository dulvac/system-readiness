package org.apache.sling.systemreadiness.core.impl;

import static org.apache.sling.systemreadiness.core.CheckStatus.State.GREEN;
import static org.apache.sling.systemreadiness.core.CheckStatus.State.YELLOW;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.sling.systemreadiness.core.impl.ServicesCheck;
import org.apache.sling.systemreadiness.core.impl.ServicesCheck.Config;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class ServicesRegisteredTest {

    @Mock
    BundleContext ctx;
    
    @Mock
    Config config;

    @Captor
    private ArgumentCaptor<ServiceListener> listenerCaptor;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void test() throws InterruptedException, InvalidSyntaxException {
        when(config.services_list()).thenReturn(new String[] {"MyService"});

        ServicesCheck check = new ServicesCheck();
        check.activate(ctx, config);
        assertThat(check.getStatus().getState(), equalTo(YELLOW));
        assertThat(check.getStatus().getDetails(), equalTo("Missing services : MyService"));

        registerService("MyService");
        assertThat(check.getStatus().getState(), equalTo(GREEN));
        assertThat(check.getStatus().getDetails(), equalTo(""));
        
        check.deactivate();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void registerService(String objectClass) throws InvalidSyntaxException {
        verify(ctx).addServiceListener(listenerCaptor.capture(), Mockito.eq("(objectClass=" + objectClass + ")"));
        ServiceListener listener = listenerCaptor.getValue();
        ServiceReference ref = Mockito.mock(ServiceReference.class);
        Object service = Mockito.mock(Object.class);
        when(ctx.getService(Mockito.eq(ref))).thenReturn(service);
        listener.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, ref));
    }
}
