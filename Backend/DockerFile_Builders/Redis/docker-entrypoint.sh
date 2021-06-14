#!/bin/sh

#if [ "$1" = 'redis-cluster' ]; then
#    sleep 10
#    echo "yes" | redis-cli --cluster create 10.0.75.2:7001 10.0.75.2:7002 10.0.75.2:7003 --cluster-replicas 1 --verbose
#    echo "DONE"
#else
#  exec "$@"
#fi

 redis-cli --cluster create 172.17.0.1:7000 172.17.0.1:7001 \
 172.17.0.1:7002 172.17.0.1:7003 172.17.0.1:7004 172.17.0.1:7005 \
 --cluster-replicas 1 --verbose
 #redis-cli --cluster create 10.0.75.2:7001 10.0.75.2:7002 10.0.75.2:7003 --cluster-replicas 1 --verbose