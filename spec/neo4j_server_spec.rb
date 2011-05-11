require File.join(File.dirname(__FILE__), 'spec_helper')

NEO4J_SERVER = "/home/andreas/software/neo4j/neo4j"



describe Neo4jServer do
  before(:all) do
    Neo4jServer.server_url = "http://localhost:7474"
  end

  describe "upload_gemfile" do
    it "creates a new file in jruby folder in the server home directory" do
      # when
      Neo4jServer.upload_gemfile('spec/fixture/Gemfile')

      # then 
      File.exist?("#{NEO4J_SERVER}/jruby/Gemfile").should be_true
      FileUtils.cmp('spec/fixture/Gemfile', "#{NEO4J_SERVER}/jruby/Gemfile").should be_true
    end

    it "install the correct Gems according to the Gemfile" do
      # when
      code = Neo4jServer.upload_gemfile('spec/fixture/Gemfile')

      code, body = Neo4jServer.eval "(defined? JSON)? 'YES' : 'NO'"
      puts "BODY = #{body}"
      code.should == '200'
    end

    
  end
end
