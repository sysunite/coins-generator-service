#!/bin/bash

echo 'version in swagger:'
cat src/main/resources/public/swagger.yaml | grep '  version: '
echo 'version in pom:'
cat pom.xml | grep '^  <version>'

read -n 1 -p 'Are those equal (ctrl-c if not)'?;

set -e

docker build -f Dockerfile-build -t sysunite/javabuild:0.1 .
echo -n "Building jar..."
docker run -it --rm -v $(pwd)/target:/usr/src/app/target sysunite/javabuild:0.1 package -q -U -T 1C -o -Dmaven.test.skip=true
echo "Done"
