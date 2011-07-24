#!/bin/bash
server=${1-'localhost:7474'}
curl -XPOST --data-binary @config.ru http://$server/script/config
curl -XPOST --data-binary @Gemfile http://$server/script/gemfile
curl -XPOST http://$server/script/restart
