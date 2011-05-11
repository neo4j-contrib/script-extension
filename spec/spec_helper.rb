require 'rubygems'
require 'bundler/setup'
require 'rspec'
require 'fileutils'

$LOAD_PATH.unshift File.join(File.dirname(__FILE__), "..", "lib")
require 'neo4j_server'

