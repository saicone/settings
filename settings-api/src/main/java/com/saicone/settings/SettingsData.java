package com.saicone.settings;

import com.saicone.settings.data.DataFormat;
import com.saicone.settings.data.DataType;
import com.saicone.settings.node.ListNode;
import com.saicone.settings.node.MapNode;
import com.saicone.settings.util.Strings;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Core class to load any settings node from data.<br>
 * <br>
 * This is a class that unifies a {@link SettingsSource} as data reader and
 * a node constructor to load the data into it.
 *
 * @author Rubenicos
 *
 * @param <T> the loadable settings node type
 */
public class SettingsData<T extends SettingsNode> {

    /**
     * A constant to represent an unknown path.
     */
    public static final String UNKNOWN_PATH = "<unknown>";
    /**
     * Default user agent to read from url.
     */
    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    /**
     * Create a {@link SettingsData} with a provided data path.<br>
     * The data type will be resolved using data path format.
     *
     * @param dataPath the path to data source
     * @return         a newly generated settings data
     */
    @NotNull
    public static SettingsData<Settings> of(@NotNull String dataPath) {
        final DataType dataType = DataType.of(dataPath);
        String s = dataPath;
        for (String alias : dataType.getAliases()) {
            if (s.replace(' ', '_').toLowerCase().startsWith(alias + ':')) {
                s = s.substring(alias.length() + 1);
                break;
            }
        }
        return of(dataType, s);
    }

    /**
     * Create a {@link SettingsData} with a provided data type and data path.<br>
     * The data path depends on data type.
     *
     * @param dataType the type representing the kind of data source
     * @param dataPath the path to data source
     * @return         a newly generated settings data
     */
    @NotNull
    public static SettingsData<Settings> of(@NotNull DataType dataType, @NotNull String dataPath) {
        return of(dataType, dataPath, Settings::new);
    }

    /**
     * Create a {@link SettingsData} with a provided data type, data path and a node constructor.<br>
     * The data path depends on data type.
     *
     * @param dataType     the type representing the kind of data source
     * @param dataPath     the path to data source
     * @param nodeSupplier the supplier that provides a settings node to load data into it
     * @return             a newly generated settings data
     * @param <T> the loadable settings node type
     */
    @NotNull
    public static <T extends SettingsNode> SettingsData<T> of(@NotNull DataType dataType, @NotNull String dataPath, @NotNull Supplier<T> nodeSupplier) {
        String format = null;

        final int i = dataPath.lastIndexOf('?');
        final int index = Math.max(dataPath.lastIndexOf('.'), i);
        if (index > 0) {
            format = DataFormat.getFormat(dataPath.substring(index + 1));
            if (i == index) {
                dataPath = dataPath.substring(0, i);
            }
        }

        final SettingsData<T> data = new SettingsData<>(dataType, dataPath, nodeSupplier);
        if (format != null) {
            data.format = format;
        }
        return data;
    }

    /**
     * Create a {@link SettingsData} using a {@link Path} to load data from.
     *
     * @param path the path containing settings data
     * @return     a newly generated settings data
     */
    @NotNull
    public static SettingsData<Settings> file(@NotNull Path path) {
        return file(path.toFile(), Settings::new);
    }

    /**
     * Create a {@link SettingsData} using a {@link Path} to load data from.
     *
     * @param path         the path containing settings data
     * @param nodeSupplier the supplier that provides a settings node to load data into it
     * @return             a newly generated settings data
     * @param <T> the loadable settings node type
     */
    @NotNull
    public static <T extends SettingsNode> SettingsData<T> file(@NotNull Path path, @NotNull Supplier<T> nodeSupplier) {
        return file(path.toFile(), nodeSupplier);
    }

    /**
     * Create a {@link SettingsData} using a {@link File} to load data from.
     *
     * @param file the file containing settings data
     * @return     a newly generated settings data
     */
    @NotNull
    public static SettingsData<Settings> file(@NotNull File file) {
        return file(file, Settings::new);
    }

    /**
     * Create a {@link SettingsData} using a {@link File} to load data from.
     *
     * @param file         the file containing settings data
     * @param nodeSupplier the supplier that provides a settings node to load data into it
     * @return             a newly generated settings data
     * @param <T> the loadable settings node type
     */
    @NotNull
    public static <T extends SettingsNode> SettingsData<T> file(@NotNull File file, @NotNull Supplier<T> nodeSupplier) {
        return new SettingsData<>(DataType.FILE, file.getPath(), nodeSupplier) {
            @Override
            public @NotNull File getFile() {
                return file;
            }
        };
    }

    /**
     * Create a {@link SettingsData} using a {@link InputStream} to load data from.
     *
     * @param input the input stream to load data from
     * @return      a newly generated settings data
     */
    @NotNull
    public static SettingsData<Settings> stream(@NotNull InputStream input) {
        return stream(input, Settings::new);
    }

