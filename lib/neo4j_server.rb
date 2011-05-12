#require 'rubygems'
require 'net/http'

module Neo4jServer

  module ClassMethods

    def server_url=(url)
      @server_url = URI.parse(url)
    end

    def server_url
      @server_url || URI.parse('http://localhost:7474')
    end

    # create a new gemfile
    def upload_gemfile(file_location)
      file = File.read(file_location)
      res = Net::HTTP.start(server_url.host, server_url.port) do |http|
        http.post('/script/jruby/gemfile', file)
      end
      [res.code, res.body]
    end

    def delete_gemfile
      res = Net::HTTP.start(server_url.host, server_url.port) do |http|
        http.delete('/script/jruby/gemfile')
      end
      res.code
    end
   
    def eval(s)
      res = Net::HTTP.start(server_url.host, server_url.port) do |http|
        http.post('/script/jruby/eval', s)
      end
      [res.code, res.body]
    end

  end

  extend ClassMethods

end
