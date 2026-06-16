# VeloPacketImpl

Packet implementations for Velocity (1.18.2-26.2)

## Getting started
[![VeloPacketImpl](https://repo.skyblocksquad.de/api/badge/latest/repo/de/timongcraft/VeloPacketImpl?name=Version&filter=none:SNAPSHOT)](https://repo.skyblocksquad.de/#/repo/de/timongcraft/VeloPacketImpl)

Note: You can find the current version [here](https://repo.skyblocksquad.de/#/repo/de/timongcraft/VeloPacketImpl).

### Maven

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version><version></version>
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
        <url>https://repo.skyblocksquad.de/repo</url>
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

### Gradle KTS

  ```kotlin
plugins {
    // version can be found here: https://plugins.gradle.org/plugin/com.gradleup.shadow
    id("com.gradleup.shadow") version "<version>"
}

repositories {
    maven {
        url = uri("https://repo.skyblocksquad.de/repo")
    }
}

dependencies {
    // version can be found here: https://repo.skyblocksquad.de/#/repo/de/timongcraft/VeloPacketImpl
    implementation("de.timongcraft:VeloPacketImpl:<version>")
}

shadowJar {
    // Replace 'com.yourpackage' with the package of your plugin 
    relocate("de.timongcraft.velopacketimpl", "com.yourpackage.shadow.velopacketimpl")
}
  ```