package com.saicone.settings;

import com.saicone.settings.data.DataFormat;
import com.saicone.settings.data.DataType;
import com.saicone.settings.node.ListNode;
import com.saicone.settings.node.MapNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.function.Supplier;

public class SettingsData<T extends SettingsNode> {

    // Parameters
    private final DataType dataType;
    private final String path;
    private final Supplier<T> nodeSupplier;

    // Mutable parameters
    private String format;
    private SettingsData<T> optional;
    private SettingsSource source;
    private T loaded;

    // Cached provided parameters
    private boolean optionalSupply = true;
    private File parentFolder;
    private ClassLoader parentClassLoader;

    @NotNull
    public static SettingsData<Settings> of(@NotNull String path) {
        return of(DataType.of(path), path);
    }

    @NotNull
    public static SettingsData<Settings> of(@NotNull DataType dataType, @NotNull String path) {
        return new SettingsData<>(dataType, path, Settings::new);
    }

    public SettingsData(@NotNull DataType dataType, @NotNull String path, @NotNull Supplier<T> nodeSupplier) {
        this.dataType = dataType;
        this.nodeSupplier = nodeSupplier;

        final int i = path.lastIndexOf('?');
        final int index = Math.max(path.lastIndexOf('.'), i);
        if (index > 0) {
            this.format = DataFormat.getFormat(path.substring(index + 1));
            if (i == index) {
                this.path = path.substring(0, i);
                return;
            }
        }
        this.path = path;
    }

    @NotNull
    @Contract("_ -> this")
    public SettingsData<T> or(@NotNull SettingsData<T> optional) {
        this.optional = optional;
        return this;
    }

    @NotNull
    @Contract("_, _ -> this")
    public SettingsData<T> or(@NotNull DataType dataType, @NotNull String path) {
        return or(new SettingsData<>(dataType, path, nodeSupplier));
    }

    @NotNull
    @Contract("_ -> this")
    public SettingsData<T> source(SettingsSource source) {
        this.source = source;
        return this;
    }

    @NotNull
    public DataType getDataType() {
        return dataType;
    }

    @NotNull
    public String getPath() {
        return path;
    }

    @NotNull
    public Supplier<T> getNodeSupplier() {
        return nodeSupplier;
    }

    @Nullable
    public String getFormat() {
        return format;
    }

    @Nullable
    public SettingsData<T> getOptional() {
        return optional;
    }

    @Nullable
    public T getOptionalLoaded() {
        return optional == null ? null : optional.getLoaded();
    }

    @NotNull
    public SettingsSource getSource() {
        if (source == null) {
            source = loadSource(format);
        }
        return source;
    }

    @NotNull
    public T getLoaded() {
        return loaded;
    }

    @NotNull
    public File getFile() {
        File file = parentFolder;
        for (String s : path.split("/")) {
            file = new File(file, s);
        }
        if (file == null) {
            file = new File(path);
        }
        // Path marked with recursive search
        if (!file.exists() && path.endsWith(".*")) {
            file = file.getParentFile();
            // Check if the file type was specified or cached after search
            if (!format.equals("*")) {
                final File typeFile = new File(file, path.substring(0, path.lastIndexOf('.')) + '.' + format);
                if (typeFile.exists()) {
                    return typeFile;
                }
            }
            // Search for format types
            for (String extension : DataFormat.getExtensions()) {
                final File other = new File(file, path.substring(0, path.lastIndexOf('.')) + '.' + extension);
                if (other.exists()) {
                    file = other;
                    // Cache file format
                    this.format = extension;
                    // Update SettingsSource to the new file format
                    if (this.source != null) {
                        this.source = loadSource(extension);
                    }
                    break;
                }
            }
        }
        return file;
    }

    @NotNull
    public File getNearestFile(@NotNull String type) {
        final File file = getFile();
        if (file.getName().endsWith(".*")) {
            this.format = type;
            return new File(file.getParentFile(), file.getName().substring(0, file.getName().lastIndexOf('.')) + '.' + type);
        }
        return file;
    }

    @Nullable
    public InputStream getResourceAsStream() {
        return (parentClassLoader != null ? parentClassLoader : SettingsData.class.getClassLoader()).getResourceAsStream(path);
    }

