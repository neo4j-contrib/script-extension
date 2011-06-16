require File.join(File.dirname(__FILE__), 'spec_helper')

GEMS_LOCATION="/Users/tbaum/Projekte/neo4j/hosting-extension/data/gems/Gemfile"

# TODO integrate into maven-build, start neo4j-server


describe Neo4jServer do
  before(:all) do
    Neo4jServer.server_url = "http://localhost:7474"
  end

  describe "upload_gemfile" do
    after(:each) do
      Neo4jServer.delete_gemfile
    end

    it "creates a new file in jruby folder in the server home directory" do
      # when
      Neo4jServer.upload_gemfile('spec/fixture/Gemfile')

      # then 
      File.exist?(GEMS_LOCATION).should be_true
      FileUtils.cmp('spec/fixture/Gemfile', GEMS_LOCATION).should be_true
    end

    it "install the correct Gems according to the Gemfile" do
      # when
      code = Neo4jServer.upload_gemfile('spec/fixture/Gemfile')
      code, body = Neo4jServer.eval %{JSON::VERSION}
      puts "BODY = #{body}"
      body.should == '1.5.1'
      code.should == '200'
    end

    it "install a different version of gem" do
      pending
      # when installing a gemfile with JSON 1.5.0
      Neo4jServer.eval 'Object.send(:remove_const, "JSON")'
      
      code, body = Neo4jServer.upload_gemfile('spec/fixture/Gemfile2')
      code.should == "200"
      puts "uploaded gemfile2"

      code, body = Neo4jServer.eval %{$".delete_if{|x| x =~ /json/}; require 'json'; JSON::VERSION}
      body.should == '1.5.0'
      code.should == '200'
    end

    
  end
end
