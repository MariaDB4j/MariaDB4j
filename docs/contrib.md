# MariaDB4j Contributor Guide

This document contains information relevant for contributors and maintainers of MariaDB4j.

## MariaDB database JARs, and version upgrades

The original creator and current maintainer of this library (@vorburger) will gladly merge any pull request contributions with updates to the MariaDB native binaries. If you raise a change with new versions, you will be giving back to other users of the community, in exchange for being able to use this free project - that's how open-source works.

Any issues raised in the GitHub bug tracker about requesting new versions of MariaDB native binaries will be tagged with the "helpwanted" label, asking for contributions from YOU or others - ideally from the person raising the issue, but perhaps from someone else, some.. other time, later. (But if you are reading this, YOU should contribute through a Pull Request!)

Note that the Maven <version> number of the core/app/pom artifacts versus the db artifacts, while originally the same, are now intentionally decoupled for this reason. So your new DBs/mariaDB4j-db-(linux/mac/win)(64/32)-VERSION/pom.xml should have its Maven <version> matching the new mariadb binary you are contributing (probably like 10.1.x or so), and not the MariaDB4j launcher (which is like 2.x.y).

In addition to the new directory, you then need to correspondingly increase: 1. the `version` of the `dependency` in the mariaDB4j/pom.xml (non-root) & 2. the databaseVersion in the DBConfigurationBuilder class. Please have a look for contributions made by others in the git log if in doubt; e.g. [issue 37](https://github.com/MariaDB4j/MariaDB4j/issues/37). Please TEST your pull request on your platform! @vorburger will only only run the build on Linux, not Windows and Mac OS X. As the DBs jars are separated from the main project, one needs to build the DB JAR so it ends it up in your local repo first: cd down the DBs subfolder and do a ./mvnw clean install for the DB you want to build i.e. mariaDB4j-db-mac64-10.1.9/ . After that, up again to the project root repository and ./mvnw clean install should work fine.

So when you contribute new MariaDB native binaries versions, place them in a new directory named mariaDB4j-db-PLATFORM-VERSION under the DBs/ directory - next to the existing ones. This is better than renaming an existing one and replacing files, because (in theory) if someone wanted to they could then easily still depend on earlier released database binary versions just by changing the `<dependency>` of the mariaDB4j-db* artifactId in their own project's pom.xml, even with using later version of MariaDB4j Java classes (mariadb4j core & app).

Of course, even if we would replace existing version with new binaries (like it used to originally be done in the project), then the ones already deployed to Maven central would remain there. However it is just much easier to see which version are available, and to locally build JARs for older versions, if all are simply kept in the head `main` branch (even if not actively re-built anymore, other than the latest version). The size of the git repository will gradually grow through this, and slightly more than if we would replace existing binaries (because git uses delta diffs, for both text and binary files). We just accept that in this project - for clarity & convenience.

## Release Process

Remember that `mariaDB4j-pom-lite` & `DBs/mariaDB4j-db-*` are now versioned non SNAPSHOT, always fixed; VS the rest that continues to be a 2.2.x-SNAPSHOT (as before). All the steps below except the last one only apply at the root pom.xml (=mariaDB4j-pom) with is mariaDB4j-core, mariaDB4j & mariaDB4j-app `<modules>`. The `mariaDB4j-pom-lite` & `DBs/mariaDB4j-db-*` with their manually maintained fixed `<version>` however are simply deployed manually with a direct ./mvnw deploy as shown in the last step.

When doing a release, here are a few things to do every time:

1. update the Maven version numbers in this README

2. update the dependencies to the latest 3rd-party libraries & Maven plug-in versions available.

3. Make sure the project builds, without pulling anything which should be part of this build from outside:

   ```shell
   ./mvnw clean package && rm -rf ~/.m2/repository/ch/vorburger && ./mvnw clean package
   ```

4. Make to sure that the JavaDoc is clean. Check for both errors and any WARNING (until [MJAVADOC-401](http://jira.codehaus.org/browse/MJAVADOC-401)):

   ```shell
   ./mvnw license:update-file-header
   ./mvnw -Dmaven.test.skip=true package
   ```

5. Finalize [CHANGELOG.md](../CHANGELOG.md) Release Notes, incl. set today's date, and update the version numbers in this README.

6. Preparing & performing the release (this INCLUDES an ./mvnw deploy):

   ```shell
   ./mvnw release:prepare
   ./mvnw release:perform -Pgpg
   ./mvnw release:clean
   ```

7. Deploy to Maven central, only for the mariaDB4j-pom-lite & DBs/mariaDB4j-db projects:

   ```shell
   ./mvnw clean deploy -Pgpg
   ```

In case of any problems: Discard and go back to fix something and re-release e.g. using EGit via Rebase Interactive on the commit before "prepare release" and skip the two commits made by the maven-release-plugin. Use git push --force to remote, and remove local tag using git tag -d mariaDB4j-2.x.y, and remote tag using 'git push origin :mariaDB4j-2.x.y'. (Alternatively try BEFORE release:clean use './mvnw release:rollback', but that leaves ugly commits.)

PS: The `~/.m2/settings.xml` needs to have a `<server>` [with valid credentials](https://github.com/vorburger/ch.vorburger.exec/issues/105), of course; see `P/m2/settings.xml`.
