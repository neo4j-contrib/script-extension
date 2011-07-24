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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.server.LocalTestServer;
import org.neo4j.server.RestRequest;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

/**
 * @author mh
 * @since 11.05.11
 */
public class JRubyLoggingTest {
    private static final String HOSTNAME = "localhost";
    private static final int PORT = 7473;
    public static final String URI = "http://" + HOSTNAME + ":" + PORT + "/";
    private static LocalTestServer server = new LocalTestServer(HOSTNAME, PORT).withPropertiesFile("test-db.properties");
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
        gds.getReferenceNode().removeProperty("script/gemfile");
        tx.success();
        tx.finish();
        server.cleanDb();
    }

    @Test
    public void testEval() throws Exception {
        final ClientResponse resp1 = request.post("script/gemfile", "" +
                "source :gemcutter\n" +
                "gem 'sinatra'\n");
        final String result1 = resp1.getEntity(String.class);
        System.out.println("= script/gemfile ================================================");
        System.out.println(result1);
        resp1.close();


        final ClientResponse resp2 = request.post("script/config", "" +
                "require 'sinatra'\n" +
                "class App < Sinatra::Base\n" +
                "    get '/' do\n" +
                "      puts 'some stdout'\n" +
                "      'ok'\n" +
                "    end\n" +
                "    get '/fail' do\n" +
                "       puts 'some-other stdout'\n" +
                "       raise 'syntax-error'\n" +
                "    end\n" +
                "end\n" +
                "run App");

        String result2 = resp2.getEntity(String.class);
        System.out.println("= script/config ================================================");
        System.out.println(result2);
        resp2.close();

        final ClientResponse resp2a = request.post("script/restart", "");

        String result2a = resp2a.getEntity(String.class);
        System.out.println("= script/restart ================================================");
        System.out.println(result2a);
        resp2a.close();


        Date now = new Date();

        final ClientResponse response3 = request.get("dsr");
        String result3 = response3.getEntity(String.class);
        System.out.println("= dsr ================================================");
        System.out.println(result3);
        response3.close();

        final ClientResponse response4 = request.get("script/log/" + now.getTime());
        String result4 = response4.getEntity(String.class);
        System.out.println("= script/log/" + now.getTime() + "  ================================================");
        System.out.println(result4);
        response4.close();

        now = new Date();

        final ClientResponse response5 = request.get("dsr/fail");
        String result5 = response5.getEntity(String.class);
        System.out.println("= dsr/fail ================================================");
        System.out.println(result5);
        response5.close();


        final ClientResponse response6 = request.get("script/log/" + now.getTime());
        String result6 = response6.getEntity(String.class);
        System.out.println("= script/jruby/log/" + now.getTime() + "  ================================================");
        System.out.println(result6);
        response6.close();
    }

}
