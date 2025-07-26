package ro.dragomiralin.openstack_mcp_server.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OpenStackCommander {

    private static final Logger logger = Logger.getLogger(OpenStackCommander.class.getName());

    @Value("${openstack.authType}")
    private String authType;
    @Value("${openstack.authUrl}")
    private String authUrl;
    @Value("${openstack.identityApiVersion}")
    private String apiVersion;
    @Value("${openstack.regionName}")
    private String region;
    @Value("${openstack.interface}")
    private String iface;
    @Value("${openstack.applicationCredentialId}")
    private String appCredId;
    @Value("${openstack.applicationCredentialSecret}")
    private String appCredSecret;

    private static final int PROCESS_TIMEOUT_SECONDS = 60;

    /**
     * Runs an OpenStack command using the configured connection credentials.
     *
     * @param command The command to execute
     * @return The output of the command execution
     */
    @Tool(name = "run_openstack_command", description = "Run OpenStack command")
    public String runOpenStackCommand(String command) {
        logger.info("Received OpenStack command: " + command);
        if (isOpenStackInstalled()) {
            return execute(command);
        } else {
            return getOpenStackDiagnostics();
        }
    }

    /**
     * Executes an OpenStack command with the configured connection credentials.
     *
     * @param command The command to execute
     * @return The output of the command execution
     */
    public String execute(String command) {
        try {
            List<String> cmd = List.of("/bin/sh", "-c", command);
            ProcessBuilder builder = new ProcessBuilder(cmd);
            builder.redirectErrorStream(true);

            Map<String, String> env = builder.environment();
            
            // Add OpenStack path to existing PATH
            String currentPath = env.getOrDefault("PATH", "");
            String openstackPath = "/Users/vovanduc/.local/bin";
            if (!currentPath.contains(openstackPath)) {
                env.put("PATH", openstackPath + ":" + currentPath);
            }
            
            env.put("OS_AUTH_TYPE", authType);
            env.put("OS_AUTH_URL", authUrl);
            env.put("OS_IDENTITY_API_VERSION", apiVersion);
            env.put("OS_REGION_NAME", region);
            env.put("OS_INTERFACE", iface);
            env.put("OS_APPLICATION_CREDENTIAL_ID", appCredId);
            env.put("OS_APPLICATION_CREDENTIAL_SECRET", appCredSecret);

            Process process = builder.start();
            StringBuilder output = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line)
                        .append(System.lineSeparator());
                }
            }

            if (!process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return "Command execution timed out after " + PROCESS_TIMEOUT_SECONDS + " seconds";
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                return "Command execution failed with exit code: " + exitCode;
            }

            return output.toString();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread()
                .interrupt();
            return "An error occurred while executing the command: " + e.getMessage();
        }
    }

    /**
     * Checks if OpenStack is installed on the system.
     *
     * @return true if OpenStack is installed, false otherwise
     */
    public boolean isOpenStackInstalled() {
        String result = execute("which openstack");
        return result != null && !result.trim()
            .isEmpty() && !result.contains("Command execution failed");
    }

    /**
     * Provides diagnostics information about the OpenStack installation and configuration.
     *
     * @return A string containing diagnostics information
     */
    public String getOpenStackDiagnostics() {
        StringJoiner diagnostics = new StringJoiner("\n");

        String whichResult = execute("which openstack");
        diagnostics.add("OpenStack binary location: " + (whichResult.contains("Command execution failed") ? "Not found" : whichResult.trim()));

        String versionResult = execute("openstack --version");
        diagnostics.add("OpenStack version: " + (versionResult.contains("Command execution failed") ? "Unable to determine" : versionResult.trim()));

        diagnostics.add("Environment configuration:");
        diagnostics.add("  OS_AUTH_TYPE: " + (authType != null ? "Set" : "Not set"));
        diagnostics.add("  OS_AUTH_URL: " + (authUrl != null ? "Set" : "Not set"));
        diagnostics.add("  OS_IDENTITY_API_VERSION: " + (apiVersion != null ? "Set" : "Not set"));
        diagnostics.add("  OS_REGION_NAME: " + (region != null ? "Set" : "Not set"));
        diagnostics.add("  OS_INTERFACE: " + (iface != null ? "Set" : "Not set"));
        diagnostics.add("  OS_APPLICATION_CREDENTIAL_ID: " + (appCredId != null ? "Set" : "Not set"));
        diagnostics.add("  OS_APPLICATION_CREDENTIAL_SECRET: " + (appCredSecret != null ? "Set (hidden)" : "Not set"));

        return diagnostics.toString();
    }

}
