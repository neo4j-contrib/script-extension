package org.neo4j.examples.server.unmanaged;

import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;
import org.neo4j.graphdb.GraphDatabaseService;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;


@Path("/jruby")
public class JRubyResource {

    private final GraphDatabaseService database;

    public JRubyResource(@Context GraphDatabaseService database) {
        this.database = database;
    }

    private String getServerHome() {
        return System.getenv("yajsw_home");
    }

    private String getRubyHome() {
        return new File(getServerHome(), "jruby").toString();
    }


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/install")
    public Response install() {
        ScriptingContainer container = new ScriptingContainer(LocalContextScope.CONCURRENT);
        container.setHomeDirectory("/home/andreas/.rvm/rubies/jruby-1.6.1");   // needs for "require 'rubygems'"

        String gemFile = new File(getRubyHome(), "Gemfile").toString();
        if (! new File(gemFile).exists())
            throw new RuntimeException("Not found " + gemFile);

        // tell jruby where the Gemfile is located.
        container.runScriptlet("ENV['BUNDLE_GEMFILE'] = \"" + gemFile + "\"");
        container.runScriptlet("require 'rubygems'");
        container.runScriptlet("require 'bundler/setup'");
        container.runScriptlet("Bundler.require");
        container.runScriptlet("require 'neo4j'");
        container.runScriptlet("require 'json'");
        container.runScriptlet("require 'twitter'");

        container.put("$NEO_SERVER", this.database);
        container.runScriptlet("Neo4j.start(nil, $NEO_SERVER)");

        return Response.status(Response.Status.OK).entity(
                "installed".getBytes()).build();

    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/")
    public Response status() {

        Map<String, String> env = System.getenv();

        StringBuffer buf = new StringBuffer();
        Iterator iter = env.entrySet().iterator();
        while (iter.hasNext()) {
            buf.append(iter.next().toString());
            buf.append("\n");
            System.out.println(iter.next());
        }


        // Do stuff with the database
        return Response.status(Response.Status.OK).entity(
                buf.toString().getBytes()).build();
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response eval(InputStream is) {
        // This model isolates variable maps but shares Ruby runtime. A single ScriptingContainer created only
        // one runtime and thread local variable maps. Global variables and top level variables/constants are
        // not thread safe, but variables/constants tied to receiver objects are thread local.
        ScriptingContainer container = new ScriptingContainer(LocalContextScope.CONCURRENT);

        Object result = container.runScriptlet(is, "myfile.rb");

        // Do stuff with the database
        return Response.status(Response.Status.OK).entity(
                ("Result " + result.toString()).getBytes()).build();
    }

}