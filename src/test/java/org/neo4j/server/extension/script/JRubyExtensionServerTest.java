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

import com.sun.jersey.api.client.ClientResponse;
import org.junit.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.server.LocalTestServer;
import org.neo4j.server.RestRequest;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author mh
 * @since 11.05.11
 */
public class JRubyExtensionServerTest {
    private static final String HOSTNAME = "localhost";
    private static final int PORT = 7473;
    public static final String URI = "http://" + HOSTNAME + ":" + PORT + "/script/";
    private static LocalTestServer server = new LocalTestServer(HOSTNAME,PORT).withPropertiesFile("test-db.properties");
    private static RestRequest request;

    @BeforeClass
    public static void startServer() throws URISyntaxException {
        server.start();
        request = new RestRequest(new URI(URI));
        request.setMediaType(MediaType.TEXT_PLAIN_TYPE);
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
    public void testEval() throws Exception {
        final ClientResponse response = request.post("eval", "$NEO4J_SERVER.getReferenceNode().getId()");
        final String result = response.getEntity(String.class);
        System.out.println(result);
        Assert.assertEquals(server.getGraphDatabase().getReferenceNode().getId(), Long.parseLong(result));
    }

}
