#!/bin/bash

echo "Redeploying Christmas List app..."

# Quit the whole script if there is an error.
set -e

# Run tests and package.
mvn package

# Take backup of running JAR.
ssh -i ~/dannyec2.pem ec2-user@ec2-3-8-234-161.eu-west-2.compute.amazonaws.com 'cp ~/christmas_list-0.0.1-SNAPSHOT.jar ~/old.jar'

# Copy new JAR to server.
scp -i ~/dannyec2.pem target/christmas_list-0.0.1-SNAPSHOT.jar ec2-user@ec2-3-8-234-161.eu-west-2.compute.amazonaws.com:~/

# Reboot the server.
ssh -i ~/dannyec2.pem ec2-user@ec2-3-8-234-161.eu-west-2.compute.amazonaws.com sudo reboot
