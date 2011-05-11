package org.neo4j.server.extension.script;

import com.sun.jersey.api.client.ClientResponse;
import org.junit.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.server.LocalTestServer;
import org.neo4j.server.RestRequest;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author mh
 * @since 11.05.11
 */
public class JRubyExtensionServerTest {
    private static final String HOSTNAME = "localhost";
    private static final int PORT = 7473;
    public static final String URI = "http://" + HOSTNAME + ":" + PORT + "/script/jruby/";
    private static LocalTestServer server = new LocalTestServer(HOSTNAME,PORT).withPropertiesFile("test-db.properties");
    private static RestRequest request;

    @BeforeClass
    public static void startServer() throws URISyntaxException {
        server.start();
        request = new RestRequest(new URI(URI));
    }
    @AfterClass
    public static void stopServer() {
        server.stop();
    }
    @Before
    public void cleanup() {
        final GraphDatabaseService gds = server.getGraphDatabase();
        final Transaction tx = gds.beginTx();
        gds.getReferenceNode().removeProperty("gemfile");
        tx.success();
        tx.finish();
        server.cleanDb();
    }

    @Test
    public void testEval2() throws Exception {
        final ClientResponse response = request.post("eval2", "\"$NEO4J_SERVER.getReferenceNode().getId()\"");
        final String result = response.getEntity(String.class);
        System.out.println(result);
        Assert.assertEquals(server.getGraphDatabase().getReferenceNode().getId(), Long.parseLong(result));
    }

}
