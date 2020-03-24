//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.travja.townybridge.util;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.reflect.Reflection;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

@Beta
public final class ClassPath {
    private static final Logger logger = Logger.getLogger(ClassPath.class.getName());
    private static final Predicate<ClassPath.ClassInfo> IS_TOP_LEVEL = new Predicate<ClassPath.ClassInfo>() {
        public boolean apply(ClassPath.ClassInfo info) {
            return info.className.indexOf(36) == -1;
        }
    };
    private static final Splitter CLASS_PATH_ATTRIBUTE_SEPARATOR = Splitter.on(" ").omitEmptyStrings();
    private static final String CLASS_FILE_NAME_EXTENSION = ".class";
    private final ImmutableSet<ClassPath.ResourceInfo> resources;

    private ClassPath(ImmutableSet<ClassPath.ResourceInfo> resources) {
        this.resources = resources;
    }

    public static ClassPath from(ClassLoader classloader) throws IOException {
        ClassPath.DefaultScanner scanner = new ClassPath.DefaultScanner();
        scanner.scan(classloader);
        return new ClassPath(scanner.getResources());
    }

    public ImmutableSet<ClassPath.ResourceInfo> getResources() {
        return this.resources;
    }

    public ImmutableSet<ClassPath.ClassInfo> getAllClasses() {
        return FluentIterable.from(this.resources).filter(ClassPath.ClassInfo.class).toSet();
    }

    public ImmutableSet<ClassPath.ClassInfo> getTopLevelClasses() {
        return FluentIterable.from(this.resources).filter(ClassPath.ClassInfo.class).filter(IS_TOP_LEVEL).toSet();
    }

    public ImmutableSet<ClassPath.ClassInfo> getTopLevelClasses(String packageName) {
        Preconditions.checkNotNull(packageName);
        Builder<ClassPath.ClassInfo> builder = ImmutableSet.builder();
        UnmodifiableIterator var3 = this.getTopLevelClasses().iterator();

        while(var3.hasNext()) {
            ClassPath.ClassInfo classInfo = (ClassPath.ClassInfo)var3.next();
            if (classInfo.getPackageName().equals(packageName)) {
                builder.add(classInfo);
            }
        }

        return builder.build();
    }

    public ImmutableSet<ClassPath.ClassInfo> getTopLevelClassesRecursive(String packageName) {
        Preconditions.checkNotNull(packageName);
        String packagePrefix = packageName + '.';
        Builder<ClassPath.ClassInfo> builder = ImmutableSet.builder();
        UnmodifiableIterator var4 = this.getTopLevelClasses().iterator();

        while(var4.hasNext()) {
            ClassPath.ClassInfo classInfo = (ClassPath.ClassInfo)var4.next();
            if (classInfo.getName().startsWith(packagePrefix)) {
                builder.add(classInfo);
            }
        }

        return builder.build();
    }

    @VisibleForTesting
    static String getClassName(String filename) {
        int classNameEnd = filename.length() - ".class".length();
        return filename.substring(0, classNameEnd).replace('/', '.');
    }

    @VisibleForTesting
    static File toFile(URL url) {
        Preconditions.checkArgument(url.getProtocol().equals("file"));

        try {
            return new File(url.toURI());
        } catch (URISyntaxException var2) {
            return new File(url.getPath());
        }
    }

    @VisibleForTesting
    static final class DefaultScanner extends ClassPath.Scanner {
        private final SetMultimap<ClassLoader, String> resources = MultimapBuilder.hashKeys().linkedHashSetValues().build();

        DefaultScanner() {
        }

        ImmutableSet<ClassPath.ResourceInfo> getResources() {
            Builder<ClassPath.ResourceInfo> builder = ImmutableSet.builder();
            Iterator var2 = this.resources.entries().iterator();

            while(var2.hasNext()) {
                Entry<ClassLoader, String> entry = (Entry)var2.next();
                builder.add(ClassPath.ResourceInfo.of((String)entry.getValue(), (ClassLoader)entry.getKey()));
            }

            return builder.build();
        }

        protected void scanJarFile(ClassLoader classloader, JarFile file) {
            Enumeration entries = file.entries();

            while(entries.hasMoreElements()) {
                JarEntry entry = (JarEntry)entries.nextElement();
                if (!entry.isDirectory() && !entry.getName().equals("META-INF/MANIFEST.MF")) {
                    this.resources.get(classloader).add(entry.getName());
                }
            }

        }

