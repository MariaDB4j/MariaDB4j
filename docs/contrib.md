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

To deploy the `mariaDB4j-pom-lite`, use:

    ./mvnw deploy -Pgpg -f mariaDB4j-pom-lite/pom.xml

When doing a normal release, here are a few things to do every time:

1. Make sure that the JavaDoc is clean. Check for both errors and any WARNING (until [MJAVADOC-401](http://jira.codehaus.org/browse/MJAVADOC-401)):

   ```shell
   ./mvnw license:update-file-header
   ./mvnw -Dmaven.test.skip=true package
   ```

2. Perform the release (this INCLUDES an `./mvnw deploy`), and **keep staring at Terminal, to remember when touch is required for signing:**

   ```shell
   git checkout main
   git pull origin
   git push vorburger
   
   # TODO Next time, to save GPG during prepare, try: ./mvnw -DskipTests -Darguments=-DskipTests clean release:clean release:prepare && ./mvnw -DskipTests -Darguments=-DskipTests release:perform -Pgpg
   ./mvnw -DskipTests -Darguments=-DskipTests clean release:clean release:prepare release:perform -Pgpg
   
   git push origin mariaDB4j-3.3.0

   gcob version-next
   gpv
   # Merge PR like https://github.com/MariaDB4j/MariaDB4j/pull/1266
   git pull
   git reset --hard origin/main
   ```
   
   There is a bit of a mess between the `origin` and the `vorburger` remote; but like this,
   it actually works BETTER - because on the MariaDB4j org `main` there is push protection, which interferes.
   (I guess I could temporarily disable it for the occasional releases, but that having to do that seems a bit stupid.)

4. Later go to e.g. https://github.com/MariaDB4j/MariaDB4j/releases/tag/mariaDB4j-3.3.0,
   click _Create Release from tag,_ set the _Release title_ to a version number like `3.3.0`,
   choose the PREVIOUS (!) tag and _Generate release notes,_ and at least briefly order it (like previous one),
   then copy/paste that into [CHANGELOG.md](../CHANGELOG.md) Release Notes, incl. set today's date.

BTW: https://central.sonatype.com/publishing/deployments shows deployment progress - it's SLOW!

In case of any problems: Discard and go back to fix something and re-release:

   ```shell
   ./mvnw release:clean
   git reset --hard origin/main
   git push vorburger --force
   git tag -d mariaDB4j-3.3.0
   git push vorburger :mariaDB4j-3.3.0
   git push origin :mariaDB4j-3.3.0
   ```

PS: The `~/.m2/settings.xml` needs to have a `<server>` [with valid credentials](https://central.sonatype.org/publish/publish-portal-maven/#credentials), of course; see `P/m2/settings.xml`.
