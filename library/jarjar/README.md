# jarjar

JarJar Links is a utility originally developed by Google to facilitate repacking jar dependencies.
This directory contains an adaptation of a [pantsbuild](https://github.com/pantsbuild/jarjar) fork.
The project contains a convenience pre-built jar for executing jarjar locally and in CI environments.

## Building a new jar

To build a new jar execute the following from this directory:

```
./pants binary //:jarjar-main
```

The resulting jar will be available in dist/jarjar-main.jar.
