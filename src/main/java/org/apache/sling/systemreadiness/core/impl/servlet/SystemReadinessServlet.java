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
package org.apache.sling.systemreadiness.core.impl.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.sling.systemreadiness.core.SystemReadinessMonitor;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component(
        name = "SystemReadinessServlet",
        service = Servlet.class,
        property = {
                HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT + "=" + "(" + HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME + "=" + "org.osgi.service.http" + ")",
                HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN + "=" + "/system/console/ready",
        }
)
@Designate(ocd=SystemReadinessServlet.Config.class)
public class SystemReadinessServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @ObjectClassDefinition(
            name ="System Readiness Servlet",
            description="Servlet exposing a configurable http endpoint for showing the status reported by the system readiness monitor"
    )
    public @interface Config {

        @AttributeDefinition(name = "Servlet Path", description = "The servlet path")
        String osgi_http_whiteboard_servlet_pattern() default SystemReadinessServlet.DEFAULT_PATH;

    }

    private static final String INSTANCE_READY = "Instance ready";

    private static final String INSTANCE_NOT_READY = "Instance not ready";
    private static final String DEFAULT_PATH = "/system/console/ready";
    private static final Logger LOG = LoggerFactory.getLogger(SystemReadinessServlet.class);

    @Reference
    private SystemReadinessMonitor monitor;

    @Activate
    protected void activate(final BundleContext ctx, final Map<String, Object> properties, final Config config) {
        final String path = config.osgi_http_whiteboard_servlet_pattern();
        // TODO: configurable path
        LOG.info("Registered servlet to listen on {}", path);
    }

    /**
     * Process polling requests
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: better response
        if (!this.monitor.isReady()) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, INSTANCE_NOT_READY + "\n" + this.monitor.getStatuses());
            // TODO: statuses
            return;
        } else {
            response.getWriter().print(INSTANCE_READY + "\n" + this.monitor.getStatuses()); // all servicechecks reported that they started.
            response.flushBuffer();
        }
    }

}
