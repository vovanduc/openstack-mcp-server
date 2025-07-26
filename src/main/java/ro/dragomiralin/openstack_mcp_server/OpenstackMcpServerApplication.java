package ro.dragomiralin.openstack_mcp_server;

import java.util.List;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import ro.dragomiralin.openstack_mcp_server.service.OpenStackCommander;

@SpringBootApplication
public class OpenstackMcpServerApplication {

	public static void main(String[] args) {
		System.err.println("Starting OpenStack MCP Server...");
		SpringApplication.run(OpenstackMcpServerApplication.class, args);
	}

	@Bean
	public List<ToolCallback> openstackTools(OpenStackCommander openStackCommander) {
		System.err.println("Registering OpenStack tools...");
		return List.of(ToolCallbacks.from(openStackCommander));
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		System.err.println("OpenStack MCP Server is ready and running");
		// Keep the application alive
		Thread keepAliveThread = new Thread(() -> {
			try {
				Thread.sleep(Long.MAX_VALUE);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
		keepAliveThread.setDaemon(false);
		keepAliveThread.start();
	}

}
