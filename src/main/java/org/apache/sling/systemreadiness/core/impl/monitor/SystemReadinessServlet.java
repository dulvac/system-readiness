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
package org.apache.sling.systemreadiness.core.impl.monitor;

import org.apache.sling.systemreadiness.core.monitor.SystemReadinessMonitor;
import org.osgi.annotation.versioning.ProviderType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;


@Component(
        name = "System Readiness Servlet",
        service = {Servlet.class},
        configurationPid = "org.apache.sling.systemreadiness.core.impl.monitor.SystemReadinessServlet",
        immediate = true
)
@Designate(ocd=SystemReadinessServlet.Config.class)
@ProviderType
public class SystemReadinessServlet extends HttpServlet {

    @ObjectClassDefinition(
            name ="System Readiness Servlet",
            description="Servlet exposing a configurable http endpoint for showing the status reported by the system readiness monitor"
    )
    public @interface Config {

        @AttributeDefinition(name = "Servlet Path", description = "The servlet path")
        String servlet_path() default SystemReadinessServlet.DEFAULT_PATH;

    }

    private static final String INSTANCE_READY = "Instance ready";

    private static final String INSTANCE_NOT_READY = "Instance not ready";
    private static final String DEFAULT_PATH = "/system/console/ready";
    private static final String CONTEXT_NAME = "org.osgi.service.http";
    private static final Logger LOG = LoggerFactory.getLogger(SystemReadinessServlet.class);

    private ServiceRegistration<Servlet> servletRegistration;
    private SystemReadinessMonitor monitor;

    @Activate
    protected void activate(final BundleContext ctx, final Map<String, Object> properties, final Config config) {
        final ServiceReference<SystemReadinessMonitor> serviceRef = ctx.getServiceReference(SystemReadinessMonitor.class);
        this.monitor = ctx.getService(serviceRef);

        final String path = config.servlet_path();

        if ((null != path ) && (!path.isEmpty())) {
            // Register the servlet
            final Dictionary<String, String> servletConfig = new Hashtable<>();
            servletConfig.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT,
                    "(" + HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME + "=" + CONTEXT_NAME + ")");
            servletConfig.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, path);
            servletConfig.put(Constants.SERVICE_DESCRIPTION, "System Readiness Servlet");
            this.servletRegistration = ctx.registerService(Servlet.class, this, servletConfig);
            LOG.info("Registered servlet to listen on {}", path);
        }
    }

    /**
     * Process polling requests
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: better response
        if (!this.monitor.isReady()) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, INSTANCE_NOT_READY);
            return;
        } else {
            response.getWriter().print(INSTANCE_READY); // all servicechecks reported that they started.
            response.flushBuffer();
        }
    }

}
