package org.neo4j.server.plugin.gremlin;

import junit.framework.TestCase;
import org.jruby.embed.ScriptingContainer;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.kernel.ImpermanentGraphDatabase;
import org.neo4j.server.rest.repr.OutputFormat;
import org.neo4j.server.rest.repr.formats.JsonFormat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;

//curl -H 'Content-Type: application/json' -H 'Accept: application/json' -d '{"script" : "42"}' http://localhost:7474/db/data/ext/RubyPlugin/graphdb/execute_script


/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 5/10/11
 * Time: 9:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class JRubyTest extends TestCase {

    private static ImpermanentGraphDatabase neo4j = null;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        neo4j = new ImpermanentGraphDatabase("target/db");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testNeoRuby() throws UnsupportedEncodingException {
        System.out.println("It works");
        Map<String,String> env = System.getenv();

        StringBuffer buf = new StringBuffer();
         Iterator iter = env.entrySet().iterator();
    while (iter.hasNext()) {
        buf.append(iter.next().toString());
      System.out.println(iter.next());
    }

        neo4j = new ImpermanentGraphDatabase("target/db");

        ScriptingContainer container = new ScriptingContainer();
        String script = "puts \"Neo: #{neo.class}\"; 42";
        InputStream is = new ByteArrayInputStream(script.getBytes("UTF-8"));
        container.put("neo", neo4j);
        Object result = container.runScriptlet(is, "myfile.rb");
        System.out.println("Result " + result);
    }
}
