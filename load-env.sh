#!/bin/bash

# Load OpenStack environment variables
if [ -f "openstack.env" ]; then
    echo "Loading OpenStack environment variables from openstack.env..."
    export $(cat openstack.env | grep -v '^#' | xargs)
    echo "Environment variables loaded successfully!"
else
    echo "Warning: openstack.env file not found!"
    echo "Please create openstack.env with your OpenStack configuration."
fi 