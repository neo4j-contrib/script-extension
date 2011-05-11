package org.neo4j.server.scriptextension;

import org.apache.commons.configuration.Configuration;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.plugins.Injectable;
import org.neo4j.server.plugins.PluginLifecycle;

import java.util.Arrays;
import java.util.Collection;

public class JRubyExtensionInitializer implements PluginLifecycle {

    /**
     * whatever is returned from this methos can be injected with @Context ParamType param in other resources
     * @param gds
     * @param config server config
     * @return
     */
    @Override
    public Collection<Injectable<?>> start( GraphDatabaseService gds, Configuration config ) {
        // steps, init scripting container, load gemfiles, run bundler
        String gemfile = getGemFile(gds);
        final String jrubyHome = config.getString("org.neo4j.server.extension.scripting.jruby");
        ScriptingContainer container = setupContainer(gemfile, jrubyHome, gds);
        return Arrays.<Injectable<?>>asList(new ScriptExtensionInjectable<ScriptingContainer>(container));
    }

    private ScriptingContainer setupContainer(String gemFile, String jrubyHome, GraphDatabaseService gds) {
        ScriptingContainer container = new ScriptingContainer(LocalContextScope.CONCURRENT);
        container.setHomeDirectory(jrubyHome);   // needs for "require 'rubygems'"
        container.put("$NEO4J_SERVER", gds);
        return loadGems(container, gemFile);
    }

    private ScriptingContainer loadGems(ScriptingContainer container, String gemFile) {
        // tell jruby where the Gemfile is located.
        container.runScriptlet("require 'rubygems'");
        if (gemFile!=null) {
            container.runScriptlet("ENV['BUNDLE_GEMFILE'] = \"" + gemFile + "\"");
            container.runScriptlet("require 'bundler/setup'");
            container.runScriptlet("Bundler.require");
        }
        container.runScriptlet("require 'neo4j'");
        return container;
    }


    private String getGemFile(GraphDatabaseService gds) {
        return (String) gds.getReferenceNode().getProperty("gemfile",null);
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