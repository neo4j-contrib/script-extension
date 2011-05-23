package org.neo4j.server.extension.script;

import org.jruby.embed.ScriptingContainer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.logging.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.List;
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
    public Response createGemfile(@Context RestartableScriptContainer container, String txt) throws IOException {
        System.out.println("Create new Gemfile");
        logger.info("NEW GEMFILE");
        container.restart(database);

        // First store the new gemfile in the graph db
        logger.info("Store new gemfile in db");
        Gemfile.storeInGraphDb(txt, database);

        // create the file on the filesystem
        logger.info("Create file");
        Gemfile gemfile = Gemfile.createFileFromGraphDb(database);

        // initialize all the gems
        logger.info("Load gems");
        gemfile.loadGems(container);

        container.put("$NEO4J_SERVER", database); // looks like the initialization is not always run ???

        logger.info("Initialized all gems");
        return Response.status(Response.Status.OK).entity((
                "created at: " + gemfile.getGemFile().toString()).getBytes()).build();
    }

    @DELETE
    @Path("/gemfile")
    public Response deleteGemfile(@Context RestartableScriptContainer container, String txt) throws IOException {
        if (Gemfile.existOnFile()) {
            logger.info("Delete gemfile on filesystem");
            Gemfile.deleteFile();
        }
        if (Gemfile.existInGraphDb(database)) {
            logger.info("Delete in graph db");
            Gemfile.deleteInGraphDb(database);
        }

        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/eval")
    public Response eval(@Context RestartableScriptContainer container, String script) throws IOException {
        logger.info("Eval: '" + script + "'");

        container.put("$NEO4J_SERVER", database); // looks like the initialization is not always run ???

        Object result = container.runScriptlet(script);
        return Response.status(Response.Status.OK).entity(
                (result.toString()).getBytes()).build();
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Path("/call")
    public Response call(@Context RestartableScriptContainer container, MultivaluedMap<String, String> formParams) {
        logger.info("Call2");
        try {
            for (String key : formParams.keySet()) {
                logger.info("Key '" + key + "' value: '" + formParams.getFirst(key) + "'");
            }
            String rubyclass = formParams.getFirst("class");
            String rubymethod = formParams.getFirst("method");
            String nbrArgs  = formParams.getFirst("args");
            logger.info("Call class: '" + rubyclass + "' method: '" + rubymethod + "' #args: " + nbrArgs);
            container.put("$NEO4J_SERVER", database); // looks like the initialization is not always run ???

            int size = Integer.parseInt(nbrArgs);

            // Isn't java painful :(
            StringBuffer script = new StringBuffer();
            script.append(rubyclass + ".send(:" + rubymethod);
            if (size > 0)
                script.append(",");

            for (int i = 0; i < size; i++) {
                String argName = "arg" + i;
                logger.info("Set '" + argName + "' = '" + formParams.get(argName) + "'");
                container.put(argName, formParams.getFirst(argName));
                script.append(argName);
                if (i < size - 1)
                    script.append(",");
            }
            script.append(")");
            logger.info("Call '" + script.toString() + "'");
            Object result = container.runScriptlet(script.toString());
            return Response.status(Response.Status.OK).entity(
                    (result.toString()).getBytes()).build();
        } catch (Exception e) {
            logger.error("Can't call");
            logger.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DEBUG ----------------------------
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/status")
    public Response status(@Context RestartableScriptContainer container) {

        Map<String, String> env = System.getenv();

        StringBuffer buf = new StringBuffer();

        buf.append("container.getHomeDirectory(): " + container.getHomeDirectory() + "\n");
        buf.append("GemFile location: " + Gemfile.directory() + "\n");
        buf.append("GemFile existOnFile: " + Gemfile.existOnFile() + "\n");

//        Iterator iter = env.entrySet().iterator();
//        while (iter.hasNext()) {
//            buf.append(iter.next().toString());
//            buf.append("\n");
//            //System.out.println(iter.next());
//        }
//
//
        // Do stuff with the database
        return Response.status(Response.Status.OK).entity(
                buf.toString().getBytes()).build();
    }

}