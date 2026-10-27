package io.github.verils.gotemplate.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Loads template files and classpath resources for the public Template API.
 */
public final class TemplateResourceLoader {

    private static final Set<String> GLOB_META_CHARACTERS = new LinkedHashSet<>();

    static {
        GLOB_META_CHARACTERS.add("*");
        GLOB_META_CHARACTERS.add("?");
        GLOB_META_CHARACTERS.add("[");
        GLOB_META_CHARACTERS.add("{");
    }

    private TemplateResourceLoader() {
    }

    /**
     * Loads matching regular files below a directory.
     *
     * @param directory root directory
     * @param glob      glob pattern relative to the directory
     * @return resources sorted by relative path
     * @throws IOException if the directory is invalid, no files match, or a file cannot be read
     */
    public static List<Resource> loadFiles(Path directory, String glob) throws IOException {
        validateDirectory(directory);
        PathMatcher matcher = pathMatcher(glob);
        Path root = directory.toAbsolutePath().normalize();
        List<Path> matches;
        try (Stream<Path> paths = Files.walk(root)) {
            matches = paths.filter(Files::isRegularFile)
                    .filter(path -> matcher.matches(root.relativize(path)))
                    .sorted(Comparator.comparing(path -> normalizePath(root.relativize(path))))
                    .collect(Collectors.toList());
        }
        if (matches.isEmpty()) {
            throw new IOException("No template files matched pattern '" + glob + "' in directory: " + root);
        }

        List<Resource> resources = new ArrayList<>();
        for (Path path : matches) {
            resources.add(new Resource(
                    normalizePath(root.relativize(path)),
                    path.getFileName().toString(),
                    Files.readString(path, StandardCharsets.UTF_8)));
        }
        validateUniqueNames(resources);
        return resources;
    }

    /**
     * Loads all classpath resources matching an exact path or glob.
     *
     * @param pattern exact classpath path or glob
     * @return resources sorted by normalized classpath path
     * @throws IOException if no resource matches or a resource cannot be read
     */
    public static List<Resource> loadClasspath(String pattern) throws IOException {
        String normalizedPattern = normalizeClasspathPattern(pattern);
        if (!containsGlob(normalizedPattern)) {
            Resource resource = loadExactClasspathResource(normalizedPattern);
            return Collections.singletonList(resource);
        }

        PathMatcher matcher = pathMatcher(normalizedPattern);
        Map<String, Resource> resourcesByPath = new LinkedHashMap<>();
        Set<URL> roots = classpathRoots();
        for (URL root : roots) {
            scanRoot(root, matcher, resourcesByPath);
        }
        if (resourcesByPath.isEmpty()) {
            throw new IOException("No classpath template resources matched pattern: " + normalizedPattern);
        }

        List<Resource> resources = new ArrayList<>(resourcesByPath.values());
        resources.sort(Comparator.comparing(Resource::path));
        validateUniqueNames(resources);
        return resources;
    }

    /**
     * Loads one exact classpath resource.
     *
     * @param path exact resource path
     * @return loaded resource
     * @throws IOException if the resource is missing or cannot be read
     */
    public static Resource loadClasspathResource(String path) throws IOException {
        String normalizedPath = normalizeClasspathPattern(path);
        if (containsGlob(normalizedPath)) {
            throw new IllegalArgumentException("Exact classpath resource path cannot contain a glob: " + path);
        }
        return loadExactClasspathResource(normalizedPath);
    }

    private static Resource loadExactClasspathResource(String path) throws IOException {
        for (ClassLoader loader : effectiveClassLoaders()) {
            try (InputStream stream = loader.getResourceAsStream(path)) {
                if (stream != null) {
                    return new Resource(
                            path,
                            fileName(path),
                            IOUtils.read(new InputStreamReader(stream, StandardCharsets.UTF_8)));
                }
            }
        }
        throw new IOException("Template classpath resource not found: " + path);
    }

