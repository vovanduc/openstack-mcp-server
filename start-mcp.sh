#!/bin/bash

# Load environment variables
if [ -f "openstack.env" ]; then
    echo "Loading OpenStack environment variables..."
    export $(cat openstack.env | grep -v '^#' | xargs)
    echo "Environment variables loaded successfully!"
else
    echo "Warning: openstack.env file not found!"
    echo "Please create openstack.env with your OpenStack configuration."
fi

# Start MCP Server
echo "Starting OpenStack MCP Server..."
java -jar target/openstack-mcp-server-0.0.1-SNAPSHOT.jar 