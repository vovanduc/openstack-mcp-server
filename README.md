# OpenStack MCP Server

## Overview
A lightweight and extensible service that enables AI assistants to securely execute OpenStack CLI commands via the Model Context Protocol (MCP).

This project is designed to work with the [Claude Desktop](https://www.anthropic.com/claude-desktop) application, allowing you to interact with OpenStack resources directly from the AI assistant.


## Project Requirements

- Java 21
- Maven 3.8+
- Spring Boot 3.4.4
- Spring AI 1.0.0-M6
- OpenStack CLI installed on your machine

## Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:
- Java 21
- Maven 3.8+
- OpenStack CLI

### Configuration

1. **OpenStack Configuration**: Update the `application.yml` file with your OpenStack credentials.
   - `authUrl`: Your OpenStack authentication URL.
   - `regionName`: Your OpenStack region name.
   - `applicationCredentialId`: Your OpenStack application credential ID.
   - `applicationCredentialSecret`: Your OpenStack application credential secret.

```yaml
spring:
  main:
    web-application-type: none
    banner-mode: off
  ai:
    mcp:
      server:
        name: openstack-mcp-server
        version: 0.0.1

logging:
  pattern:
    console:

openstack:
  authType: v3applicationcredential
  authUrl: <your_auth_url>
  identityApiVersion: 3
  regionName: <your_region_name>
  interface: public
  applicationCredentialId: "<your_application_credential_id>"
  applicationCredentialSecret: "<your_application_credential_secret>"

server:
  port: 8080
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