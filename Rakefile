require 'rake'
require 'rspec/core/rake_task'

NEO4J_SERVER = "/home/andreas/software/neo4j/neo4j"
JAR_FILE = "script-extension-jruby-0.1-SNAPSHOT.jar"

task :default => [:all]

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
