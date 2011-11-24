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

import junit.framework.TestCase;
import org.jruby.embed.ScriptingContainer;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.test.ImpermanentGraphDatabase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

//curl -H 'Content-Type: application/json' -H 'Accept: application/json' -d '{"script" : "42"}' http://localhost:7474/db/data/ext/RubyPlugin/graphdb/execute_script

/**
 * @author andreas
 * @since 5/10/11
 */
public class JRubyTest extends TestCase {

    private static ImpermanentGraphDatabase neo4j = null;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        neo4j = new ImpermanentGraphDatabase();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testNeoRuby() throws UnsupportedEncodingException {
        System.out.println("It works");
        Map<String, String> env = System.getenv();

        for (Map.Entry<String, String> entry : env.entrySet()) {
            System.out.println(entry);
        }

        neo4j = new ImpermanentGraphDatabase();

        ScriptingContainer container = new ScriptingContainer();
        String script = "puts \"Neo: #{neo.class}\"; 42";
        InputStream is = new ByteArrayInputStream(script.getBytes("UTF-8"));
        container.put("neo", neo4j);
        Object result = container.runScriptlet(is, "myfile.rb");
        System.out.println("Result " + result);
    }
}
