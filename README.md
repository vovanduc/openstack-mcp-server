# OpenStack MCP Server

## Overview
A lightweight and extensible service that enables AI assistants to securely execute OpenStack CLI commands via the Model Context Protocol (MCP).

This project is designed to work with the [Claude Desktop](https://www.anthropic.com/claude-desktop) application, allowing you to interact with OpenStack resources directly from the AI assistant.


## Project Requirements

- Java 21
- Maven 3.8+
- Spring Boot 3.4.4
- Spring AI 1.0.0-M7
- OpenStack CLI installed on your machine

## Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:
- Java 21
- Maven 3.8+
- OpenStack CLI

### Configuration

1. **OpenStack Configuration**: Create an `openstack.env` file with your OpenStack credentials.
   ```bash
   # Copy the template
   cp openstack.env.template openstack.env
   
   # Edit with your actual values
   OPENSTACK_AUTH_TYPE=v3applicationcredential
   OPENSTACK_AUTH_URL=http://your-openstack-url:5000
   OPENSTACK_IDENTITY_API_VERSION=3
   OPENSTACK_REGION_NAME=your-region
   OPENSTACK_INTERFACE=public
   OPENSTACK_APPLICATION_CREDENTIAL_ID=your-credential-id
   OPENSTACK_APPLICATION_CREDENTIAL_SECRET=your-credential-secret
   ```

2. **Load Environment Variables**: Before running the application, load the environment variables:
   ```bash
   source load-env.sh
   ```

3. **Alternative**: You can also set environment variables directly:
   ```bash
   export OPENSTACK_AUTH_URL=http://your-openstack-url:5000
   export OPENSTACK_APPLICATION_CREDENTIAL_ID=your-credential-id
   export OPENSTACK_APPLICATION_CREDENTIAL_SECRET=your-credential-secret
   # ... other variables
   ```

### Packaging

To package the application as a JAR file, run the following command:

```bash
mvn clean package
```

### Integration with Claude Desktop

To integrate the OpenStack MCP server with Claude Desktop, you need to configure the `claude-desktop.json` file. This file contains the necessary configuration for the integration.

Please update `<path_to_your_jar>` with the actual path to your JAR file.

```json
{
  "mcpServers": {
    "openstack-mcp-server": {
      "command": "java",
      "args": [
        "-jar",
        "<path_to_your_jar>/openstack-mcp-server-0.0.1.jar",
        "--port",
        "8080",
        "--host",
        "localhost"
      ]
    }
  }
}
```

### Testing

To test the integration, prompt to Claude Desktop to list your servers, or projects:

- `List my servers`
- `List my projects`
- `List my images`

![get-server](/assets/get-server.png)
![get-flavors](/assets/list-flavors.png)
![get-servers](/assets/list-servers.png)