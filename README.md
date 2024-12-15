# VeloPacketImpl

Packet implementations for Velocity (1.18.2-1.21.4)

## Getting started
Note: You can find the current version [here](https://repo.skyblocksquad.de/#/repo/de/timongcraft/VeloPacketImpl).

### Maven

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.6.0</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <relocations>
                    <relocation>
                        <pattern>de.timongcraft.velopacketimpl</pattern>
                        <!-- Replace 'com.yourpackage' with the package of your plugin ! -->
                        <shadedPattern>com.yourpackage.velopacketimpl</shadedPattern>
                    </relocation>
                </relocations>
            </configuration>
        </plugin>
    </plugins>
</build>

<repositories>
    <repository>
        <id>skyblocksquad</id>
        <url>https://repo.skyblocksquad.de/repo<repository></url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>de.timongcraft</groupId>
        <artifactId>VeloPacketImpl</artifactId>
        <version>CURRENT_VERSION</version>
    </dependency>
</dependencies>
```

When using Maven, make sure to build directly with Maven and not with your IDE configuration (on IntelliJ IDEA: in the `Maven` tab on the right, in `Lifecycle`, use `package`).

### Gradle

```groovy
plugins {
    id 'io.github.goooler.shadow' version '8.1.8'
}

repositories {
    maven {
        url "https://repo.skyblocksquad.de/repo"
    }
}

dependencies {
    implementation 'de.timongcraft:VeloPacketImpl:CURRENT_VERSION'
}

shadowJar {
    // Replace 'com.yourpackage' with the package of your plugin 
    relocate 'de.timongcraft.velopacketimpl', 'com.yourpackage.velopacketimpl'
}
```

### Manual

Copy all files in your plugin.