    @NotNull
    public T load() {
        if (loaded == null) {
            loaded = nodeSupplier.get();
        } else if (loaded.isMap()) {
            loaded.asMapNode().clear();
        } else if (loaded.isList()) {
            loaded.asListNode().clear();
        }

        try {
            Reader reader;
            try {
                reader = createReader();
            } catch (IOException e) {
                if (optional == null) {
                    throw e;
                }
                if (optionalSupply) {
                    optional.saveInto(this);
                    reader = createReader();
                } else {
                    reader = optional.createReader();
                }
            }

            final MapNode node = getSource().read(reader, loaded.isMap() ? loaded.asMapNode() : new MapNode());
            if (loaded.isList()) {
                for (Map.Entry<String, SettingsNode> entry : node.getValue().entrySet()) {
                    ((ListNode) loaded).add(entry.getValue().setParent(null));
                }
            }
            return loaded;
        } catch (IOException e) {
            throw new RuntimeException("Cannot load settings node from source", e);
        }
    }

    @NotNull
    public T load(boolean optionalSupply) {
        this.optionalSupply = optionalSupply;
        return load();
    }

    @NotNull
    public T load(@NotNull File parentFolder) {
        this.parentFolder = parentFolder;
        return load();
    }

    @NotNull
    public T load(@NotNull ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
        return load();
    }

    @NotNull
    public T load(@NotNull File parentFolder, boolean optionalSupply) {
        this.parentFolder = parentFolder;
        return load(optionalSupply);
    }

    @NotNull
    public T load(@NotNull ClassLoader parentClassLoader, boolean optionalSupply) {
        this.parentClassLoader = parentClassLoader;
        return load(optionalSupply);
    }

    @NotNull
    public SettingsSource loadSource(@NotNull String type) {
        return DataFormat.getSource(type);
    }

    public void save() {
        if (loaded instanceof MapNode) {
            try (Writer writer = createWriter()) {
                source.write(writer, (MapNode) loaded);
            } catch (IOException e) {
                throw new RuntimeException("Cannot save loaded map node into source", e);
            }
        }
    }

    public void saveInto(@NotNull SettingsData<?> provider) throws IOException {
        if (provider.dataType == DataType.FILE && provider.format.equalsIgnoreCase(format)) {
            final File toFile = provider.getNearestFile(format);
            if (!toFile.getParentFile().exists()) {
                toFile.getParentFile().mkdirs();
            }
            switch (dataType) {
                case FILE:
                    final File fromFile = getFile();
                    if (fromFile.exists()) {
                        Files.copy(fromFile.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    return;
                case URL:
                    try (InputStream in = new URL(path).openStream()) {
                        Files.copy(in, toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    return;
                case INPUT_STREAM:
                    final InputStream in = getResourceAsStream();
                    if (in != null) {
                        Files.copy(in, toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    return;
                default:
                    break;
            }
            return;
        }
        try (Reader reader = createReader(); Writer writer = provider.createWriter()) {
            final MapNode node = getSource().read(reader, new MapNode());
            provider.getSource().write(writer, node);
        }
    }

    @NotNull
    public Reader createReader() throws IOException {
        switch (dataType) {
            case FILE:
                final File file = getFile();
                if (file.exists()) {
                    return new BufferedReader(new FileReader(file));
                }
                throw new IOException("Cannot create reader for unknown file");
            case URL:
                final URLConnection con = new URL(path).openConnection();
                con.addRequestProperty("User-Agent", "Mozilla/5.0");
                return new InputStreamReader(new BufferedInputStream(con.getInputStream()));
            case INPUT_STREAM:
                final InputStream in = getResourceAsStream();
                if (in != null) {
                    return new InputStreamReader(new BufferedInputStream(in));
                }
                throw new IOException("The path '" + path + "' doesn't exist as resource on parent class loader");
            default:
                throw new IOException("Cannot create writer for " + dataType.name() + " data type");
        }
    }

    @NotNull
    public Writer createWriter() throws IOException {
        if (dataType.isWriteable()) {
            if (dataType == DataType.FILE) {
                final File file = getFile();
                if (!file.exists()) {
                    file.createNewFile();
                }
                return new BufferedWriter(new FileWriter(file));
            }
        }
        throw new IOException("Cannot create writer for " + dataType.name() + " data type");
    }
}
