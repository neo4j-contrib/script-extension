package org.neo4j.server.scriptextension;

import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;
import org.neo4j.graphdb.GraphDatabaseService;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.Iterator;
import java.util.Map;


@Path("/jruby")
public class JRubyResource {

    private final GraphDatabaseService database;
    private static File gemFile;

    public JRubyResource(@Context GraphDatabaseService database) {
        this.database = database;
    }


    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/gemfile")
    public Response gemfile(InputStream is) throws IOException {
//        gemFile = createGemFile(is);

        ScriptingContainer container = scriptingContainer();
        container.setHomeDirectory("/home/andreas/.rvm/rubies/jruby-1.6.1");   // needs for "require 'rubygems'"

        String gemFile = new File(getRubyHome(), "Gemfile").toString();
        if (! new File(gemFile).exists())
            throw new RuntimeException("Not found " + gemFile);

        // tell jruby where the Gemfile is located.
        container.runScriptlet("ENV['BUNDLE_GEMFILE'] = \"" + gemFile + "\"");
        container.runScriptlet("require 'rubygems'");
        container.runScriptlet("require 'bundler/setup'");
        container.runScriptlet("Bundler.require");

        return Response.status(Response.Status.OK).entity((
                "created: " + gemFile.toString()).getBytes()).build();
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/script")
    public Response script(InputStream is) throws IOException {
        if (true) {
             if (gemFile == null || !gemFile.exists())
                throw new RuntimeException("Not found " + gemFile);
//            container.runScriptlet("Bundler.require");
//            container.runScriptlet("require 'neo4j'");

        return Response.status(Response.Status.OK).entity((
                "create script file: ").getBytes()).build();
        } else {
//        File scriptFile = createScriptFile(is);

        File gemFile = new File(getRubyHome(), "Gemfile");
         if (! gemFile.exists())
            throw new RuntimeException("Not found " + gemFile);
        ScriptingContainer container = scriptingContainer();

        // tell jruby where the Gemfile is located.
        container.runScriptlet("ENV['BUNDLE_GEMFILE'] = \"" + gemFile + "\"");
        container.runScriptlet("require 'rubygems'");
        container.runScriptlet("require 'bundler/setup'");
        container.runScriptlet("Bundler.require");
        container.runScriptlet("require 'neo4j'");

        container.setHomeDirectory("/home/andreas/.rvm/rubies/jruby-1.6.1");   // needs for "require 'rubygems'"
        container.put("$NEO4J_SERVER", this.database);

        // or we should rewind the input stream if possible instead of creating a new InputStream
  //      InputStream is2 = new BufferedInputStream(new FileInputStream(scriptFile));


        Object result = container.runScriptlet(is, "script.rb");

        return Response.status(Response.Status.OK).entity((
                "create script file: " + result.toString()).getBytes()).build();
        }
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/eval")
    public Response eval(InputStream is) {
        ScriptingContainer container = scriptingContainer();

        Object result = container.runScriptlet(is, "eval.rb");

        // Do stuff with the database
        return Response.status(Response.Status.OK).entity(
                (result.toString()).getBytes()).build();
    }
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/eval2")
    public Response eval2(@Context ScriptingContainer container, String input) {

        Object result = container.runScriptlet(input);

        // Do stuff with the database
        return Response.status(Response.Status.OK).entity(
                (result.toString()).getBytes()).build();
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


    // --- HELPERS

    private ScriptingContainer scriptingContainer() {
        // This model isolates variable maps but shares Ruby runtime. A single ScriptingContainer created only
        // one runtime and thread local variable maps. Global variables and top level variables/constants are
        // not thread safe, but variables/constants tied to receiver objects are thread local.
        return new ScriptingContainer(LocalContextScope.CONCURRENT);
    }


    private String getServerHome() {
        return System.getenv("yajsw_home");
    }

    private String getRubyHome() {
        return new File(getServerHome(), "jruby").toString();
    }

    private File createGemFile(InputStream is) throws IOException {
        File gemFile = new File(getRubyHome(), "Gemfile");
        writeFile(is, gemFile);
        return gemFile;
    }

    private File createScriptFile(InputStream is) throws IOException {
        File scriptFile = new File(getRubyHome(), "script.rb");
        writeFile(is, scriptFile);
        return scriptFile;
    }

    private void writeFile(InputStream is, File gemFile) throws IOException {
        if (gemFile.exists()) {
            gemFile.delete();
        }
        OutputStream os = new FileOutputStream(gemFile);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        is.close();
        os.close();
    }


    // DEBUG ----------------------------
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

}