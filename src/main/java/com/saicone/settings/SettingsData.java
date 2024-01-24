package com.saicone.settings;

import com.saicone.settings.data.DataFormat;
import com.saicone.settings.data.DataType;
import com.saicone.settings.node.ListNode;
import com.saicone.settings.node.MapNode;
import com.saicone.settings.util.Strings;
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

/**
 * Class to load any settings node from data.
 *
 * @author Rubenicos
 *
 * @param <T> the loadable settings node type.
 */
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

    /**
     * Create a settings data instance that load {@link Settings} object from provided data path.
     *
     * @param path the data path, normally a file path.
     * @return     a settings data loader.
     */
    @NotNull
    public static SettingsData<Settings> of(@NotNull String path) {
        final DataType dataType = DataType.of(path);
        String s = path;
        for (String alias : dataType.getAliases()) {
            if (s.replace(' ', '_').toLowerCase().startsWith(alias + ':')) {
                s = s.substring(alias.length() + 1);
                break;
            }
        }
        return of(dataType, s);
    }

    /**
     * Create a settings data instance that load {@link Settings} object from provided path with data type.
     *
     * @param dataType the data type.
     * @param path     the data path.
     * @return         a settings data loader.
     */
    @NotNull
    public static SettingsData<Settings> of(@NotNull DataType dataType, @NotNull String path) {
        return new SettingsData<>(dataType, path, Settings::new);
    }

    /**
     * Constructs a settings data instance with provided parameters.
     *
     * @param dataType     the data type.
     * @param path         the data path.
     * @param nodeSupplier the supplier to build loadable settings node type.
     */
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

    /**
     * Set the optional settings data instance to provide a node in case this data path doesn't exist.
     *
     * @param optional the optional settings data.
     * @return         this settings data instance.
     */
    @NotNull
    @Contract("_ -> this")
    public SettingsData<T> or(@NotNull SettingsData<T> optional) {
        this.optional = optional;
        return this;
    }

    /**
     * Set the optional settings data to provide a node in case this data path doesn't exist.
     *
     * @param dataType the data type.
     * @param path     the data path.
     * @return         this settings data instance.
     */
    @NotNull
    @Contract("_, _ -> this")
    public SettingsData<T> or(@NotNull DataType dataType, @NotNull String path) {
        return or(new SettingsData<>(dataType, path, nodeSupplier));
    }

    /**
     * Set the settings source that will be used to read or write any settings data.
     *
     * @param source the settings source to read and write data.
     * @return       this settings data instance.
     */
    @NotNull
    @Contract("_ -> this")
    public SettingsData<T> source(SettingsSource source) {
        this.source = source;
        return this;
    }

    /**
     * Set the loaded settings node from data.
     *
     * @param loaded the current settings node.
     * @return       this settings data instance.
     */
    @NotNull
    @Contract("_ -> this")
    public SettingsData<T> loaded(@NotNull T loaded) {
        this.loaded = loaded;
        return this;
    }

    /**
     * Set the optional supply state in this class.
     *
     * @param optionalSupply true to save optional data in the current data path.
     * @return               this settings data instance.
     */
    @NotNull
    @Contract("_ -> this")
    public SettingsData<T> optionalSupply(boolean optionalSupply) {
        this.optionalSupply = optionalSupply;
        return this;
    }

    /**
     * Set the parent folder to load this data from.
     *
     * @param parentFolder the parent folder.
     * @return             this settings data instance.
     */
    @NotNull
    @Contract("_ -> this")
    public SettingsData<T> parentFolder(@Nullable File parentFolder) {
        this.parentFolder = parentFolder;
        return this;
    }

    /**
     * Set the parent class loader to load this data from.
     *
     * @param parentClassLoader the parent class loader.
     * @return                  this settings data instance.
     */
    @NotNull
    @Contract("_ -> this")
    public SettingsData<T> parentClassLoader(@Nullable ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
        return this;
    }

    /**
     * Get the type this data come from.
     *
     * @return a data type.
     */
    @NotNull
    public DataType getDataType() {
        return dataType;
    }

    /**
     * Get the path this data come from.
     *
     * @return a data path.
     */
    @NotNull
    public String getPath() {
        return path;
    }

    /**
     * Get the current node supplier that build loadable settings node type.
     *
     * @return a node type supplier.
     */
    @NotNull
    public Supplier<T> getNodeSupplier() {
        return nodeSupplier;
    }

    /**
     * Get the current data format.
     *
     * @return a data format type.
     */
    @Nullable
    public String getFormat() {
        return format;
    }

    /**
     * Get the current optional settings data.
     *
     * @return a optional settings data if is set, null otherwise.
     */
    @Nullable
    public SettingsData<T> getOptional() {
        return optional;
    }

    /**
     * Get the optional loaded settings node instance.
     *
     * @return an optional settings node if is loaded, null otherwise.
     */
    @Nullable
    public T getOptionalLoaded() {
        return optional == null ? null : optional.getLoaded();
    }

    /**
     * Get the current settings source of load a new one.
     *
     * @return a settings source instance.
     */
    @NotNull
    public SettingsSource getSource() {
        if (source == null) {
            source = loadSource(format);
        }
        return source;
    }

    /**
     * Get loaded settings node instance.
     *
     * @return an settings node.
     */
    @NotNull
    public T getLoaded() {
        return loaded;
    }

    /**
     * Get effective settings file using current path and parent folder.
     *
     * @return a file containing settings node data.
     */
    @NotNull
    public File getFile() {
        if (dataType == DataType.FILE_RESOURCE) {
            return new File(this.getClass().getResource(path).getFile());
        }
        File file = parentFolder;
        for (String s : Strings.split(path, '/')) {
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

    /**
     * Get the nearest file using provided data format.
     *
     * @param type the data format type.
     * @return     a file containing settings node data.
     */
    @NotNull
    public File getNearestFile(@NotNull String type) {
        final File file = getFile();
        if (file.getName().endsWith(".*")) {
            this.format = type;
            return new File(file.getParentFile(), file.getName().substring(0, file.getName().lastIndexOf('.')) + '.' + type);
        }
        return file;
    }

    /**
     * Get effective settings stream using current path and parent class loader.
     *
     * @return a input stream containing settings node data.
     */
    @Nullable
    public InputStream getResourceAsStream() {
        return (parentClassLoader != null ? parentClassLoader : SettingsData.class.getClassLoader()).getResourceAsStream(path);
    }

    /**
     * Load the current settings data into node.
     *
     * @return the loaded settings node.
     */
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

    /**
     * Load the current settings data into node.
     *
     * @param optionalSupply true to save optional data in the current data path.
     * @return               the loaded settings node.
     */
    @NotNull
    public T load(boolean optionalSupply) {
        this.optionalSupply = optionalSupply;
        return load();
    }

    /**
     * Load the current settings data into node.
     *
     * @param parentFolder the parent folder.
     * @return             the loaded settings node.
     */
    @NotNull
    public T load(@NotNull File parentFolder) {
        this.parentFolder = parentFolder;
        return load();
    }

    /**
     * Load the current settings data into node.
     *
     * @param parentClassLoader the parent class loader.
     * @return                  the loaded settings node.
     */
    @NotNull
    public T load(@NotNull ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
        return load();
    }

    /**
     * Load the current settings data into node.
     *
     * @param parentFolder   the parent folder.
     * @param optionalSupply true to save optional data in the current data path.
     * @return               the loaded settings node.
     */
    @NotNull
    public T load(@NotNull File parentFolder, boolean optionalSupply) {
        this.parentFolder = parentFolder;
        return load(optionalSupply);
    }

    /**
     * Load the current settings data into node.
     *
     * @param parentClassLoader the parent class loader.
     * @param optionalSupply    true to save optional data in the current data path.
     * @return                  the loaded settings node.
     */
    @NotNull
    public T load(@NotNull ClassLoader parentClassLoader, boolean optionalSupply) {
        this.parentClassLoader = parentClassLoader;
        return load(optionalSupply);
    }

    /**
     * Create a settings source from provided source type id.
     *
     * @param type the source type.
     * @return     a settings source.
     */
    @NotNull
    public SettingsSource loadSource(@NotNull String type) {
        return DataFormat.getSource(type);
    }

    /**
     * Save current loaded data.
     */
    public void save() {
        final MapNode node;
        if (loaded instanceof MapNode) {
            node = (MapNode) loaded;
        } else if (loaded instanceof ListNode) {
            node = new MapNode();
            for (SettingsNode child : ((ListNode) loaded)) {
                if (child.getKey() != null) {
                    node.put(child.getKey(), child);
                }
            }
        } else {
            return;
        }
        try (Writer writer = createWriter()) {
            source.write(writer, node);
        } catch (IOException e) {
            throw new RuntimeException("Cannot save loaded map node into source", e);
        }
    }

    /**
     * Save settings data into provider.
     *
     * @param provider the provider to save data in.
     * @throws IOException if any error occurs while data is transferred.
     */
    public void saveInto(@NotNull SettingsData<?> provider) throws IOException {
        if (provider.dataType.isFile() && provider.format.equalsIgnoreCase(format)) {
            final File toFile = provider.getNearestFile(format);
            if (toFile.getParentFile() != null && !toFile.getParentFile().exists()) {
                toFile.getParentFile().mkdirs();
            }
            switch (dataType) {
                case FILE:
                case FILE_RESOURCE:
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

    /**
     * Create a reader depending on data type.
     *
     * @return a reader for the settings data.
     * @throws IOException if any error occurs on reader creation.
     */
    @NotNull
    public Reader createReader() throws IOException {
        switch (dataType) {
            case FILE:
            case FILE_RESOURCE:
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

    /**
     * Create a writer depending on data type.
     *
     * @return a writer for the settings data.
     * @throws IOException if any error occurs on writer creation.
     */
    @NotNull
    public Writer createWriter() throws IOException {
        if (dataType.isWriteable()) {
            if (dataType.isFile()) {
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
