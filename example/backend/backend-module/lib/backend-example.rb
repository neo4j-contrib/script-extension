require "sinatra"
require "neo4j"

module Backend

  class Person
    include Neo4j::NodeMixin
    property :name
    has_n :friends
    index :name

    def to_s
      "Person name: '#{name}'"
    end
  end


  class App < Sinatra::Base

    get '/' do
      "<h1>Simple-App:</h1>
      <ul>
      <li><a href='create'>./create -> create-node</a></li>
      <li><a href='list'>./list -> all-nodes</a></li>
      </ul>"
    end

    get "/list" do
      "<h1>all nodes</h1>" +
          Neo4j.all_nodes.collect do |node|
            "#{node.id}: #{node}<br/>" unless node == Neo4j.ref_node
          end.join
    end

    get "/create" do
      person = Neo4j::Transaction.run { Person.new }
      Neo4j::Transaction.run { person.name = 'kalle' }

      "created #{person}"
    end
  end

end