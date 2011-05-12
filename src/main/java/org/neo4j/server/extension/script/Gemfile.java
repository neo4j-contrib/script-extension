package org.neo4j.server.extension.script;

import org.jruby.embed.ScriptingContainer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import java.io.*;

/**
 * Represent a Ruby Gemfile on the filesystem
 */
public class Gemfile {

    private File gemFile;

    public Gemfile(File file) {
        gemFile = file;
        if (!gemFile.exists())
            throw new RuntimeException("Gemfile missing " + gemFile.toString());
    }

    public File getGemFile() {
        return gemFile;
    }

    public void loadGems(ScriptingContainer container) {
        // tell jruby where the Gemfile is located.

        container.runScriptlet("require 'rubygems'");
        container.runScriptlet("ENV['BUNDLE_GEMFILE'] = \"" + gemFile.toString() + "\"");
        container.runScriptlet("Dir.chdir('" + directory() + "'); system 'bundle install'" );
        container.runScriptlet("require 'bundler/setup'");
        // TODO not sure if we should require all the gems. Maybe we want to unload them first
        container.runScriptlet("Bundler.require");
    }

    public static String getServerHome() {
        // TODO, hardcoded location
        return System.getenv("yajsw_home");
    }

    /**
     * Location of the Gemfile and where the ruby scripts are being executed.
     */
    public static String directory() {
        // TODO, hardcoded location
        return new File(getServerHome(), "jruby").toString();
    }


    public static boolean existOnFile() {
        return getFile().exists();
    }

    public static boolean existInGraphDb(GraphDatabaseService gds) {
        return gds.getReferenceNode().hasProperty("gemFile");
    }

    public static void deleteFile() {
        getFile().delete();
        if (getLockFile().exists()) {
            getLockFile().delete();
        }
    }


    public static Gemfile createFileFromGraphDb(GraphDatabaseService gds) {
        String gemfile = (String) gds.getReferenceNode().getProperty("gemFile");
        try {
            return new Gemfile(createGemFile(gemfile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void storeInGraphDb(String gemfile, GraphDatabaseService gds) throws IOException {
        Transaction tx = gds.beginTx();
        gds.getReferenceNode().setProperty("gemFile", gemfile);
        tx.success();
        tx.finish();
    }

    public static void deleteInGraphDb(GraphDatabaseService gds) throws IOException {
        Transaction tx = gds.beginTx();
        gds.getReferenceNode().removeProperty("gemFile");
        tx.success();
        tx.finish();
    }

    private static File getFile() {
        return new File(directory(), "Gemfile");
    }

    private static File getLockFile() {
        return new File(directory(), "Gemfile.lock");
    }

    private static File createGemFile(String is) throws IOException {
        File gemFile = getFile();
        writeFile(is, gemFile);
        return gemFile;
    }

    private static void writeFile(String input, File file) throws IOException {
        InputStream is = new ByteArrayInputStream(input.getBytes("UTF-8"));
        writeFile(is, file);
    }

    private static void writeFile(InputStream is, File file) throws IOException {
        if (file.exists()) {
            file.delete();
        }
        OutputStream os = new FileOutputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        is.close();
        os.close();
    }

}
