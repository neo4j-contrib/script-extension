Neo4j - JRuby Server Extension
===============================

Installation
------------

1. extract the script-extension-jruby-{version}-server-plugin.zip files into {NEO4J_HOME}/plugins

2. add script-extension in {NEO4J_HOME}/conf/neo4j-server.properties through adding 
   `org.neo4j.server.thirdparty_jaxrs_classes=org.neo4j.server.extension.script=/script`

3. install your Gemfile and config.ru 

````
curl -XPOST --data-binary @Gemfile http://localhost:7474/script/jruby/gemfile
curl -XPOST --data-binary @config.ru http://localhost:7474/script/jruby/config
````

4. the rack-application then should load at http://localhost:7474/rack


Eval:
----

You can eval a ruby script

    curl -d "class Person < Neo4j::Rails::Model; end; 42" http://localhost:7474/script/jruby/eval
    curl -d "Person.create(:name => 'kalle')" http://localhost:7474/script/jruby/eval
    curl -d "Neo4j.ref_node.rels.size" http://localhost:7474/script/jruby/eval


Call Ruby Method
----------------

You can execute a method in a class by posting a form with the parameter class, method and arg0, arg1, arg2... and args to the number of argument.

http://localhost:7474/script/jruby/call
