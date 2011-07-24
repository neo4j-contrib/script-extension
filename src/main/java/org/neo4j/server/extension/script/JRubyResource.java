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
import org.jruby.internal.runtime.GlobalVariables;
import org.jruby.runtime.builtin.IRubyObject;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.extension.script.resources.ServerResource;
import org.neo4j.server.logging.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Date;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;
import static org.jruby.javasupport.JavaUtil.convertJavaToRuby;

@Path("/")
public class JRubyResource {
    private static final Logger LOG = new Logger(JRubyResource.class);
    private final GraphDatabaseService database;

    public JRubyResource(@Context GraphDatabaseService database) {
        this.database = database;
    }

    @POST @Consumes("application/x-www-form-urlencoded") @Path("/call")
    public Response call(@Context EmbeddedRackApplicationFactory applicationFactory,
                         MultivaluedMap<String, String> formParams) {
        LOG.info("Call2");
        try {
            for (String key : formParams.keySet()) {
                LOG.info("Key '" + key + "' value: '" + formParams.getFirst(key) + "'");
            }
            String rubyclass = formParams.getFirst("class");
            String rubymethod = formParams.getFirst("method");
            String nbrArgs = formParams.getFirst("args");
            LOG.info("Call class: '" + rubyclass + "' method: '" + rubymethod + "' #args: " + nbrArgs);
            Ruby container = applicationFactory.getRuntime();
            
            int size = Integer.parseInt(nbrArgs);

            StringBuilder script = new StringBuilder();
            script.append(rubyclass).append(".send(:").append(rubymethod);
            GlobalVariables globalVariables = container.getGlobalVariables();

            for (int i = 0; i < size; i++) {
                script.append(",");
                String argName = "arg" + i;
                String value = formParams.getFirst(argName);
                LOG.info("Set '" + argName + "' = '" + value + "'");
                globalVariables.set(argName, convertJavaToRuby(container, value));
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
    public Response createConfigFile(@Context JRubyRackContextMap contextMap,
                                     String data) throws IOException {
        String endpoint = contextMap.getSingleEndpointName();
        return createConfigFile(contextMap, endpoint, data);
    }

    @POST @Produces(TEXT_PLAIN) @Path("/config/{endpoint}")
    public Response createConfigFile(@Context JRubyRackContextMap contextMap,
                                     @PathParam("endpoint") String endpoint,
                                     String data) throws IOException {
        LOG.info("Create new config for " + endpoint);

        if (!endpoint.startsWith("/")) {
            endpoint = "/" + endpoint;
        }

        JRubyRackContext container = contextMap.getEndpoint(endpoint);
        container.getConfigRu().store(data, database);
        return Response.status(OK).build();
    }

    @POST @Produces(TEXT_PLAIN) @Path("/gemfile")
    public Response createGemFile(@Context EmbeddedRackApplicationFactory applicationFactory,
                                  String data) throws IOException {
        LOG.info("Create new Gemfile");
        applicationFactory.getGemFile().store(data, database);
        return Response.status(OK).build();
    }

    @DELETE @Path("/config")
    public Response deleteConfigFile(@Context JRubyRackContextMap contextMap) throws IOException {
        String singleEndpoint = contextMap.getSingleEndpointName();
        return deleteConfigFile(contextMap, singleEndpoint);
    }

    @DELETE @Path("/config/{endpoint}")
    public Response deleteConfigFile(@Context JRubyRackContextMap contextMap,
                                     @PathParam("endpoint") String endpoint) throws IOException {
        JRubyRackContext container = contextMap.getEndpoint("/" + endpoint);
        container.getConfigRu().delete(database);
        return Response.status(OK).build();
    }

    @DELETE @Path("/gemfile")
    public Response deleteGemFile(@Context EmbeddedRackApplicationFactory applicationFactory) throws IOException {
        applicationFactory.getGemFile().delete(database);
        return Response.status(OK).build();
    }

    @POST @Produces(TEXT_PLAIN) @Path("/eval")
    public Response eval(@Context EmbeddedRackApplicationFactory applicationFactory, String script) {
        LOG.info("Eval: '" + script + "'");
        Ruby container = applicationFactory.getRuntime();
        IRubyObject result = container.evalScriptlet(script);
        return Response.status(OK).entity((result.toString()).getBytes()).build();
    }

    @GET @Produces({TEXT_PLAIN, APPLICATION_JSON}) @Path("/log")
    public Response log(@Context JRubyRackContextMap contextMap) {
        return Response.status(OK).entity(contextMap.getLogAll(null)).build();
    }

    @GET @Produces({TEXT_PLAIN, APPLICATION_JSON}) @Path("/log/{since}")
    public Response log(@Context JRubyRackContextMap contextMap, @PathParam("since") long since) {
        return Response.status(OK).entity(contextMap.getLogAll(new Date(since))).build();
    }

    @POST @Path("/restart")
    public Response restart(@Context JRubyRackContextMap contextMap) {
        Date start = new Date();
        contextMap.restartAll();
        return Response.status(OK).entity(contextMap.getLogAll(start)).build();
    }

    @POST @Path("/restart/{endpoint}")
    public Response restart(@Context JRubyRackContextMap contextMap, @PathParam("endpoint") String endpoint) {
        Date start = new Date();
        JRubyRackContext container = contextMap.getEndpoint("/" + endpoint);
        container.restart();
        return Response.status(OK).entity(container.getLog(start)).build();
    }
}
