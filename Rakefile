require 'rake'
require 'rspec/core/rake_task'

NEO4J_SERVER = "/home/andreas/software/neo4j/neo4j"
JAR_FILE = "target/script-extension-jruby-0.1-SNAPSHOT.jar"

task :default => [:all]

desc "Config server"
task :install do
  # create a folder where the Gemfile are stored
  system "mkdir -p #{NEO4J_SERVER}/jruby" 

  # install the configuration for the server
  system "cp config/neo4j-server.properties #{NEO4J_SERVER}/conf" 
end

desc "Compile"
task :compile do
  system "mvn clean; mvn install -DskipTests=true"
end

desc "Copy Plugin"
task :copy_plugin do
  system "cp #{JAR_FILE} #{NEO4J_SERVER}/plugins"
end

desc "Restart"
task :restart do
  system "#{NEO4J_SERVER}/bin/neo4j restart"
end

desc "Compile, copy jar and restart server"
task :all => [:compile, :copy_plugin, :restart]


desc "Run all specs"
RSpec::Core::RakeTask.new("spec") do |t|
  t.rspec_opts = ["-f d -c"]
end