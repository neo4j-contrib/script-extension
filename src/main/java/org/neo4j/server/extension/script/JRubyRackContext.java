/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.server.extension.script;

import org.apache.commons.configuration.Configuration;
import org.jruby.rack.*;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.resource.Resource;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static org.neo4j.helpers.collection.MapUtil.map;

/**
 * @author tbaum
 * @since 07.06.2011
 */
public class JRubyRackContext extends Context {

    private static final Logger LOG = new Logger(JRubyRackContext.class);

    private final ServerResource configRu;
    private final GraphDatabaseService gds;
    private final EmbeddedRackApplicationFactory factory;

    public JRubyRackContext(String contextPath, GraphDatabaseService gds, Configuration configuration,
                            EmbeddedRackApplicationFactory factory) throws IOException {
        super(null, contextPath, false, false);
        this.gds = gds;
        this.factory = factory;

        final String gemHome = Util.locateGemHome(configuration);
        configRu = new ServerResource(new File(gemHome, "config.ru"), "sinatra_config");

        setBaseResource();

        configureRackWebContext();
    }

    private void configureRackWebContext() {
        setInitParams();
        addFilter(new FilterHolder(RackFilter.class), "/*", Handler.ALL);
        addServlet(RackServlet.class, "/*");

        addEventListener(new RackServletContextListener() {
            @Override protected RackApplicationFactory newApplicationFactory(final RackConfig rackConfig) {
                return new SharedRackApplicationFactory(JRubyRackContext.this.factory);
            }
        });
    }

    private void setInitParams() {
        final Map params = map(
                "jruby.min.runtimes", "2",
                "jruby.max.runtimes", "4",
                "jruby.rack.filter.adds.html", "false"
        );
        updateConfigRu(params);
        setInitParams(params);
    }

    private void setBaseResource() throws IOException {
        final URL base = getClass().getResource("/");
        setBaseResource(Resource.newResource(base));
    }

    public ServerResource getConfigRu() {
        return configRu;
    }

    public void restart() {
        factory.shutdownAllRuntimes();

        LOG.info("doing restart");
        try {
            stop();
        } catch (Exception e) {
            LOG.error(e);
        }

        updateConfigRu(getInitParams());

        try {
            start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void updateConfigRu(final Map initParams) {
        if (configRu.existInGraphDb(gds)) {
            initParams.put("rackup", configRu.retrieve(gds));
        } else {
            initParams.remove("rackup");
        }
    }
}