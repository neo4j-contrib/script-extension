Neo4j - JRuby Server Extension
===============================

You need to install the (unreleased neo4j.rb 1.1.0.beta.4)

Installation
------------

1. create a folder in your server home folder, named jruby

2. add a Gemfile in this folder

```
gem 'json'
gem 'twitter'
gem 'neo4j', '1.1.0.beta.4'  # Unlreased !!!
```

Now you can install those gems in the server

    curl http://localhost:7474/jruby/install

Example:
-------

    curl -d "class Person < Neo4j::Rails::Model; end; 42" http://localhost:7474/jruby
    curl -d "Person.create(:name => 'kalle')" http://localhost:7474/jruby
    curl -d "Neo4j.ref_node.rels.size" http://localhost:7474/jruby

Currently the script must not return a nil value or it will crash
