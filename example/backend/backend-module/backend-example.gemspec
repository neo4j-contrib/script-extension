lib = File.expand_path('../lib/', __FILE__)
$:.unshift lib unless $:.include?(lib)

Gem::Specification.new do |s|
  s.name = "backend-example"
  s.version = "0.0.1"
  s.summary = "neo4j script-extension backend-example"
  s.require_path = 'lib'
  s.files = Dir.glob("**/*")
  s.test_files = Dir.glob("{test,spec,features}/**/*")

  s.required_ruby_version = ">= 1.8.7"

  s.add_dependency("sinatra", [">= 1.1.3"])
  s.add_dependency("neo4j", [">= 1.1.2"])
end