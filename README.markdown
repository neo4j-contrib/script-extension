Neo4j - JRuby Server Extension
===============================

Installation
------------

1. create a folder in your server home folder, named jruby

2. goto the (this) bin directory and run the neo4j-server shell script.
That script will upload and install the Gemfile (bin/Gemfile)

You also install a gemfile by POSTing to http://localhost:7474/script/jruby/install


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
