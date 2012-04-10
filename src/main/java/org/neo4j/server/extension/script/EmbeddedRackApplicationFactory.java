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
import org.jruby.RubyInstanceConfig;
import org.jruby.exceptions.RaiseException;
import org.jruby.rack.DefaultRackApplicationFactory;
import org.jruby.rack.RackApplicationFactory;
import org.jruby.rack.RackContext;
import org.jruby.rack.RackInitializationException;
import org.jruby.util.collections.WeakHashSet;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.extension.script.resources.GemfileServerResource;
import org.neo4j.server.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.lang.String.format;
import static org.jruby.javasupport.JavaUtil.convertJavaToRuby;

/**
 * @author tbaum
 * @since 16.06.11 08:54
 */
public class EmbeddedRackApplicationFactory extends DefaultRackApplicationFactory implements RackApplicationFactory {

    private static final Logger LOG = new Logger(EmbeddedRackApplicationFactory.class);

    private RackContext rackContext;

    private final RubyInstanceConfig defaultConfig;
    private final WeakHashSet<Ruby> runtimes = new WeakHashSet<Ruby>();
    private final GemfileServerResource gemFile;
    private final String gemHome;
    private final GraphDatabaseAPI gds;

    private final LogBufferOutputStream stderrLogger = new LogBufferOutputStream("E ");
    private final LogBufferOutputStream stdoutLogger = new LogBufferOutputStream("  ");
    private final PrintStream stderr = new PrintStream(stderrLogger);
    private final PrintStream stdout = new PrintStream(stdoutLogger);

    private Ruby runtime;

    public EmbeddedRackApplicationFactory(final GraphDatabaseAPI gds, final String gemHome) throws IOException {
        this.gds = gds;
        this.gemHome = gemHome;
        this.defaultConfig = createDefaultConfig();
        this.gemFile = new GemfileServerResource(new File(gemHome, "GemFile"), "jruby.gemFile");
    }

    private RubyInstanceConfig createDefaultConfig() {
        RubyInstanceConfig config = new RubyInstanceConfig();
        config.setLoader(Thread.currentThread().getContextClassLoader());
        config.setError(stderr);
        config.setOutput(stdout);
        return config;
    }

    public GemfileServerResource getGemFile() {
        return gemFile;
    }

    public Ruby getRuntime() {
        return runtime;
    }

    @Override public void init(final RackContext rackContext) {
        super.init(rackContext);
        this.rackContext = rackContext;
    }

    @Override public synchronized Ruby newRuntime() throws RackInitializationException {
        if (runtime != null)
            return runtime;

        runtime = createRuntime();

        LOG.info("using gem-home: " + gemHome);

        try {
            injectNeo4J();

            installBundler();
            installUserGemfile();

            if (rackContext != null) {
                prepareServletEnv();
            }

        } catch (RaiseException e) {
            e.printStackTrace(stderr);
            throw new RackInitializationException(e);
        } catch (Exception e) {
            e.printStackTrace(stderr);
            throw new RuntimeException(e);
        }
        return runtime;
    }

    private void injectNeo4J() {
        runtime.getGlobalVariables().set("$NEO4J_SERVER", convertJavaToRuby(runtime, gds));
    }

    private void prepareServletEnv() {
        runtime.getGlobalVariables().set("$servlet_context", convertJavaToRuby(runtime, rackContext));
        if (rackContext.getConfig().isIgnoreEnvironment()) {
            runtime.evalScriptlet("ENV.clear");
        }
        runtime.evalScriptlet("require 'rack/handler/servlet'");
    }

    private void installBundler() {
        runtime = ensureRequire(runtime, "bundler");
        runtime.evalScriptlet("require 'bundler'");
    }

    private void installUserGemfile() throws IOException {
        if (gemFile.existInGraphDb(gds)) {
            gemFile.updateFileSystem(gds);
            installGem(gemFile.getFile().getCanonicalPath());
        }
    }

    private Ruby createRuntime() {
        Ruby runtime = Ruby.newInstance(defaultConfig);
        runtimes.add(runtime);
        runtime.evalScriptlet(format("ENV['GEM_HOME'] = '%s'", gemHome));
        runtime.evalScriptlet("require 'rubygems'");
        return runtime;
    }

    private Ruby ensureRequire(Ruby runtime, final String gem) {
        try {
            runtime.evalScriptlet(format("" +
                    "begin\n" +
                    "  require '%1$s'\n" +      // try to load gem
                    "rescue LoadError=>e\n" +
                    "  puts '%1$s not found -> installing'\n" +
                    "  require 'rubygems/gem_runner'\n" +      // install gem through gem_installer
                    "  begin\n" +
                    "    Gem::GemRunner.new.run ['install','%1$s']\n" +
                    "  rescue SystemExit => e\n" +
                    "  end\n" +
                    "end\n" +
                    "require '%1$s'", gem));

        } catch (RaiseException e) {
            e.printStackTrace(stderr);
            // recreate vm when gem was not installed
            runtime.tearDown(false);
            return createRuntime();
        }
        return runtime;
    }

    private void installGem(final String gemFile) {
        runtime.evalScriptlet(format("ENV['BUNDLE_GEMFILE'] = '%s'", gemFile));
        runtime.evalScriptlet("require 'bundler/cli'");
        runtime.evalScriptlet("begin\n" +
                "Bundler::CLI.start()\n" +
                "rescue SystemExit => e\n" +
                "end");

        runtime.evalScriptlet("require 'bundler/setup'");
        runtime.evalScriptlet("Bundler.require");
    }

    public synchronized void shutdownAllRuntimes() {
        System.gc();

        for (Ruby runtime : runtimes) {
            runtime.tearDown(false);
        }
        System.gc();
        System.err.println(new ArrayList<Ruby>(runtimes));

        if (runtime != null) {
            runtime.tearDown(false);
            runtime = null;
        }
    }

    public String getLog(Date since) {
        SortedSet<LogBufferOutputStream.LogLine> lines = new TreeSet<LogBufferOutputStream.LogLine>();

        lines.addAll(stderrLogger.getLogSince(since));
        lines.addAll(stdoutLogger.getLogSince(since));

        StringBuilder result = new StringBuilder();
        for (LogBufferOutputStream.LogLine line : lines) {
            result.append(line.toString()).append('\n');
        }
        return result.toString();
    }

}
