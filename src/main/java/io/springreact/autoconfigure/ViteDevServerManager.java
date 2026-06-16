package io.springreact.autoconfigure;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

/**
 * In DEV mode, starts the frontend toolchain ({@code npm run dev}) as a managed child
 * process and stops it on shutdown — so a single {@code bootRun} brings up the whole
 * stack and one console shows both Spring and Vite logs. No second command to run.
 *
 * <p>In PROD (a built bundle is present) this does nothing: assets are served from the
 * JAR and the entire app is one process on one port.
 */
public class ViteDevServerManager implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(ViteDevServerManager.class);

    private final ReactProperties properties;
    private final ReactRenderer renderer;

    private volatile Process process;
    private volatile boolean running;

    public ViteDevServerManager(ReactProperties properties, ReactRenderer renderer) {
        this.properties = properties;
        this.renderer = renderer;
    }

    @Override
    public void start() {
        if (!renderer.isDev() || !properties.isManageDevServer()) {
            return;
        }
        File dir = new File(properties.getFrontendDir());
        if (!new File(dir, "package.json").isFile()) {
            log.warn("[spring-react] manageDevServer is on but no package.json in {} — "
                    + "skipping. Run the dev server yourself or set spring.react.frontend-dir.",
                    dir.getAbsolutePath());
            return;
        }
        try {
            boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
            String npm = windows ? "npm.cmd" : "npm";
            log.info("[spring-react] starting frontend dev server: {} run dev (in {})",
                    npm, dir.getAbsolutePath());
            process = new ProcessBuilder(npm, "run", "dev")
                    .directory(dir)
                    .inheritIO() // stream Vite output into the Spring console
                    .start();
            running = true;
        } catch (Exception ex) {
            log.warn("[spring-react] could not start frontend dev server: {}", ex.getMessage());
        }
    }

    @Override
    public void stop() {
        running = false;
        Process p = this.process;
        if (p != null && p.isAlive()) {
            log.info("[spring-react] stopping frontend dev server");
            p.destroy();
            try {
                if (!p.waitFor(5, TimeUnit.SECONDS)) {
                    p.destroyForcibly();
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                p.destroyForcibly();
            }
        }
        this.process = null;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /** Start after the web server is up, stop early on shutdown. */
    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
