package io.springreact.autoconfigure;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Reads Vite's {@code manifest.json} (produced by {@code vite build}) so the server
 * can reference the correct hashed JS/CSS files in PROD. Loaded lazily and cached.
 */
public class ReactManifest {

    private final ObjectMapper objectMapper;
    private final String location;
    private volatile Map<String, ManifestEntry> entries; // null until first load attempt
    private volatile boolean loaded;

    public ReactManifest(ObjectMapper objectMapper, String location) {
        this.objectMapper = objectMapper;
        this.location = location;
    }

    /** True when a Vite build manifest is present on the classpath (i.e. a real build exists). */
    public boolean isAvailable() {
        return load() != null;
    }

    /** The JS file for an entry (e.g. {@code assets/main-ab12cd.js}), or null if unknown. */
    public String jsFile(String entryKey) {
        Map<String, ManifestEntry> all = load();
        if (all == null) {
            return null;
        }
        ManifestEntry e = all.get(entryKey);
        return e != null ? e.file : null;
    }

    /** All CSS files for an entry, including CSS pulled in by its imported chunks. */
    public List<String> cssFiles(String entryKey) {
        Map<String, ManifestEntry> all = load();
        List<String> result = new ArrayList<>();
        if (all == null) {
            return result;
        }
        Set<String> visited = new LinkedHashSet<>();
        collectCss(all, entryKey, visited, result);
        return result;
    }

    private void collectCss(Map<String, ManifestEntry> all, String key,
                            Set<String> visited, List<String> out) {
        if (!visited.add(key)) {
            return;
        }
        ManifestEntry e = all.get(key);
        if (e == null) {
            return;
        }
        if (e.css != null) {
            for (String c : e.css) {
                if (!out.contains(c)) {
                    out.add(c);
                }
            }
        }
        if (e.imports != null) {
            for (String imp : e.imports) {
                collectCss(all, imp, visited, out);
            }
        }
    }

    private Map<String, ManifestEntry> load() {
        if (!loaded) {
            synchronized (this) {
                if (!loaded) {
                    entries = readFromClasspath();
                    loaded = true;
                }
            }
        }
        return entries;
    }

    private Map<String, ManifestEntry> readFromClasspath() {
        Resource resource = new ClassPathResource(location);
        if (!resource.exists()) {
            return null;
        }
        try (InputStream in = resource.getInputStream()) {
            return objectMapper.readValue(in, new TypeReference<Map<String, ManifestEntry>>() {});
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read Vite manifest at " + location, ex);
        }
    }

    /** A single record in Vite's manifest.json. */
    public static class ManifestEntry {
        public String file;
        public List<String> css;
        public List<String> imports;
    }
}
