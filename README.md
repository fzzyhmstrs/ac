# Amethyst Core

### Library mod that powers the Amethyst Imbuement family of mods; can be used to create your very own Magic-themed Mod!
see the [wiki](https://github.com/fzzyhmstrs/ac/wiki) for more detailed information.

### Including AC in your project
Amethyst Core is stored on Modrinth, so you can easily grab it from there with the following maven and dependencies notation:

**Current Latest Versions:**

MC Version | AC Version
---|----
1.18.2 | 0.4.3+1.18.2
1.19 | 0.4.3+1.19
1.19.3 | 0.4.3+1.19.3

repositories:
```
//(build.gradle.kts)
maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
        content {
            includeGroup("maven.modrinth")
        }
    }
```
```
//(build.gradle)
maven {
        name = "Modrinth"
        url = "https://api.modrinth.com/maven"
        content {
            includeGroup "maven.modrinth"
        }
    }
```

dependencies:
```
//(build.gradle.kts)
modImplementation("maven.modrinth:amethyst-core:[VERSION]")
```
```
//(build.gradle)
modImplementation "maven.modrinth:amethyst-core:[VERSION]"
```

optional:
```
include("maven.modrinth:amethyst-core:[VERSION])
//or
include "maven.modrinth:amethyst-core:[VERSION]
```