    /**
     * Create a {@link SettingsData} using a {@link InputStream} to load data from.
     *
     * @param input        the input stream to load data from
     * @param nodeSupplier the supplier that provides a settings node to load data into it
     * @return             a newly generated settings data
     * @param <T> the loadable settings node type
     */
    @NotNull
    public static <T extends SettingsNode> SettingsData<T> stream(@NotNull InputStream input, @NotNull Supplier<T> nodeSupplier) {
        return new SettingsData<>(DataType.INPUT_STREAM, UNKNOWN_PATH, nodeSupplier) {
            @Override
            public @NotNull InputStream getResourceAsStream() {
                return input;
            }
        };
    }

    /**
     * Create a {@link SettingsData} using a {@link InputStream} supplier to load data from.
     *
     * @param input the input stream to load data from
     * @return      a newly generated settings data
     */
    @NotNull
    public static SettingsData<Settings> stream(@NotNull Supplier<InputStream> input) {
        return stream(input, Settings::new);
    }

    /**
     * Create a {@link SettingsData} using a {@link InputStream} supplier to load data from.
     *
     * @param input        the input stream to load data from
     * @param nodeSupplier the supplier that provides a settings node to load data into it
     * @return             a newly generated settings data
     * @param <T> the loadable settings node type
     */
    @NotNull
    public static <T extends SettingsNode> SettingsData<T> stream(@NotNull Supplier<InputStream> input, @NotNull Supplier<T> nodeSupplier) {
        return new SettingsData<>(DataType.INPUT_STREAM, UNKNOWN_PATH, nodeSupplier) {
            @Override
            public @NotNull InputStream getResourceAsStream() {
                return input.get();
            }
        };
    }

    // Parameters
    private final DataType dataType;
    private final String dataPath;
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
     * Constructs a settings data instance with provided parameters.
     *
     * @param dataType     the type representing the kind of data source
     * @param dataPath     the path to data source
     * @param nodeSupplier the supplier that provides a settings node to load data into it
     */
    public SettingsData(@NotNull DataType dataType, @NotNull String dataPath, @NotNull Supplier<T> nodeSupplier) {
        this.dataType = dataType;
        this.dataPath = dataPath;
        this.nodeSupplier = nodeSupplier;
    }

    /**
     * Set the optional settings data instance to provide a node in case this data path doesn't exist.
     *
     * @param optional the optional settings data
     * @return         this settings data instance
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
     * @param dataType the data type
     * @param path     the data path
     * @return         this settings data instance
     */
    @NotNull
    @Contract("_, _ -> this")
    public SettingsData<T> or(@NotNull DataType dataType, @NotNull String path) {
        return or(of(dataType, path, nodeSupplier));
    }

    /**
     * Set the settings source that will be used to read or write any settings data.
     *
     * @param source the settings source to read and write data
     * @return       this settings data instance
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
     * @param loaded the current settings node
     * @return       this settings data instance
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
     * @param optionalSupply true to save optional data in the current data path
     * @return               this settings data instance
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
     * @param parentFolder the parent folder
     * @return             this settings data instance
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
     * @param parentClassLoader the parent class loader
     * @return                  this settings data instance
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
     * @return a data type
     */
    @NotNull
    public DataType getDataType() {
        return dataType;
    }

    /**
     * Get the path this data come from.
     *
     * @return a data path
     */
    @NotNull
    public String getDataPath() {
        return dataPath;
    }

    /**
     * Get the current node supplier that build loadable settings node type.
     *
     * @return a node type supplier
     */
    @NotNull
    public Supplier<T> getNodeSupplier() {
        return nodeSupplier;
    }

    /**
     * Get the current data format.
     *
     * @return a data format type
     */
    @Nullable
    public String getFormat() {
        return format;
    }

    /**
     * Get the current optional settings data.
     *
     * @return a optional settings data if is set, null otherwise
     */
    @Nullable
    public SettingsData<T> getOptional() {
        return optional;
    }

    /**
     * Get the optional loaded settings node instance.
     *
     * @return an optional settings node if is loaded, null otherwise
     */
    @Nullable
    public T getOptionalLoaded() {
        return optional == null ? null : optional.getLoaded();
    }

    /**
     * Get the current settings source of load a new one.
     *
     * @return a settings source instance
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
     * @return a settings node
     */
    @NotNull
    public T getLoaded() {
        if (loaded == null) {
            loaded = nodeSupplier.get();
        }
        return loaded;
    }

