# Overview

This is a 3D survival horror game being developed by me and a few friends.

# Video

<iframe width="699" height="393" src="https://www.youtube.com/embed/wbCgHfwIKBs" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

# Required Libraries
- [LWJGL2] https://www.lwjgl.org/
- [JOML] https://github.com/JOML-CI/JOML
- [Slick2D] http://slick.ninjacave.com/
- [JOrbis] http://www.jcraft.com/jorbis/
- [Ini4j] http://ini4j.sourceforge.net/
- [JSquish] https://github.com/memo33/jsquish

# Compilation
Include the required libraies in your project's build path, as well as the source code to this project. Be sure to include LWJGL's natives as well.

# Running the Project
Check the releases for compiled builds, unzip the contents into a folder, and run the jar file. Should any issues arise, such as the program terminating immediately on startup, you can trace the cause of the problem by running this batch file in the same directory as the jar instead of running the jar directly:

```
java -jar (JARNAME).jar
@pause
```
