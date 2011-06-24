gem build backend-example.gemspec
mkdir -p repo/gems
mv backend-example-0.0.1.gem repo/gems
gem generate_index -d repo