        protected void scanDirectory(ClassLoader classloader, File directory) throws IOException {
            Set<File> currentPath = new HashSet();
            currentPath.add(directory.getCanonicalFile());
            this.scanDirectory(directory, classloader, "", currentPath);
        }

        private void scanDirectory(File directory, ClassLoader classloader, String packagePrefix, Set<File> currentPath) throws IOException {
            File[] files = directory.listFiles();
            if (files == null) {
                ClassPath.logger.warning("Cannot read directory " + directory);
            } else {
                File[] var6 = files;
                int var7 = files.length;

                for(int var8 = 0; var8 < var7; ++var8) {
                    File f = var6[var8];
                    String name = f.getName();
                    if (f.isDirectory()) {
                        File deref = f.getCanonicalFile();
                        if (currentPath.add(deref)) {
                            this.scanDirectory(deref, classloader, packagePrefix + name + "/", currentPath);
                            currentPath.remove(deref);
                        }
                    } else {
                        String resourceName = packagePrefix + name;
                        if (!resourceName.equals("META-INF/MANIFEST.MF")) {
                            this.resources.get(classloader).add(resourceName);
                        }
                    }
                }

            }
        }
    }

    abstract static class Scanner {
        private final Set<File> scannedUris = Sets.newHashSet();

        Scanner() {
        }

        public final void scan(ClassLoader classloader) throws IOException {
            UnmodifiableIterator var2 = getClassPathEntries(classloader).entrySet().iterator();

            while(var2.hasNext()) {
                Entry<File, ClassLoader> entry = (Entry)var2.next();
                this.scan((File)entry.getKey(), (ClassLoader)entry.getValue());
            }

        }

        @VisibleForTesting
        final void scan(File file, ClassLoader classloader) throws IOException {
            if (this.scannedUris.add(file.getCanonicalFile())) {
                this.scanFrom(file, classloader);
            }

        }

        protected abstract void scanDirectory(ClassLoader var1, File var2) throws IOException;

        protected abstract void scanJarFile(ClassLoader var1, JarFile var2) throws IOException;

        private void scanFrom(File file, ClassLoader classloader) throws IOException {
            try {
                if (!file.exists()) {
                    return;
                }
            } catch (SecurityException var4) {
                ClassPath.logger.warning("Cannot access " + file + ": " + var4);
                return;
            }

            if (file.isDirectory()) {
                this.scanDirectory(classloader, file);
            } else {
                this.scanJar(file, classloader);
            }

        }

        private void scanJar(File file, ClassLoader classloader) throws IOException {
            JarFile jarFile;
            try {
                jarFile = new JarFile(file);
            } catch (IOException var13) {
                return;
            }

            try {
                UnmodifiableIterator var4 = getClassPathFromManifest(file, jarFile.getManifest()).iterator();

                while(var4.hasNext()) {
                    File path = (File)var4.next();
                    this.scan(path, classloader);
                }

                this.scanJarFile(classloader, jarFile);
            } finally {
                try {
                    jarFile.close();
                } catch (IOException var12) {
                }

            }
        }

        @VisibleForTesting
        static ImmutableSet<File> getClassPathFromManifest(File jarFile, @NullableDecl Manifest manifest) {
            if (manifest == null) {
                return ImmutableSet.of();
            } else {
                Builder<File> builder = ImmutableSet.builder();
                String classpathAttribute = manifest.getMainAttributes().getValue(Name.CLASS_PATH.toString());
                if (classpathAttribute != null) {
                    Iterator var4 = ClassPath.CLASS_PATH_ATTRIBUTE_SEPARATOR.split(classpathAttribute).iterator();

                    while(var4.hasNext()) {
                        String path = (String)var4.next();

                        URL url;
                        try {
                            url = getClassPathEntry(jarFile, path);
                        } catch (MalformedURLException var8) {
                            ClassPath.logger.warning("Invalid Class-Path entry: " + path);
                            continue;
                        }

                        if (url.getProtocol().equals("file")) {
                            builder.add(ClassPath.toFile(url));
                        }
                    }
                }

                return builder.build();
            }
        }

