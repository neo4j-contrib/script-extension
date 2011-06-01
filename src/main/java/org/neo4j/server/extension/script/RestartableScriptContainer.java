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

import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.logging.Logger;

public class RestartableScriptContainer {
    private static final Logger logger = new Logger(RestartableScriptContainer.class);

    private ScriptingContainer container;
    private String rubyHome;

    public RestartableScriptContainer(String rubyHome, GraphDatabaseService gds) {
        this.rubyHome = rubyHome;
        start(gds);
    }

    public String getRubyHome() {
        return rubyHome;
    }

    public ScriptingContainer getContainer() {
        return container;
    }

    public Object put(java.lang.String key, java.lang.Object value) {
        return container.put(key, value);
    }

    public Object runScriptlet(String script) {
        Object ret = container.runScriptlet(script);
        if (ret == null)
            return "";
        else
            return ret;
    }

    public String getHomeDirectory() {
        return container.getHomeDirectory();
    }

    public void start(GraphDatabaseService gds) {
        if (container != null)
            throw new RuntimeException("Already running");

        container = new ScriptingContainer(LocalContextScope.SINGLETHREAD); // LocalContextScope.CONCURRENT
        logger.info("Create new CONTAINER - set jrubyHome = '" + getRubyHome() + "'");
        container.setHomeDirectory(getRubyHome());   // needs for "require 'rubygems'"
        container.put("$NEO4J_SERVER", gds);
    }

    public void loadGemsFromGraphDb(GraphDatabaseService gds) {
        if (Gemfile.existInGraphDb(gds)) {
            logger.info("gemfile stored in graph db, create file");
            Gemfile gemfile = Gemfile.createFileFromGraphDb(gds);
            gemfile.loadGems(this);
        } else {
            logger.info("No gemfile stored in graph db");
        }
    }

    public void restart(GraphDatabaseService gds) {
        stop();
        start(gds);
    }

    public void stop() {
        logger.info("Stop container");
        container.terminate();
        container = null;
        System.gc();
    }

}
