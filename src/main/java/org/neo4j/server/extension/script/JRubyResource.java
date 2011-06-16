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

import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.logging.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;
import static org.jruby.javasupport.JavaUtil.convertJavaToRuby;

@Path("/jruby")
public class JRubyResource {

    private static final Logger LOG = new Logger(JRubyResource.class);
    private final GraphDatabaseService database;

    public JRubyResource(@Context GraphDatabaseService database) {
        this.database = database;
    }

    @POST @Consumes("application/x-www-form-urlencoded") @Path("/call")
    public Response call(@Context EmbeddedRackApplicationFactory f, MultivaluedMap<String, String> formParams) {
        LOG.info("Call2");
        try {
            for (String key : formParams.keySet()) {
                LOG.info("Key '" + key + "' value: '" + formParams.getFirst(key) + "'");
            }
            String rubyclass = formParams.getFirst("class");
            String rubymethod = formParams.getFirst("method");
            String nbrArgs = formParams.getFirst("args");
            LOG.info("Call class: '" + rubyclass + "' method: '" + rubymethod + "' #args: " + nbrArgs);
            final Ruby container = f.getRuntime();

            // container.put("$NEO4J_SERVER", database); // looks like the initialization is not always run ???
            int size = Integer.parseInt(nbrArgs);

            StringBuilder script = new StringBuilder();
            script.append(rubyclass).append(".send(:").append(rubymethod);

            for (int i = 0; i < size; i++) {
                script.append(",");
                String argName = "arg" + i;
                LOG.info("Set '" + argName + "' = '" + formParams.get(argName) + "'");
                container.getGlobalVariables().set(argName, convertJavaToRuby(container, formParams.getFirst(argName)));
                script.append(argName);
            }
            script.append(")");
            LOG.info("Call '" + script.toString() + "'");
            Object result = container.evalScriptlet(script.toString());
            return Response.status(OK).entity((result.toString()).getBytes()).build();
        } catch (Exception e) {
            LOG.error(e);
            return Response.status(INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST @Produces(TEXT_PLAIN) @Path("/config")
    public Response createConfigFile(@Context JRubyRackContext container, String txt) throws IOException {
        LOG.info("Create new config");

        ServerResource res = container.getConfigRu();
        res.store(txt, database);

        container.restart();

        LOG.info("Initialized");
        return Response.status(OK).build();
    }

    @POST @Produces(TEXT_PLAIN) @Path("/gemfile")
    public Response createGemFile(@Context JRubyRackContext container,
                                  @Context EmbeddedRackApplicationFactory f, String txt) throws IOException {
        LOG.info("Create new Gemfile");
        GemfileServerResource gemFile = f.getGemFile();
        gemFile.store(txt, database);
        container.restart();
        return Response.status(OK).build();
    }

    @DELETE @Path("/config")
    public Response deleteConfigFile(@Context JRubyRackContext container, String txt) throws IOException {
        ServerResource config = container.getConfigRu();
        config.delete(database);
        container.restart();
        return Response.status(OK).build();
    }

    @DELETE @Path("/gemfile")
    public Response deleteGemFile(@Context JRubyRackContext container,
                                  @Context EmbeddedRackApplicationFactory f, String txt) throws IOException {
        GemfileServerResource gemFile = f.getGemFile();
        gemFile.delete(database);
        container.restart();
        return Response.status(OK).build();
    }

    @POST @Produces(TEXT_PLAIN) @Path("/eval")
    public Response eval(@Context EmbeddedRackApplicationFactory f, String script) throws IOException {
        LOG.info("Eval: '" + script + "'");
        // container.put("$NEO4J_SERVER", database);
        final Ruby container = f.getRuntime();
        IRubyObject result = container.evalScriptlet(script);
        return Response.status(OK).entity(
                (result.toString()).getBytes()).build();
    }
}