        @VisibleForTesting
        static ImmutableMap<File, ClassLoader> getClassPathEntries(ClassLoader classloader) {
            LinkedHashMap<File, ClassLoader> entries = Maps.newLinkedHashMap();
            ClassLoader parent = classloader.getParent();
            if (parent != null) {
                entries.putAll(getClassPathEntries(parent));
            }

            UnmodifiableIterator var3 = getClassLoaderUrls(classloader).iterator();

            while(var3.hasNext()) {
                URL url = (URL)var3.next();
                if (url.getProtocol().equals("file")) {
                    File file = ClassPath.toFile(url);
                    if (!entries.containsKey(file)) {
                        entries.put(file, classloader);
                    }
                }
            }

            return ImmutableMap.copyOf(entries);
        }

        private static ImmutableList<URL> getClassLoaderUrls(ClassLoader classloader) {
            if (classloader instanceof URLClassLoader) {
                return ImmutableList.copyOf(((URLClassLoader)classloader).getURLs());
            } else {
                return classloader.equals(ClassLoader.getSystemClassLoader()) ? parseJavaClassPath() : ImmutableList.of();
            }
        }

        @VisibleForTesting
        static ImmutableList<URL> parseJavaClassPath() {
            com.google.common.collect.ImmutableList.Builder<URL> urls = ImmutableList.builder();
            Iterator var1 = Splitter.on(StandardSystemProperty.PATH_SEPARATOR.value()).split(StandardSystemProperty.JAVA_CLASS_PATH.value()).iterator();

            while(var1.hasNext()) {
                String entry = (String)var1.next();

                try {
                    try {
                        urls.add((new File(entry)).toURI().toURL());
                    } catch (SecurityException var4) {
                        urls.add(new URL("file", (String)null, (new File(entry)).getAbsolutePath()));
                    }
                } catch (MalformedURLException var5) {
                    ClassPath.logger.log(Level.WARNING, "malformed classpath entry: " + entry, var5);
                }
            }

            return urls.build();
        }

        @VisibleForTesting
        static URL getClassPathEntry(File jarFile, String path) throws MalformedURLException {
            return new URL(jarFile.toURI().toURL(), path);
        }
    }

    @Beta
    public static final class ClassInfo extends ClassPath.ResourceInfo {
        private final String className;

        ClassInfo(String resourceName, ClassLoader loader) {
            super(resourceName, loader);
            this.className = ClassPath.getClassName(resourceName);
        }

        public String getPackageName() {
            return Reflection.getPackageName(this.className);
        }

        public String getSimpleName() {
            int lastDollarSign = this.className.lastIndexOf(36);
            String packageName;
            if (lastDollarSign != -1) {
                packageName = this.className.substring(lastDollarSign + 1);
                return CharMatcher.digit().trimLeadingFrom(packageName);
            } else {
                packageName = this.getPackageName();
                return packageName.isEmpty() ? this.className : this.className.substring(packageName.length() + 1);
            }
        }

        public String getName() {
            return this.className;
        }

        public Class<?> load() {
            try {
                return this.loader.loadClass(this.className);
            } catch (ClassNotFoundException var2) {
                throw new IllegalStateException(var2);
            }
        }

        public String toString() {
            return this.className;
        }
    }

    @Beta
    public static class ResourceInfo {
        private final String resourceName;
        final ClassLoader loader;

        static ClassPath.ResourceInfo of(String resourceName, ClassLoader loader) {
            return (ClassPath.ResourceInfo)(resourceName.endsWith(".class") ? new ClassPath.ClassInfo(resourceName, loader) : new ClassPath.ResourceInfo(resourceName, loader));
        }

        ResourceInfo(String resourceName, ClassLoader loader) {
            this.resourceName = (String)Preconditions.checkNotNull(resourceName);
            this.loader = (ClassLoader)Preconditions.checkNotNull(loader);
        }

        public final URL url() {
            URL url = this.loader.getResource(this.resourceName);
            if (url == null) {
                throw new NoSuchElementException(this.resourceName);
            } else {
                return url;
            }
        }

        public final ByteSource asByteSource() {
            return Resources.asByteSource(this.url());
        }

        public final CharSource asCharSource(Charset charset) {
            return Resources.asCharSource(this.url(), charset);
        }

        public final String getResourceName() {
            return this.resourceName;
        }

        public int hashCode() {
            return this.resourceName.hashCode();
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof ClassPath.ResourceInfo)) {
                return false;
            } else {
                ClassPath.ResourceInfo that = (ClassPath.ResourceInfo)obj;
                return this.resourceName.equals(that.resourceName) && this.loader == that.loader;
            }
        }

        public String toString() {
            return this.resourceName;
        }
    }
}
