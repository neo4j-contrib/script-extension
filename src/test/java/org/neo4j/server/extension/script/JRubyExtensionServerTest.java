package org.neo4j.server.extension.script;

import com.sun.jersey.api.client.ClientResponse;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.server.LocalTestServer;
import org.neo4j.server.RestRequest;

import java.net.URI;

import static org.junit.Assert.assertEquals;

/**
 * @author mh
 * @since 11.05.11
 */
public class JRubyExtensionServerTest {
    private static final String HOSTNAME = "localhost";
    private static final int PORT = 7473;
    private static LocalTestServer server = new LocalTestServer(HOSTNAME,PORT).withPropertiesFile("test-db.properties");

    @BeforeClass
    public static void startServer() {
        server.start();
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
        final RestRequest request = new RestRequest(new URI("http://" + HOSTNAME + ":" + PORT + "/jruby/"));
        final ClientResponse response = request.post("eval2","Neo4j.ref_node.rels.size");
        Integer count = IteratorUtil.asCollection(server.getGraphDatabase().getReferenceNode().getRelationships()).size();
        assertEquals(count, response.getEntity(Integer.class));
    }

}
