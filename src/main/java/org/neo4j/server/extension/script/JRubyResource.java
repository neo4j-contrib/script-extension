package org.neo4j.server.extension.script;

import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.logging.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.Iterator;
import java.util.Map;


@Path("/jruby")
public class JRubyResource {

    private static final Logger logger = new Logger(JRubyResource.class);
    private final GraphDatabaseService database;

    public JRubyResource(@Context GraphDatabaseService database) {
        this.database = database;
    }


    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/gemfile")
    public Response gemfile(@Context ScriptingContainer container, String txt) throws IOException {

        // First store the new gemfile in the graph db
        logger.info("Store new gemfile in db");
        Gemfile.storeInGraphDb(txt, database);

        // create the file on the filesystem
        logger.info("Create file");
        Gemfile gemfile = Gemfile.createFileFromGraphDb(database);

        // initialize all the gems
        logger.info("Load gems");
        gemfile.loadGems(container);

        logger.info("Initialized all gems");
        return Response.status(Response.Status.OK).entity((
                "created at: " + gemfile.getGemFile().toString()).getBytes()).build();
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/eval")
    public Response eval(@Context ScriptingContainer container, String script) throws IOException {
        logger.info("Eval " + script);
        Object result = container.runScriptlet(script);

        // Do stuff with the database
        return Response.status(Response.Status.OK).entity(
                (result.toString()).getBytes()).build();
    }


    // DEBUG ----------------------------
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/status")
    public Response status() {

        Map<String, String> env = System.getenv();

        StringBuffer buf = new StringBuffer();
        Iterator iter = env.entrySet().iterator();
        while (iter.hasNext()) {
            buf.append(iter.next().toString());
            buf.append("\n");
            //System.out.println(iter.next());
        }


        // Do stuff with the database
        return Response.status(Response.Status.OK).entity(
                buf.toString().getBytes()).build();
    }

}