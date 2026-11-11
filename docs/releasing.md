# Releasing JAMS

The version is `major.minor.patch[-SNAPSHOT]`, e.g. `4.0.8-SNAPSHOT`. The
`jams.version.major`/`jams.version.minor`/`jams.version.patch` properties
in the root `pom.xml` are the single place this is maintained —
`version.txt` is generated from them at build time.

To cut a release:

1. Start [.github/workflows/release.yml](../.github/workflows/release.yml)
   from the Actions tab ("Run workflow") with the release version `X.Y.Z`
   (empty: the version from the root `pom.xml`), the release date shown in
   the About dialog (empty: today) and the jamsmodels libraries to bundle
   (comma-separated; the default is the standard set). It builds the bundle
   with `-Drevision=X.Y.Z` (dropping the `-SNAPSHOT` suffix for that build
   only) and the given release date, adds the creators and contributors
   listed in the `.zenodo.json` files of the bundled jamsmodels libraries to
   [.zenodo.json](../.zenodo.json) (which lists the jams people and is
   maintained by hand, following the rules in
   [jamsmodels docs/zenodo.md](https://github.com/jamsframework/jamsmodels/blob/main/docs/zenodo.md)),
   tags a commit with that merged file and the release date `vX.Y.Z` and
   publishes it as a GitHub Release, with a changelog of commits since the
   previous tag. Zenodo then archives the release and assigns a DOI. The
   run's summary lists version, date, libraries and the number of Zenodo
   creators/contributors. If the jamsmodels
   libraries changed since the last jamsmodels release, the ru n ends with a
   warning: then also start the jamsmodels Release workflow.
2. Optionally bump `jams.version.patch` in the root `pom.xml` afterward and
   commit, so local/CI SNAPSHOT builds stay ahead of the last release
   (`<revision>` always carries `-SNAPSHOT` in the repository - only the
   release build's `-Drevision` override drops it).
