#!/bin/bash
server=${1-'localhost:7474'}
curl -XPOST --data-binary @config.ru http://$server/script/jruby/config
curl -XPOST --data-binary @Gemfile http://$server/script/jruby/gemfile
