# JAMS Modelling Framework

JAMS (originally an acronym for "Jena Adaptable Modelling System") is a
pure-Java, open-source software framework for building and applying
modular environmental simulation models. Originally developed for
eco-hydrological modelling at Friedrich Schiller University Jena
(Department of Geoinformatics), it provides the process-based simulation
components that form the basis of the J2000 model family and has been
applied worldwide for simulating hydrological, nutrient transport and
erosion processes.

Models are assembled from reusable components (process modules, I/O,
aggregation, analysis) that are wired together in an XML model definition —
either by hand or visually in the JUICE model editor. The framework takes
care of the simulation life cycle, spatial and temporal iteration, data
handling and parallel execution, and comes with tools for model setup,
calibration, application and analysis.

**Website:** https://jamsframework.org
**Documentation & Wiki:** https://jams.uni-jena.de (setup guides, feature
documentation, component wiki, release archive)

## What's in the box

| Module | Description |
|---|---|
| `jams-api` | Core API: component model, data types, annotations |
| `jams-main` | Runtime: model loader, class loading, workspaces, data stores |
| `jams-common` | Shared utilities and GUI helpers |
| `jams-components` | Standard component library (I/O, aggregation, statistics, …) |
| `jams-ui` | JUICE model editor and JAMS launcher GUI |
| `jams-explorer` | JADE data explorer for model output analysis |
| `jams-worldwind` | 3D visualization of spatial model data (NASA WorldWind) |
| `jams-optas` | OPTAS: calibration, optimization and sensitivity analysis |
| `jams-cloud-client` / `jams-cloud-core` | Client for running models on JAMS Cloud |
| `jams-starter` | Distribution module — assembles the runnable `jams-bin/` bundle |

Domain-specific component libraries (e.g. the J2000 hydrological model
family) are maintained in the separate
[jamsmodels](https://github.com/jamsframework/jamsmodels) repository.

## Requirements

**Running JAMS** only requires a Java runtime (JRE), version 17 or newer.

> **macOS note:** a bug in the Java runtime's native accessibility support
> (present in current Java 17–23 builds, fixed in Java 24) can crash the
> application when a screen reader or another macOS accessibility tool is
> active. If you run into this, use a Java 24 or newer runtime.

**Building from source** only requires a JDK, version 17 or newer
(developed and tested with JDK 21). Maven does not need to be installed —
the repository ships the Maven Wrapper (`./mvnw`, `mvnw.cmd` on Windows),
which downloads a suitable Maven version on first use.

All third-party dependencies are included in the `libs-repo/` directory,
a project-local Maven repository — the build works offline out of the box.
See [libs-repo/VERSIONS.md](libs-repo/VERSIONS.md) for what is in there.

## Building

```
./mvnw install -pl jams-starter -am
```

This builds all required modules, installs the JAMS artifacts into your
local Maven repository (where e.g. the
[jamsmodels](https://github.com/jamsframework/jamsmodels) build picks
them up) and assembles a runnable application bundle in `jams-bin/` at
the repository root:

```
jams-bin/
├── jams-starter.jar     JAMS launcher (main class jamsui.launcher.JAMSui)
├── juice-starter.jar    JUICE model editor (main class jamsui.juice.JUICE)
├── lib/                 runtime classpath
├── components/          standard component library (loaded at runtime)
├── data/                example workspaces (J2000, Thornthwaite)
└── jams.sh / juice.sh   start scripts (macOS/Linux; .bat for Windows)
```

`./mvnw clean -pl jams-starter` removes the bundle again (your `default.jap`
configuration file survives).

Building behind an HTTP proxy? See [docs/proxy.md](docs/proxy.md).

## Releasing

The version is `major.minor.patch[-SNAPSHOT]`, e.g. `4.0.8-SNAPSHOT`. The
`jams.version.major`/`jams.version.minor`/`jams.version.patch` properties
in the root `pom.xml` are the single place this is maintained —
`version.txt` is generated from them at build time.

To cut a release:

1. Remove the `-SNAPSHOT` suffix from `<revision>` in the root `pom.xml`,
   commit.
2. Tag the commit `vX.Y.Z` (matching the version) and push the tag — this
   triggers [.github/workflows/release.yml](.github/workflows/release.yml),
   which builds the bundle and publishes it as a GitHub Release.
3. Bump `jams.version.patch` and re-append `-SNAPSHOT` to `<revision>`,
   commit, to resume development.

## Running

```
cd jams-bin
./juice.sh        # model editor
./jams.sh         # launcher GUI
```

or equivalently `java -jar juice-starter.jar` / `java -jar jams-starter.jar`.

JAMS stores its configuration in `default.jap` next to the starter jars.
Additional component libraries can be added there via the semicolon-separated
`libs` property or via the settings dialog in JUICE.

## History

JAMS has been continuously developed since 2005. The complete history —
including 20 years of Subversion commits — is preserved in this repository.
Releases up to 3.17 were published via https://jams.uni-jena.de, where an
archive of historical releases remains available. Starting with this
repository, JAMS uses a Maven-based build. Previous releases were built
with Ant and ran on Java 8. This repository targets Java 17 — the version required by the
upgraded GeoTools components.

## License

JAMS is free software, licensed under the
[GNU Lesser General Public License v3](LICENSE)
(see also [COPYING.GPL](COPYING.GPL) for the GNU GPL v3 that accompanies it).
