#require 'rubygems'
require 'net/http'

module Neo4jServer

  module ClassMethods

    def server_url=(url)
      @server_url = URI.parse(url)
    end


    # create a new gemfile
    def upload_gemfile(file_location)
      file = File.read(file_location)
      res = Net::HTTP.start(@server_url.host, @server_url.port) do |http|
        http.post('/jruby/gemfile', file)
      end
      [res.code, res.body]
    end


    def install
      res = Net::HTTP.start(@server_url.host, @server_url.port) do |http|
        http.get('/jruby/install')
      end
      [res.code, res.body]
    end
    def upload_script(file_location)
      file = File.read(file_location)
      res = Net::HTTP.start(@server_url.host, @server_url.port) do |http|
        http.post('/jruby/script', file)
      end
      [res.code, res.body]
    end


    def eval(s)
      res = Net::HTTP.start(@server_url.host, @server_url.port) do |http|
        http.post('/jruby/eval', s)
      end
      [res.code, res.body]
    end

  end

  extend ClassMethods

end