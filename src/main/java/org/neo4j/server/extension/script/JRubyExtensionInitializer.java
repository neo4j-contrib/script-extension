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
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.logging.Logger;
import org.neo4j.server.plugins.Injectable;
import org.neo4j.server.plugins.PluginLifecycle;

import java.util.Arrays;
import java.util.Collection;

public class JRubyExtensionInitializer implements PluginLifecycle {
    private static final Logger logger = new Logger(JRubyExtensionInitializer.class);
    /**
     * whatever is returned from this methos can be injected with @Context ParamType param in other resources
     * @param gds
     * @param config server config
     * @return
     */
    @Override
    public Collection<Injectable<?>> start( GraphDatabaseService gds, Configuration config ) {
        logger.info("START " + JRubyExtensionInitializer.class.toString());
        final String jrubyHome = config.getString("org.neo4j.server.extension.scripting.jruby");
        RestartableScriptContainer container = new RestartableScriptContainer(jrubyHome, gds);
        container.loadGemsFromGraphDb(gds);
        return Arrays.<Injectable<?>>asList(new ScriptExtensionInjectable<RestartableScriptContainer>(container));
    }


    public void stop() {
    }



    private static class ScriptExtensionInjectable<T> implements Injectable<T> {

        private final T value;
        private final Class<? extends T> type;

        public ScriptExtensionInjectable(T value) {
            this(value, (Class<T>) value.getClass());
        }

        public ScriptExtensionInjectable(T value, Class<? extends T> type) {
            this.value = value;
            this.type = type;
        }

        @Override
        public T getValue() {
            return value;
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public Class<T> getType() {
            return (Class<T>) type;
        }
    }

}