    private static Set<URL> classpathRoots() throws IOException {
        Set<URL> roots = new LinkedHashSet<>();
        for (ClassLoader loader : effectiveClassLoaders()) {
            addUrlClassLoaderRoots(loader, roots);
            try {
                Enumeration<URL> enumerated = loader.getResources("");
                while (enumerated.hasMoreElements()) {
                    roots.add(enumerated.nextElement());
                }
            } catch (IOException ignored) {
                // Other class loaders and the system classpath may still expose the resource.
            }
        }
        addSystemClasspathRoots(roots);
        return roots;
    }

    private static void addUrlClassLoaderRoots(ClassLoader loader, Set<URL> roots) {
        ClassLoader current = loader;
        Set<ClassLoader> visited = new LinkedHashSet<>();
        while (current != null && visited.add(current)) {
            if (current instanceof URLClassLoader) {
                for (URL url : ((URLClassLoader) current).getURLs()) {
                    roots.add(url);
                }
            }
            current = current.getParent();
        }
    }

    private static void addSystemClasspathRoots(Set<URL> roots) {
        String classpath = System.getProperty("java.class.path");
        if (classpath == null || classpath.isEmpty()) {
            return;
        }
        for (String entry : classpath.split(File.pathSeparator)) {
            if (entry.isEmpty()) {
                continue;
            }
            try {
                roots.add(Paths.get(entry).toAbsolutePath().normalize().toUri().toURL());
            } catch (IOException ignored) {
                // Invalid classpath entries are ignored; other roots may still contain the resource.
            }
        }
    }

    private static void scanRoot(URL root, PathMatcher matcher, Map<String, Resource> resources) throws IOException {
        if ("file".equals(root.getProtocol())) {
            scanFileRoot(root, matcher, resources);
        } else if ("jar".equals(root.getProtocol())) {
            scanJarRoot(root, matcher, resources);
        }
    }

    private static void scanFileRoot(URL root, PathMatcher matcher, Map<String, Resource> resources)
            throws IOException {
        Path path;
        try {
            path = Paths.get(root.toURI());
        } catch (URISyntaxException | IllegalArgumentException e) {
            throw new IOException("Invalid classpath URL: " + root, e);
        }
        if (Files.isDirectory(path)) {
            Path normalizedRoot = path.toAbsolutePath().normalize();
            try (Stream<Path> paths = Files.walk(normalizedRoot)) {
                for (Path candidate : paths.filter(Files::isRegularFile).collect(Collectors.toList())) {
                    String resourcePath = normalizePath(normalizedRoot.relativize(candidate));
                    if (matcher.matches(Paths.get(resourcePath))) {
                        addResource(resources, new Resource(
                                resourcePath,
                                candidate.getFileName().toString(),
                                Files.readString(candidate, StandardCharsets.UTF_8)));
                    }
                }
            }
        } else if (Files.isRegularFile(path)) {
            scanJarFile(path, matcher, resources);
        }
    }

    private static void scanJarRoot(URL root, PathMatcher matcher, Map<String, Resource> resources)
            throws IOException {
        try {
            JarURLConnection connection = (JarURLConnection) root.openConnection();
            connection.setUseCaches(false);
            try (JarFile jarFile = connection.getJarFile()) {
                scanJar(jarFile, matcher, resources);
            }
        } catch (ClassCastException e) {
            throw new IOException("Unsupported classpath URL: " + root, e);
        }
    }

    private static void scanJarFile(Path path, PathMatcher matcher, Map<String, Resource> resources)
            throws IOException {
        if (!path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar")) {
            return;
        }
        try (JarFile jarFile = new JarFile(path.toFile())) {
            scanJar(jarFile, matcher, resources);
        }
    }

