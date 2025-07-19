# ShopChest

ShopChest - Spigot/Bukkit Plugin

<!-- TOC -->
* [ShopChest](#shopchest)
  * [General information](#general-information)
  * [Usage](#usage)
    * [Why](#why)
    * [Instructions](#instructions)
  * [API](#api)
  * [Build](#build)
  * [Issues](#issues)
  * [Download](#download)
<!-- TOC -->

## General information

This fork of [ShopChest](https://github.com/EpicEricEE/ShopChest) intend to be updatable easily.
You can find below the previous and original description of this fork.
Although the current repository structure still respect what it's said in the section, goals changed.
**The current project only aims to support recent minecraft versions.**
Maybe in the future a complete rewrite will happen, but it has not started yet.

### Previous description
It also adds some common features like barrel or shulker shops.
It's still in progress and therefore, this repository has many branches.
Most of them have very explicit name, but there is two things to know:

- The 'master' branch is the original plugin, with no major changes.
  It supports the most recent version of Minecraft.
- The 'feature/clean-project' branch is a 'work-in-progress' branch.
  It contains bug fixes and adds new features.
  However, as the structure change, some plugin that were previously using ShopChest can break.
  If it's the case, just use the original version (in 'master' branch).

## Usage

To download the plugin, see the Download section below. <br>

### Why
Since 1.20.1, ShopChest use a new language system in order to avoid major changes at each minecraft release. <br>
Note that it can break support for older version.
Although it uses a minecraft chat mechanic added in 1.8, it's part of the Bukkit API since February 13 of 2023.
Backward compatibility is possible but requires more work and will be provided only if requested in the issues section. <br>
Previously, all messages were store in a single file called `<locale>.lang`.
Now each language is made of two files:
- `messages-<locale>.lang` which contains ShopChest internal messages and which will likely never change
- `items-<locale>.lang` which contains item translations used by holograms.

The second one differs a bit from previously.
As minecraft is constantly updated with new items, there is no point to provide it, it would be constantly outdated.

### Instructions

- Install the plugin in your plugin folder.
- Run the server a first time to generate each configuration files
- Set the desired locale in the configuration
- Restart the server (or process the command /shop reload) to generate local-specifics language files.
- Copy the output of [LangGenerator](https://github.com/Flowsqy/ShopChestLangGenerator) into the used items language file.
- Restart the server (or process the command /shop reload) to load the correct translations.

You may have some warnings if shops are loaded while the items file is empty.
(For example during the reload process).
It's perfectly fine, even if it says that you should report them, you can ignore it **as long as the file is empty**.
<br>
You may need to repeat this process at each minecraft update in order to be able to have the translations for the new items.

## API

To use the API, you need to add the following repository and dependency in your maven project:

```xml
<repositories>
  <repository>
    <id>shopchest-repo</id>
    <url>https://epicericee.github.io/ShopChest/maven/</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>de.epiceric</groupId>
    <artifactId>ShopChest</artifactId>
    <version>1.11.1</version>
    <scope>provided</scope>
  </dependency>
</dependencies>
```

You can find the javadoc here: https://epicericee.github.io/ShopChest/javadoc/

## Build

To compile the project, you will need jdk 21 and maven.

You also need, in order to compile nms spigot modules, to have previously run the spigot buildtools with the `--remapped` arg for each version specified in `nms/spigot/README.md`.
This requirement might force you to get other jdk version to run the buildtools for specific versions. It's only needed by the buildtools, not by ShopChest.

First, add nms interface to your maven local repository.
```batch
./gradlew nms:interface:publishToMavenLocal
```
It needs to be done at least once.
As long as there is no changes to the `nms/interface` module, you don't need to do this step again.

Then you need to compile spigot nms submodules
```batch
cd nms/spigot
mvn install
```

Finally to actually compile the plugin, return at the root of the project (`cd ../..` if you are in the same shell) and do
```batch
./gradlew plugin:shadowJar
```

After the build succeeded, the ShopChest jar is found in the `plugin/build/libs/` folder.

## Issues

If you find any issues, please provide them in the [Issues Section](https://github.com/Flowsqy/ShopChest/issues) with a
good description of how to reproduce it. If you get any error messages in the console, please also provide them.

## Download

You can download the recent builds in the [release section](https://github.com/Flowsqy/ShopChest/releases).
This resource/plugin can also be found on the official spigot
page [here](https://www.spigotmc.org/resources/shopchest.11431/), but as I don't own it, the most recent build there
only supports Minecraft up to 1.16.

