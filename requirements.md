What do you think about the following requirements for this project ?

basics:
1. to let the developer design his own domain protocol talking to the server so we can avoid too chatty and slow communication
2. to run any (j)ruby code on the server so that the developer can continue using C Ruby
7. error handling (as status code and serialized exceptions) for compile / runtime errors
10. there should of course be some tests of all these
7. provide means for the script to declare content-type and other headers as well as status codes

gems:

3. to have dependencies to any other gems
8. persist Gemfile and script in the neo4j graph so that it can be replicated in a HA cluster
7. when the server starts it should load the Gemfile from the graph

execution modes:
1. ad-hoc post full script body to execute
2. ad-hoc rpc
8. stored-procedure like scripts that are exposed as REST endpoints


later
6. paging of large response (not sure about this).
9. would be cool to support HTTP cache headers somehow

not required
4. to persist classes and constants, so that I don't have to recreate them for each request.
5. remove those (old) classes and constants, e.g we wan't to install a new version of the classes/constants (not sure how to do this).

I'm thinking of having a very simple API for (just 3 endpoints is needed)

* create and install Gemfile
* install script which will persist on the file system/node space
* eval ruby code on the server in the context of the installed Gemfile and script, return to the client the result

I started to write this API in here:
https://github.com/neo4j/script-extension/blob/master/lib/neo4j_server.rb
