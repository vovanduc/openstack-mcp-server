package ro.dragomiralin.openstack_mcp_server;

import java.util.List;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import ro.dragomiralin.openstack_mcp_server.service.OpenStackCommander;

@SpringBootApplication
public class OpenstackMcpServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(OpenstackMcpServerApplication.class, args);
	}

	@Bean
	public List<ToolCallback> openstackTools(OpenStackCommander openStackCommander) {
		return List.of(ToolCallbacks.from(openStackCommander));
	}

}
