package org.neo4j.server;

import org.apache.commons.configuration.Configuration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.kernel.ImpermanentGraphDatabase;
import org.neo4j.server.configuration.PropertyFileConfigurator;
import org.neo4j.server.database.Database;
import org.neo4j.server.database.GraphDatabaseFactory;
import org.neo4j.server.modules.RESTApiModule;
import org.neo4j.server.modules.ServerModule;
import org.neo4j.server.modules.ThirdPartyJAXRSModule;
import org.neo4j.server.startup.healthcheck.StartupHealthCheck;
import org.neo4j.server.startup.healthcheck.StartupHealthCheckRule;
import org.neo4j.server.web.Jetty6WebServer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LocalTestServer {
    private NeoServerWithEmbeddedWebServer neoServer;
    private final int port;
    private final String hostname;
    protected String propertiesFile = "test-db.properties";

    public LocalTestServer() {
        this("localhost",7473);
    }

    public LocalTestServer(String hostname, int port) {
        this.port = port;
        this.hostname = hostname;
    }

    public void start() {
        if (neoServer!=null) throw new IllegalStateException("Server already running");
        URL url = getClass().getResource("/" + propertiesFile);
        if (url==null) throw new IllegalArgumentException("Could not resolve properties file "+propertiesFile);
        final List<Class<? extends ServerModule>> serverModules = Arrays.asList(RESTApiModule.class, ThirdPartyJAXRSModule.class);
        final Bootstrapper bootstrapper = new Bootstrapper() {
            @Override
            protected GraphDatabaseFactory getGraphDatabaseFactory(Configuration configuration) {
                return new GraphDatabaseFactory() {
                    @Override
                    public AbstractGraphDatabase createDatabase(String databaseStoreDirectory, Map<String, String> databaseProperties) {
                        try {
                            return new ImpermanentGraphDatabase();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
            }

            @Override
            protected Iterable<StartupHealthCheckRule> getHealthCheckRules() {
                return Collections.emptyList();
            }

            @Override
            protected Iterable<Class<? extends ServerModule>> getServerModules() {
                return serverModules;
            }
        };
        final AddressResolver addressResolver = new AddressResolver() {
            @Override
            public String getHostname() {
                return hostname;
            }
        };
        neoServer = new NeoServerWithEmbeddedWebServer(bootstrapper,addressResolver,new StartupHealthCheck(),new PropertyFileConfigurator(new File(url.getPath())),new Jetty6WebServer(),serverModules) {
            @Override
            protected int getWebServerPort() {
                return port;
            }
        };
        neoServer.start();
    }

    public void stop() {
        try {
        neoServer.stop();
        } catch(Exception e) {
            System.err.println("Error stopping server: "+e.getMessage());
        }
        neoServer=null;
    }

    public int getPort() {
        return port;
    }

    public String getHostname() {
        return hostname;
    }

    public LocalTestServer withPropertiesFile(String propertiesFile) {
        this.propertiesFile = propertiesFile;
        return this;
    }
    public Database getDatabase() {
        return neoServer.getDatabase();
    }

    public URI baseUri() {
        return neoServer.baseUri();
    }

    public void cleanDb() {
        Neo4jDatabaseCleaner cleaner = new Neo4jDatabaseCleaner(getGraphDatabase());
        cleaner.cleanDb();
    }

    public GraphDatabaseService getGraphDatabase() {
        return getDatabase().graph;
    }
}
