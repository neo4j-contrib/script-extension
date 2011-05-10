mvn clean
mvn install -DskipTests=true
cp target/script-extension-jruby-0.1-SNAPSHOT.jar /home/andreas/software/neo4j/neo4j/plugins/
/home/andreas/software/neo4j/neo4j/bin/neo4j restart
