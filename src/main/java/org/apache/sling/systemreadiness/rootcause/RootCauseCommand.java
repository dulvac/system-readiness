package org.apache.sling.systemreadiness.rootcause;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;

@Component(
        service = RootCauseCommand.class,
        property = {
                "osgi.command.scope=readiness", //
                "osgi.command.function=rootcause"
        }
        )
public class RootCauseCommand {
    
    @Reference
    ServiceComponentRuntime scr;
    
    public DSComp rootcause(String componentName) {
        ComponentDescriptionDTO cdesc = scr.getComponentDescriptionDTOs().stream()
            .filter(desc -> desc.name.equals(componentName))
            .findFirst().get();
        DSComp rootCause = new DSRootCause(scr).getRootCause(cdesc);
        new RootCausePrinter().print(rootCause);
        return rootCause;
    }
}
