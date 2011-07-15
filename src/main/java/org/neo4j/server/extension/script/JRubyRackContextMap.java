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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tbaum
 * @since 21.06.11 11:42
 */
public class JRubyRackContextMap {
    private final Map<String, JRubyRackContext> ctx = new HashMap<String, JRubyRackContext>();

    public void addEndpoint(String mount, JRubyRackContext context) {
        ctx.put(mount, context);
    }

    public JRubyRackContext getEndpoint(String endpoint) {
        JRubyRackContext container = ctx.get(endpoint);
        if (container == null) {
            throw new IllegalArgumentException("endpoint " + endpoint + " not found");
        }
        return container;
    }

    public String getLogAll(Date since) {
        StringBuilder result = new StringBuilder();
        for (String s : ctx.keySet()) {
            result.append(String.format("=== for %s\n", s));
            result.append(ctx.get(s).getLog(since));
            result.append("\n");
        }
        return result.toString();
    }

    public String getSingleEndpointName() {
        if (ctx.size() != 1) {
            throw new IllegalArgumentException("more than one endpoint installed");
        }
        return ctx.keySet().iterator().next();
    }

    public void restartAll() {
        for (JRubyRackContext jRubyRackContext : ctx.values()) {
            jRubyRackContext.restart();
        }
    }
}
