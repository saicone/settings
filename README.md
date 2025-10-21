<h1 align="center">Settings</h1>

<h4 align="center">Java library to interpret multiple data formats as flexible configuration.</h4>

<p align="center">
    <a href="https://saic.one/discord">
        <img src="https://img.shields.io/discord/974288218839191612.svg?style=flat-square&label=discord&logo=discord&logoColor=white&color=7289da"/>
    </a>
    <a href="https://www.codefactor.io/repository/github/saicone/settings">
        <img src="https://img.shields.io/codefactor/grade/github/saicone/settings?style=flat-square&logo=codefactor&logoColor=white&label=codefactor&color=00b16a"/>
    </a>
    <a href="https://github.com/saicone/settings">
        <img src="https://img.shields.io/github/languages/code-size/saicone/settings?logo=github&logoColor=white&style=flat-square"/>
    </a>
    <a href="https://jitpack.io/#com.saicone/settings">
        <img src="https://img.shields.io/github/v/tag/saicone/settings?style=flat-square&logo=jitpack&logoColor=white&label=JitPack&color=brigthgreen"/>
    </a>
    <a href="https://javadoc.saicone.com/settings/">
        <img src="https://img.shields.io/badge/JavaDoc-Online-green?style=flat-square"/>
    </a>
    <a href="https://docs.saicone.com/settings/">
        <img src="https://img.shields.io/badge/Saicone-Settings%20Wiki-3b3bb0?logo=github&logoColor=white&style=flat-square"/>
    </a>
</p>

Settings library handle multiple data formats as configuration in a flexible way:

* Node templates and transformation.
* Node value substitution in non-compatible formats (like json and yaml).
* Fallback format reader.
* Iterable nodes.
* Data update parameters.
* Comparable paths to get nodes.
* Multi-layer node values.

Currently supporting the formats:

