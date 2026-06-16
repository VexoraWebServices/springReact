package io.springreact.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the Spring + React integration, bound from {@code spring.react.*}.
 */
@ConfigurationProperties(prefix = "spring.react")
public class ReactProperties {

    public enum Mode {
        /** Decide automatically: PROD if a Vite manifest is on the classpath, else DEV. */
        AUTO,
        /** Reference the running Vite dev server (HMR). */
        DEV,
        /** Reference hashed assets from the built bundle. */
        PROD
    }

    /** How asset URLs are resolved. Defaults to AUTO. */
    private Mode mode = Mode.AUTO;

    /** Base URL of the Vite dev server, used only in DEV mode. */
    private String devServerUrl = "http://localhost:5173";

    /** The Vite entry module (must match rollupOptions.input in vite.config). */
    private String entry = "src/main.tsx";

    /** Classpath location of the Vite build manifest. */
    private String manifestLocation = "static/.vite/manifest.json";

    /** Default <title> for the rendered HTML shell. */
    private String title = "Spring React";

    /** Filesystem location of the frontend project (where package.json lives). */
    private String frontendDir = "frontend";

    /**
     * In DEV mode, let Spring start/stop the frontend toolchain (npm run dev) as a child
     * process so you only run one command. Set false to manage Vite yourself.
     */
    private boolean manageDevServer = true;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public String getDevServerUrl() {
        return devServerUrl;
    }

    public void setDevServerUrl(String devServerUrl) {
        // Trim a trailing slash so we can concatenate paths cleanly.
        this.devServerUrl = devServerUrl.endsWith("/")
                ? devServerUrl.substring(0, devServerUrl.length() - 1)
                : devServerUrl;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public String getManifestLocation() {
        return manifestLocation;
    }

    public void setManifestLocation(String manifestLocation) {
        this.manifestLocation = manifestLocation;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFrontendDir() {
        return frontendDir;
    }

    public void setFrontendDir(String frontendDir) {
        this.frontendDir = frontendDir;
    }

    public boolean isManageDevServer() {
        return manageDevServer;
    }

    public void setManageDevServer(boolean manageDevServer) {
        this.manageDevServer = manageDevServer;
    }
}
