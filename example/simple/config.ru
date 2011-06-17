
###  simple app try /static /all /person

require 'rubygems'
require 'bundler'

Bundler.require

require 'sinatra'
require 'neo4j'

set :run, false
set :public, './public'
set :views, './views'
set :environment, :production

require 'neo4j'

class Person
  include Neo4j::NodeMixin
  property :name, :salary, :age, :country
  has_n :friends

  index :name
  index :salary
  index :age
  index :country

  def to_s
    "Person name: '#{name}'"
  end
end



class App < Sinatra::Base

configure do
end

get "/all" do

puts "List of all nodes:"
result = ""
Neo4j.all_nodes.each do |node|
  result+=" #{node}" unless node == Neo4j.ref_node
end

result

end

get "/person" do

person = Neo4j::Transaction.run { Person.new }
Neo4j::Transaction.run { person.name = 'kalle'; person.salary = 10000 }
Neo4j::Transaction.run { person['an_undefined_property'] = 'hello' }

"nice " + person.name 

end

get "/static" do

  p $NEO4_SERVER
  "ok * "
end
end


run App

puts "init - ok"