* [HOCON](https://github.com/lightbend/config/blob/main/HOCON.md)
* [JSON](https://www.json.org/) (with [Gson](https://github.com/google/gson))
* [TOML](https://toml.io/en/v1.0.0)
* [YAML](http://yaml.org/spec/1.1/current.html)

Take in count this library is not focused as an object serializer, the main purpose is making flexible interactions with multiple data formats at the same time.

It also has simple methods to get nodes as multiple data types if you want to implement your own object serializer.

```java
// Load settings from Map
Map<String, Object> map = new HashMap<>();
map.put("key", "value");
map.put("otherkey", 1234);

Settings settings = new Settings();
settings.merge(map);


// Settings from any file named "myfile", the format can be any supported format.
// If file doesn't exist, the optional file "myfile.json" inside .jar will be used.
SettingsData<Settings> data = SettingsData.of("myfile.*").or(DataType.INPUT_STREAM, "myfile.json");
// Load settings by providing a parent folder.
// Also, optional file will be saved inside the folder if original file doesn't exist
File folder = new File("folder");
Settings settings = data.load(folder, true);
```

## Dependency

How to use Settings library in your project.

<details>
  <summary>build.gradle</summary>

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.saicone.settings:settings:1.0.3'
    // Other modules
    implementation 'com.saicone.settings:settings-gson:1.0.3'
    implementation 'com.saicone.settings:settings-hocon:1.0.3'
    implementation 'com.saicone.settings:settings-toml:1.0.3'
    implementation 'com.saicone.settings:settings-yaml:1.0.3'
}
```

</details>

<details>
  <summary>build.gradle.kts</summary>

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.saicone.settings:settings:1.0.3")
    // Other modules
    implementation("com.saicone.settings:settings-gson:1.0.3")
    implementation("com.saicone.settings:settings-hocon:1.0.3")
    implementation("com.saicone.settings:settings-toml:1.0.3")
    implementation("com.saicone.settings:settings-yaml:1.0.3")
}
```

</details>

<details>
  <summary>pom.xml</summary>

```xml
<repositories>
    <repository>
        <id>Jitpack</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.saicone.settings</groupId>
        <artifactId>settings</artifactId>
        <version>1.0.3</version>
        <scope>compile</scope>
    </dependency>
    <!-- Other modules -->
    <dependency>
        <groupId>com.saicone.settings</groupId>
        <artifactId>settings-gson</artifactId>
        <version>1.0.3</version>
        <scope>compile</scope>
    </dependency>
    <dependency>
        <groupId>com.saicone.settings</groupId>
        <artifactId>settings-hocon</artifactId>
        <version>1.0.3</version>
        <scope>compile</scope>
    </dependency>
    <dependency>
        <groupId>com.saicone.settings</groupId>
        <artifactId>settings-toml</artifactId>
        <version>1.0.3</version>
        <scope>compile</scope>
    </dependency>
    <dependency>
        <groupId>com.saicone.settings</groupId>
        <artifactId>settings-yaml</artifactId>
        <version>1.0.3</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

</details>

## Relocated Dependency

How to implement and relocate Settings library in your project.

<details>
  <summary>build.gradle</summary>

```groovy
plugins {
    id 'com.gradleup.shadow' version '9.2.2'
}

repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.saicone.settings:settings:1.0.3'
    // Other modules
    implementation 'com.saicone.settings:settings-gson:1.0.3'
    implementation 'com.saicone.settings:settings-hocon:1.0.3'
    implementation 'com.saicone.settings:settings-toml:1.0.3'
    implementation 'com.saicone.settings:settings-yaml:1.0.3'
}

jar.dependsOn (shadowJar)

shadowJar {
    // Relocate packages (DO NOT IGNORE THIS)
    relocate 'com.saicone.types', project.group + '.libs.types'
    relocate 'com.saicone.settings', project.group + '.libs.settings'
    // Exclude unused classes (optional)
    minimize()
}
```

</details>

<details>
  <summary>build.gradle.kts</summary>

```kotlin
plugins {
    id("com.gradleup.shadow") version "9.2.2"
}

repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.saicone.settings:settings:1.0.3")
    // Other modules
    implementation("com.saicone.settings:settings-gson:1.0.3")
    implementation("com.saicone.settings:settings-hocon:1.0.3")
    implementation("com.saicone.settings:settings-toml:1.0.3")
    implementation("com.saicone.settings:settings-yaml:1.0.3")
}

tasks {
    jar {
        dependsOn(tasks.shadowJar)
    }

    shadowJar {
        // Relocate packages (DO NOT IGNORE THIS)
        relocate("com.saicone.types", "${project.group}.libs.types")
        relocate("com.saicone.settings", "${project.group}.libs.settings")
        // Exclude unused classes (optional)
        minimize()
    }
}
```

</details>

<details>
  <summary>pom.xml</summary>

```xml
<repositories>
    <repository>
        <id>Jitpack</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.saicone.settings</groupId>
        <artifactId>settings</artifactId>
        <version>1.0.3</version>
        <scope>compile</scope>
    </dependency>
    <!-- Other modules -->
    <dependency>
        <groupId>com.saicone.settings</groupId>
        <artifactId>settings-gson</artifactId>
        <version>1.0.3</version>
        <scope>compile</scope>
    </dependency>
    <dependency>
        <groupId>com.saicone.settings</groupId>
        <artifactId>settings-hocon</artifactId>
        <version>1.0.3</version>
        <scope>compile</scope>
    </dependency>
    <dependency>
        <groupId>com.saicone.settings</groupId>
        <artifactId>settings-toml</artifactId>
        <version>1.0.3</version>
        <scope>compile</scope>
    </dependency>
    <dependency>
        <groupId>com.saicone.settings</groupId>
        <artifactId>settings-yaml</artifactId>
        <version>1.0.3</version>
        <scope>compile</scope>
    </dependency>
</dependencies>

<build>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>3.6.1</version>
        <configuration>
            <relocations>
                <!-- Relocate packages (DO NOT IGNORE THIS) -->
                <relocation>
                    <pattern>com.saicone.types</pattern>
                    <shadedPattern>${project.groupId}.libs.types</shadedPattern>
                </relocation>
                <relocation>
                    <pattern>com.saicone.settings</pattern>
                    <shadedPattern>${project.groupId}.libs.settings</shadedPattern>
                </relocation>
            </relocations>
            <!-- Exclude unused classes (optional) -->
            <minimizeJar>true</minimizeJar>
        </configuration>
        <executions>
            <execution>
                <phase>package</phase>
                <goals>
                    <goal>shade</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
</build>
```

</details>

> [!NOTE]  
> This project use [Types library](https://github.com/saicone/types) to convert data types, so it's required to relocate it's package along with settings package.