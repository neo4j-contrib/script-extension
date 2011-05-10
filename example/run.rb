$LOAD_PATH.unshift File.join(File.dirname(__FILE__), "..", "lib")
require 'neo4j_server'

Neo4jServer.server_url = "http://localhost:7474"

#puts "upload gemfile"
#res = Neo4jServer.upload_gemfile('Gemfile')
#puts "  response =#{res}"


#puts "upload script"
#res = Neo4jServer.upload_script('my_script.rb')
#puts "  response =#{res}"

puts "install"
res = Neo4jServer.install
puts "  response =#{res}"


puts "Eval"
res = Neo4jServer.eval <<RUBY
# foo = Person.create(:name => 'foo')
# bar.friends << Person.create(:name => 'bar')
# bar.save
# bar.my_friends
RUBY
puts "  response =#{res}"






