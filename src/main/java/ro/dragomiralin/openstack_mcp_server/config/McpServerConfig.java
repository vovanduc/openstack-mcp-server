package ro.dragomiralin.openstack_mcp_server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.ai.mcp.server.McpServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.logging.Logger;

@Configuration
public class McpServerConfig {

    private static final Logger logger = Logger.getLogger(McpServerConfig.class.getName());

    @Autowired(required = false)
    private McpServerService mcpServerService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("OpenStack MCP Server is ready and running");
        if (mcpServerService != null) {
            logger.info("MCP Server Service is available");
        } else {
            logger.warning("MCP Server Service is not available");
        }
    }
}