    private static void scanJar(JarFile jarFile, PathMatcher matcher, Map<String, Resource> resources)
            throws IOException {
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            String entryName = entry.getName();
            if (matcher.matches(Paths.get(entryName))) {
                try (InputStream stream = jarFile.getInputStream(entry)) {
                    addResource(resources, new Resource(
                            entryName,
                            fileName(entryName),
                            IOUtils.read(new InputStreamReader(stream, StandardCharsets.UTF_8))));
                }
            }
        }
    }

    private static void addResource(Map<String, Resource> resources, Resource resource) {
        resources.putIfAbsent(resource.path(), resource);
    }

    private static void validateUniqueNames(List<Resource> resources) throws IOException {
        Map<String, String> pathsByName = new LinkedHashMap<>();
        for (Resource resource : resources) {
            String previous = pathsByName.putIfAbsent(resource.name(), resource.path());
            if (previous != null && !previous.equals(resource.path())) {
                throw new IOException("Duplicate template file name '" + resource.name()
                        + "' for resources: " + previous + " and " + resource.path());
            }
        }
    }

    private static void validateDirectory(Path directory) throws IOException {
        if (directory == null) {
            throw new IllegalArgumentException("Template directory cannot be null");
        }
        if (!Files.exists(directory)) {
            throw new IOException("Template directory not found: " + directory.toAbsolutePath());
        }
        if (!Files.isDirectory(directory)) {
            throw new IOException("Path is not a directory: " + directory.toAbsolutePath());
        }
    }

    private static PathMatcher pathMatcher(String glob) {
        if (glob == null || glob.isBlank()) {
            throw new IllegalArgumentException("Template glob cannot be null or empty");
        }
        try {
            List<PathMatcher> matchers = new ArrayList<>();
            for (String variant : expandDoubleStarDirectories(glob)) {
                matchers.add(FileSystems.getDefault().getPathMatcher("glob:" + variant));
            }
            return path -> matchers.stream().anyMatch(matcher -> matcher.matches(path));
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid template glob: " + glob, e);
        }
    }

    private static List<String> expandDoubleStarDirectories(String glob) {
        Set<String> variants = new LinkedHashSet<>();
        variants.add(glob);
        boolean changed;
        do {
            changed = false;
            for (String variant : new ArrayList<>(variants)) {
                int index = variant.indexOf("**/");
                while (index >= 0) {
                    String withoutZeroDirectories = variant.substring(0, index) + variant.substring(index + 3);
                    if (variants.add(withoutZeroDirectories)) {
                        changed = true;
                    }
                    index = variant.indexOf("**/", index + 3);
                }
            }
        } while (changed);
        return new ArrayList<>(variants);
    }

    private static String normalizeClasspathPattern(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            throw new IllegalArgumentException("Classpath pattern cannot be null or empty");
        }
        String normalized = pattern.replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Classpath pattern cannot be empty");
        }
        for (String segment : normalized.split("/")) {
            if ("..".equals(segment)) {
                throw new IllegalArgumentException("Classpath pattern cannot contain '..': " + pattern);
            }
        }
        return normalized;
    }

    private static boolean containsGlob(String path) {
        for (String metaCharacter : GLOB_META_CHARACTERS) {
            if (path.contains(metaCharacter)) {
                return true;
            }
        }
        return false;
    }

    private static List<ClassLoader> effectiveClassLoaders() {
        List<ClassLoader> loaders = new ArrayList<>();
        addClassLoader(loaders, Thread.currentThread().getContextClassLoader());
        addClassLoader(loaders, TemplateResourceLoader.class.getClassLoader());
        addClassLoader(loaders, ClassLoader.getSystemClassLoader());
        return loaders;
    }

    private static void addClassLoader(List<ClassLoader> loaders, ClassLoader loader) {
        if (loader != null && !loaders.contains(loader)) {
            loaders.add(loader);
        }
    }

    private static String normalizePath(Path path) {
        return path.toString().replace(File.separatorChar, '/');
    }

    private static String fileName(String path) {
        int slash = path.lastIndexOf('/');
        return slash < 0 ? path : path.substring(slash + 1);
    }

    /**
     * A loaded template resource.
     */
    public static final class Resource {

        private final String path;
        private final String name;
        private final String content;

        private Resource(String path, String name, String content) {
            this.path = path;
            this.name = name;
            this.content = content;
        }

        public String path() {
            return path;
        }

        public String name() {
            return name;
        }

        public String content() {
            return content;
        }
    }
}