    /**
     * Get effective settings file using current path and parent folder.
     *
     * @return a file containing settings node data
     */
    @NotNull
    public File getFile() {
        if (dataType == DataType.FILE_RESOURCE) {
            return new File(this.getClass().getResource(dataPath).getFile());
        }
        File file = parentFolder;
        for (String s : Strings.split(dataPath, '/')) {
            file = new File(file, s);
        }
        if (file == null) {
            file = new File(dataPath);
        }
        // Path marked with recursive search
        if (!file.exists() && dataPath.endsWith(".*")) {
            file = file.getParentFile();
            // Check if the file type was specified or cached after search
            if (!format.equals("*")) {
                final File typeFile = new File(file, dataPath.substring(0, dataPath.lastIndexOf('.')) + '.' + format);
                if (typeFile.exists()) {
                    return typeFile;
                }
            }
            // Search for format types
            for (String extension : DataFormat.getExtensions()) {
                final File other = new File(file, dataPath.substring(0, dataPath.lastIndexOf('.')) + '.' + extension);
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
     * @param type the data format type
     * @return     a file containing settings node data
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
     * @return a input stream containing settings node data
     */
    @Nullable
    public InputStream getResourceAsStream() throws IOException {
        switch (dataType) {
            case FILE:
            case FILE_RESOURCE:
                return new FileInputStream(getFile());
            case URL:
                final URLConnection connection = new URL(dataPath).openConnection();
                connection.addRequestProperty("User-Agent", DEFAULT_USER_AGENT);

                if (connection instanceof HttpURLConnection) {
                    final HttpURLConnection http = (HttpURLConnection) connection;

                    final int responseCode = http.getResponseCode();
                    if (responseCode < 200 || responseCode >= 300) {
                        throw new IOException("HTTP " + responseCode + " while connecting with " + dataPath + " (" + http.getResponseMessage() + ")");
                    }
                }

                return connection.getInputStream();
            case INPUT_STREAM:
                return (parentClassLoader != null ? parentClassLoader : SettingsData.class.getClassLoader()).getResourceAsStream(dataPath);
            default:
                throw new UnsupportedOperationException("Unsupported data type: " + dataType);
        }
    }

    /**
     * Load the current settings data into node.
     *
     * @return the loaded settings node
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

        try (Reader reader = createReader0()) {
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
     * @param optionalSupply true to save optional data in the current data path
     * @return               the loaded settings node
     */
    @NotNull
    public T load(boolean optionalSupply) {
        this.optionalSupply = optionalSupply;
        return load();
    }

    /**
     * Load the current settings data into node.
     *
     * @param parentFolder the parent folder
     * @return             the loaded settings node
     */
    @NotNull
    public T load(@NotNull File parentFolder) {
        this.parentFolder = parentFolder;
        return load();
    }

    /**
     * Load the current settings data into node.
     *
     * @param parentClassLoader the parent class loader
     * @return                  the loaded settings node
     */
    @NotNull
    public T load(@NotNull ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
        return load();
    }

    /**
     * Load the current settings data into node.
     *
     * @param parentFolder   the parent folder
     * @param optionalSupply true to save optional data in the current data path
     * @return               the loaded settings node
     */
    @NotNull
    public T load(@NotNull File parentFolder, boolean optionalSupply) {
        this.parentFolder = parentFolder;
        return load(optionalSupply);
    }

    /**
     * Load the current settings data into node.
     *
     * @param parentClassLoader the parent class loader
     * @param optionalSupply    true to save optional data in the current data path
     * @return                  the loaded settings node
     */
    @NotNull
    public T load(@NotNull ClassLoader parentClassLoader, boolean optionalSupply) {
        this.parentClassLoader = parentClassLoader;
        return load(optionalSupply);
    }

    /**
     * Create a settings source from provided source type id.
     *
     * @param type the source type
     * @return     a settings source
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
            getSource().write(writer, node);
        } catch (IOException e) {
            throw new RuntimeException("Cannot save loaded map node into source", e);
        }
    }

    /**
     * Save settings data into provider.
     *
     * @param provider the provider to save data in
     * @throws IOException if any error occurs while data is transferred
     */
    public void saveInto(@NotNull SettingsData<?> provider) throws IOException {
        if (provider.format.equals("*")) {
            provider.format = format;
        }
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
     * @return a reader for the settings data
     * @throws IOException if any error occurs on reader creation
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
            case INPUT_STREAM:
                InputStream in = getResourceAsStream();
                if (in != null) {
                    if (!(in instanceof BufferedInputStream)) {
                        in = new BufferedInputStream(in);
                    }
                    return new InputStreamReader(in);
                }
                throw new IOException("The data path '" + dataPath + "' doesn't exist as resource");
            default:
                throw new IOException("Cannot create writer for " + dataType.name() + " data type");
        }
    }

    @NotNull
    private Reader createReader0() throws IOException {
        try {
            return createReader();
        } catch (IOException e) {
            if (optional == null) {
                throw e;
            }
            if (optionalSupply) {
                optional.saveInto(this);
                return createReader();
            } else {
                return optional.createReader();
            }
        }
    }

    /**
     * Create a writer depending on data type.
     *
     * @return a writer for the settings data
     * @throws IOException if any error occurs on writer creation
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
