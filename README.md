# Usage
This is the setup for an initial java service template.


# Build
```
./compile.sh # This downloads the dependencies and created the target jar
             # within a docker composition to ensure all builds are consistent.

# Use the jar from the previous step to create the functional docker image
docker build -t sysunite/<pluginname>:<version> .
```
