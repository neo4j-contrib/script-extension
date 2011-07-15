#!/bin/bash
server=${1-'localhost:7474'}
curl -XDELETE @Gemfile http://$server/script/jruby/gemfile
curl -XPUT --data-binary @Gemfile http://$server/script/jruby/gemfile
curl -XPOST --data-binary @config.ru http://$server/script/jruby/config
