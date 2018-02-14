#!/bin/bash

set -e

docker build -f Dockerfile-build -t sysunite/javabuild:0.1 .
echo -n "Building jar..."
docker run -it --rm -v $(pwd)/target:/usr/src/app/target sysunite/javabuild:0.1 package -q -U -T 1C -o -Dmaven.test.skip=true
echo "Done"
