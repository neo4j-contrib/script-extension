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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.server.NeoServerWithEmbeddedWebServer;
import org.neo4j.server.RestRequest;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static junit.framework.Assert.assertEquals;
import static org.neo4j.server.helpers.ServerBuilder.server;

/**
 * @author mh
 * @since 11.05.11
 */
public class JRubyExtensionServerTest {
    private static final String HOSTNAME = "localhost";
    private static final int PORT = 7473;
    private static final String URI = "http://" + HOSTNAME + ":" + PORT + "/script/";
    private RestRequest request;
    private NeoServerWithEmbeddedWebServer server;

    @Before public void setup() throws IOException, URISyntaxException {
        server = server().onPort(PORT)
                .withThirdPartyJaxRsPackage("org.neo4j.server.extension.script", "/script")
                .build();
        server.start();
        request = new RestRequest(new URI(URI));
        request.setMediaType(MediaType.TEXT_PLAIN_TYPE);
    }

    @After public void tearDown() {
        server.stop();
    }

    @Test public void testEval() throws Exception {
        final ClientResponse response = request.post("eval", "$NEO4J_SERVER.getReferenceNode().getId()");
        final String result = response.getEntity(String.class);
        System.out.println(result);
        assertEquals(server.getDatabase().graph.getReferenceNode().getId(), Long.parseLong(result));
    }
}
