What?
=====

_Please :star: Star on GitHub and **support me 🫶 through [a Tidelift subscription](https://tidelift.com)** to ensure active maintenance!_

MariaDB4j is a Java (!) "launcher" for [MariaDB](http://mariadb.org) (the "backward compatible, drop-in replacement of the MySQL® Database Server", see [Wikipedia](http://en.wikipedia.org/wiki/MariaDB)), allowing you to use MariaDB (MySQL®) from Java without ANY installation / external dependencies.  Read again: You do NOT have to have MariaDB binaries installed on your system to use MariaDB4j!

<span class="badge-paypal"><a href="https://www.paypal.me/MichaelVorburgerCH?locale.x=en_US" title="Donate to this project using Paypal"><img src="https://img.shields.io/badge/paypal-donate-yellow.svg" alt="PayPal donate button" /></a></span>
[![Patreon me!](https://img.shields.io/badge/patreon-donate-yellow.svg)](https://www.patreon.com/vorburger)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/ch.vorburger.mariaDB4j/mariaDB4j/badge.svg)](https://maven-badges.herokuapp.com/maven-central/ch.vorburger.mariaDB4j/mariaDB4j)
[![Javadocs](http://www.javadoc.io/badge/ch.vorburger.mariaDB4j/mariaDB4j-core.svg)](http://www.javadoc.io/doc/ch.vorburger.mariaDB4j/mariaDB4j-core)
[![JitPack](https://jitpack.io/v/vorburger/MariaDB4j.svg)](https://jitpack.io/#vorburger/MariaDB4j)
[![Build Status](https://app.travis-ci.com/vorburger/MariaDB4j.svg?branch=main)](https://app.travis-ci.com/vorburger/MariaDB4j)

How? (Java)
----
The MariaDB native binaries are in the MariaDB4j-DB-win*/linux*/mac*.JARs on which the main MariaDB4j JAR depends on by Maven transitive dependencies and, by default, are extracted from the classpath to a temporary base directory on the fly, then started by Java.

An example of this can be found in the source tree, in [`MariaDB4jSampleTutorialTest.java`](https://github.com/vorburger/MariaDB4j/blob/main/mariaDB4j/src/test/java/ch/vorburger/mariadb4j/tests/MariaDB4jSampleTutorialTest.java).  Basically, you can simply:

1. Install the database with a particular configuration, using short-cut:
   ```java
   DB db = DB.newEmbeddedDB(3306);
   ```
2. (Optional) The data directory will, by default, be in a temporary directory too, and will automatically get scratched at every restart; this
is suitable for integration tests.  If you use MariaDB4j for something more permanent (maybe an all-in-one application package?),
then you can simply specify a more durable location of your data directory in the `DBConfiguration`, like so:
   ```java
   DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
   configBuilder.setPort(3306); // OR, default: setPort(0); => autom. detect free port
   configBuilder.setDataDir("/home/theapp/db"); // just an example
   DB db = DB.newEmbeddedDB(configBuilder.build());
   ```
3. Start the database
   ```java
   db.start();
   ```
4. Use the database as per standard JDBC usage. In this example, you're acquiring a JDBC `Connection` from the
`DriverManager`; note that you could easily configure this URL
to be used in any JDBC connection pool. MySQL uses a `test` database by default,
and a `root` user with no password is also a default.
   ```java
   Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/test", "root", "");
   ```

   A similar suitable JDBC URL as String can normally also be directly obtained directly from the MariaDB4j API, if you prefer (this is especially useful for tests if you let MariaDB4j automatically choose a free port, in which case a hard-coded URL is problematic):

   ```java
   Connection conn = DriverManager.getConnection(configBuilder.getURL(dbName), "root", "");
   ```

5. If desired, load data from a SQL resource, located in the classpath:
   ```java
   db.source("path/to/resource.sql");
   ```

If you would like to / need to start a specific DB version you already have, instead of the version currently
packaged in the JAR, you can use `DBConfigurationBuilder setUnpackingFromClasspath(false) & setBaseDir("/my/db/")` or `-DmariaDB4j.unpack=false -DmariaDB4j.baseDir=/home/you/stuff/myFavouritemMariadDBVersion`.   Similarly, you can also pack your own version in a JAR and put it on the classpath, and `@Override getBinariesClassPathLocation()` in `DBConfigurationBuilder` to return where to find it (check the source of the default implementation).

How (using existing native MariaDB binaries)
----
MariaDB4j supports using existing native MariaDB binaries on the host system rather than unpacking MariaDB from the
classpath. This is useful if you need a newer version than is currently distributed, [or e.g. for Mac M1/M2](https://github.com/vorburger/MariaDB4j/issues/497). You can control this via the `DBConfigurationBuilder`:

```java
import static ch.vorburger.mariadb4j.DBConfiguration.Executable.Server;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
config.setPort(0); // 0 => autom. detect free port
config.setUnpackingFromClasspath(false);
config.setLibDir(System.getProperty("java.io.tmpdir") + "/MariaDB4j/no-libs");

// On Linux it may be necessary to set both the base dir and the server executable
// as the `mysqld` binary lives in `/usr/sbin` rather than `/usr/bin`
config.setBaseDir("/usr");
config.setExecutable(Server, "/usr/sbin/mysqld");

// On MacOS with MariaDB installed via homebrew, you can just set base dir to the output of `brew --prefix`
config.setBaseDir("/usr/local") // or "/opt/homebrew" for M1 Macs
```

How (Spring)
----
MariaDB4j can be used in any Java Application on its own. It is not dependent on dependency injection or the Spring Framework (the dependency to the spring-core*.jar is for a utility, and is unrelated to DI).

If you want to use MariaDB4j with Spring-boot the opinionated presets for spring applications, then you can easily use this the ready-made MariaDB4jSpringService to reduce your coding/configuration to get you going, we have an example application ([mariaDB4j-app](<https://github.com/vorburger/MariaDB4j/blob/main/mariaDB4j-app/>)) which illustrates how to wire it up or as an alternative approach via the [MariaDB4jSpringServiceTestSpringConfiguration](<https://github.com/vorburger/MariaDB4j/blob/main/mariaDB4j/src/test/java/ch/vorburger/mariadb4j/tests/springframework/MariaDB4jSpringServiceTestSpringConfiguration.java>).

The DataSource initialization have to wait until MariaDB is ready to receive connections, so we provide `mariaDB4j-springboot` to implement it. You can use it by :

```
dependencies {
   testCompile("ch.vorburger.mariaDB4j:mariaDB4j-springboot:2.6.0")
}
```

In the module, bean name of MariaDB4jSpringService is mariaDB4j, and dataSource depends on it by name. So if you want to customize your mariaDB4j, please make sure the name is correctly.

In [issue #64](<https://github.com/vorburger/MariaDB4j/issues/64>) there is also a discussion about it and pointing to a TestDbConfig.java gist.


How (CLI)
----
Because the MariaDB4j JAR is executable, you can also quickly fire up a database from a command line interface:
```
java [-DmariaDB4j.port=3718] [-DmariaDB4j.baseDir=/home/theapp/bin/mariadb4j] [-DmariaDB4j.dataDir=/home/theapp/db] -jar mariaDB4j-app*.jar
```

Note the use of the special mariaDB4j-app*.jar for this use-case, its a fat/shaded/über-JAR, based on a Spring Boot launcher.

Where from?
-----------

MariaDB4j JAR binaries are available from:

1. Maven central (see [Issue 21](https://github.com/vorburger/MariaDB4j/issues/21))

```xml
<dependency>
    <groupId>ch.vorburger.mariaDB4j</groupId>
    <artifactId>mariaDB4j</artifactId>
    <version>2.6.0</version>
</dependency>
```

2. https://jitpack.io: [main-SNAPSHOT](https://jitpack.io/#vorburger/MariaDB4j/main-SNAPSHOT), [releases](https://jitpack.io/#vorburger/MariaDB4j), see also [issue #41 discussion](https://github.com/vorburger/MariaDB4j/issues/41)

Up to version 2.1.3 MariaDB4j was on bintray.  Starting with version 2.2.1 we’re only using Maven central  The 2.2.1 that is on Bintray is broken.

For bleeding edge SNAPSHOT versions, you (or your build server) can easily build it yourself from
source; just git clone this and then mvn install or deploy. -- MariaDB4j's Maven then coordinates are:

```xml
<dependency>
    <groupId>ch.vorburger.mariaDB4j</groupId>
    <artifactId>mariaDB4j</artifactId>
    <version>2.6.1-SNAPSHOT</version>
</dependency>
```

If you use your own packaged versions of MariaDB native binaries, then the mariaDB4j-core artifact JAR,
which contains only the launcher Java code but no embedded native binaries, will be more suitable for you.

Similarly, you could also exclude one of artifacts of the currently 3 packaged OS platform to save download if your project / community is mono-platform.

You could also override the version(s) of the respective (transitive) mariaDB4j-db dependency to downgrade it, and should so be able to use the latest mariaDB4j-core & app artifact JARs even with older versions of the JAR archives containing the native mariaDB executables etc. if your project for some reason is stuck on a fixed DB version, but wants to get the latest MariaDB4j.

[Release Notes are in CHANGES.md](CHANGES.md).

Why?
----
Being able to start a database without any installation / external dependencies
is useful in a number of scenarios, such as all-in-one application packages,
or for running integration tests without depending on the installation,
set-up and up-and-running of an externally managed server.
You could also use this easily run some DB integration tests in parallel but completely isolated,
as the MariaDB4j API explicitly support this.

Java developers frequently use pure Java databases such as H2, hsqldb (HyperSQL), Derby / JavaDB for this purpose.
This library brings the advantage of the installation-free DB approach, while maintaining MariaDB (and thus MySQL) compatibility.

Who's using it?
---------------

MariaDB4j was initially developed for use in Mifos, the "Open Source Technology that accelerates Microfinance", see http://mifos.org. Coincidentally, OpenMRS the "Open Source Medical Records System" (see http://openmrs.org), another Humanitarian Open Source (HFOSS) project, also uses MariaDB4j (see https://github.com/vorburger/MariaDB4j/pull/1).

See the [`USERS.md`](USERS.md) file (also included in each built JAR!) for a list of publicly known users.

Do send a PR adding your name/organization to `USERS.md` to show your appreciation for this free project!


Maven Plugin Info (mariadb4j-maven-plugin)
-----------------

####
Maven plugin that starts and stops a MariaDB instance for the integration test phase.

This is a Maven plugin wrapper around https://github.com/vorburger/MariaDB4j, a
helpful tool for launching MariaDB from Java.

See pom and integration test in https://github.com/vorburger/MariaDB4j/tree/mariaDB4j-maven-plugin/mariaDB4j-maven-plugin/src/it/mariadb4j-maven-plugin-test-basic  for usage example.

#### Example usage

An example usage of this plugin is to install and start a database at the start of the integration test phase, and stop and uninstall the database afterwards.

This is done by configuring the plugin to execute the `start` goal in the `pre-integration-test` phase and the `stop` goal in the `post-integration-test` phase:

```xml
<plugin>
  <groupId>ch.vorburger.mariaDB4j</groupId>
  <artifactId>mariaDB4j-maven-plugin</artifactId>
  ...
  <executions>
    <execution>
      <id>pre-integration-test</id>
      <goals>
        <goal>start</goal>
      </goals>
    </execution>
    <execution>
      <id>post-integration-test</id>
      <goals>
        <goal>stop</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

This will ensure there is a MariaDB instance running on a random port, and expose the database URL as a Maven Project property.

To access the database in your integration tests, you can pass the database URL as system property to your integration tests:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-failsafe-plugin</artifactId>
  ...
  <configuration>
    <systemProperties>
      <mariadb.databaseurl>${mariadb4j.databaseurl}</mariadb.databaseurl>
    </systemProperties>
  </configuration>
</plugin>
```

#### How to upgrade the maven plugin from mike10004 version to this version

To upgrade from mike10004 to vorbuger version please change

```xml
<plugin>
    <groupId>com.github.mike10004</groupId>
    <artifactId>mariadb4j-maven-plugin</artifactId>
    ...
</plugin>
```

to

```xml
<plugin>
    <groupId>ch.vorburger.mariaDB4j</groupId>
    <artifactId>mariaDB4j-maven-plugin</artifactId>
    ...
</plugin>
```
If you are using the argument "createDatabase" rename it to "databaseName"


JUnit Integration
-----------------

Using the JUnit feature of [Rules](https://github.com/junit-team/junit4/wiki/rules) a MariaDB4JRule class is available to be used in your tests.

Add it as a `@Rule` to your test class
```
public class TestClass {
    @Rule
    public MariaDB4jRule dbRule = new MariaDB4jRule(0); //port 0 means select random available port

    @Test
    public void test() {
        // Do whatever you want with the running DB
    }
}
```

The `MariaDB4jRule` provides 2 methods for getting data on the running DB:

* getURL() - Get the JDBC connection string to the running DB
  ```
  @Test
  public void test() {
      Connection conn = DriverManager.getConnection(dbRule.getURL(), "root", "");
  }
  ```
* getDBConfiguration() - Get the Configuration object of the running DB, exposing properties such as DB Port, Data directory, Lib Directory and even a reference to the ProcessListener for the DB process.
  ```
  public class TestClass {
    @Rule
    public MariaDB4jRule dbRule = new MariaDB4jRule(3307);

    @Test
    public void test() {
        assertEquals(3307, dbRule.getDBConfiguration().getPort());
    }
  }

  ```
  
The `MariaDB4jRule` class extends the JUnit [`ExternalResource`](https://github.com/junit-team/junit4/wiki/rules#externalresource-rules) - which means it starts the DB process before each test method is run, and stops it at the end of that test method.

The `MariaDB4jRule(DBConfiguration dbConfiguration, String dbName, String resource)` Constructor, allows to initialize your DB with a provided SQL Script (resource = path to script file) to setup needed database, tables and data.

This rule, can also be used as a [@ClassRule](https://github.com/junit-team/junit4/wiki/rules#classrule) to avoid DB Process starting every test - just make sure to clean/reset your data in the DB.

Anything else?
--------------

Security nota bene: Per default, the MariaDB4j `install()` creates a new DB with a 'root' user without a password.
It also creates a database called "test".

More generally, note that if you are using the provided mariadb database Maven artifacts, then you are pulling platform specific native binaries which will be executed on your system from a remote repository, not just regular Java JARs with classes running in the JVM, through this project.  If you are completely security paranoid, this may worry you (or someone else in your organization).  If that is the case, note that you could still use only the mariadb4j-core artifact from this project, but use a JAR file containing the binaries which you have created and deployed to your organization's Maven repository yourself.  Alternatively, you also use mariadb4j-core to launch and control mariadb binaries installed by other means, e.g. an OS package manager, or perhaps in a (Docker) Container image.  This project's sweet spot and main original intended usage scenario is for integration tests, development environments, and possibly simple all-in-one evaluation kind of packages. It's NOT recommended for serious production environments with security awareness and hot fix patch-ability requirements.


MariaDB database JARs, and version upgrades
-------------------------------------------

The original creator and current maintainer of this library (@vorburger) will gladly merge any pull request contributions with updates to the MariaDB native binaries.  If you raise a change with new versions, you will be giving back to other users of the community, in exchange for being able to use this free project - that's how open-source works.

Any issues raised in the GitHub bug tracker about requesting new versions of MariaDB native binaries will be tagged with the "helpwanted" label, asking for contributions from YOU or others - ideally from the person raising the issue, but perhaps from someone else, some.. other time, later.  (But if you are reading this, YOU should contribute through a Pull Request!)

Note that the Maven <version> number of the core/app/pom artifacts versus the db artifacts, while originally the same, are now intentionally decoupled for this reason.  So your new DBs/mariaDB4j-db-(linux/mac/win)(64/32)-VERSION/pom.xml should have its Maven <version> matching the new mariadb binary you are contributing (probably like 10.1.x or so), and not the MariaDB4j launcher (which is like 2.x.y).

In addition to the new directory, you then need to correspondingly increase: 1. the `version` of the `dependency` in the mariaDB4j/pom.xml (non-root)  &  2. the databaseVersion in the DBConfigurationBuilder class.  Please have a look for contributions made by others in the git log if in doubt; e.g. [issue 37](https://github.com/vorburger/MariaDB4j/issues/37).   Please TEST your pull request on your platform!  @vorburger will only only run the build on Linux, not Windows and Mac OS X.  As the DBs jars are separated from the main project, one needs to build the DB JAR so it ends it up in your local repo first: cd down the DBs subfolder and do a mvn clean install for the DB you want to build i.e. mariaDB4j-db-mac64-10.1.9/ . After that, up again to the project root repository and mvn clean install should work fine.

So when you contribute new MariaDB native binaries versions, place them in a new directory named mariaDB4j-db-PLATFORM-VERSION under the DBs/ directory - next to the existing ones.  This is better than renaming an existing one and replacing files, because (in theory) if someone wanted to they could then easily still depend on earlier released database binary versions just by changing the <dependency> of the mariaDB4j-db* artifactId in their own project's pom.xml, even with using later version of MariaDB4j Java classes (mariadb4j core & app).

Of course, even if we would replace existing version with new binaries (like it used to originally be done in the project), then the ones already deployed to Maven central would remain there.  However it is just much easier to see which version are available, and to locally build JARs for older versions, if all are simply kept in the head `main` branch (even if not actively re-built anymore, other than the latest version).  The size of the git repository will gradually grow through this, and slightly more than if we would replace existing binaries (because git uses delta diffs, for both text and binary files).  We just accept that in this project - for clarity & convenience.


FAQ
---
Q: Is MariaDB4j stable enough for production? I need the data to be safe, and performant.
A: Try it out, and if you do find any problem, raise an issue here and let's see if we can fix it. You probably don't risk much in terms of data to be safe and performance - remember MariaDB4j is just a wrapper launching MariaDB (which is a MySQL(R) fork) - so it's as safe and performant as the underlying native DB it uses.

Q: ERROR ch.vorburger.exec.ManagedProcess - mysql: /tmp/MariaDB4j/base/bin/mysql: error while loading shared libraries: libncurses.so.5: cannot open shared object file: No such file or directory
A: This could happen e.g. on Fedora 24 if you have not previous installed any other software package which requires libncurses, and can be fixed by finding the RPM package which provides `libncurses.so.5` via `sudo dnf provides libncurses.so.5` and then install that via `sudo dnf install ncurses-compat-libs`. On Ubuntu Focal 20.04, you need to `sudo apt update && sudo apt install libncurses5`.

Q: Is there another project that does something similar to this one?
A: Indeed there is, check out [wix-embedded-mysql](https://github.com/wix/wix-embedded-mysql)! The world is big enough for both of us, and [we cross link](https://github.com/wix/wix-embedded-mysql/pull/118).  [Testcontainers' has something similar which we recommend you use ](https://www.testcontainers.org/modules/databases/mariadb/) if you can run containers (Docker).  Also OpenMRS' [liquibase-maven-plugin](https://github.com/openmrs/openmrs-contrib-liquibase-maven-plugin) build on MariaDB4j.


Release?
--------

Remember that mariaDB4j-pom-lite & DBs/mariaDB4j-db-* are now versioned non SNAPSHOT, always fixed; VS the rest that continues to be a 2.2.x-SNAPSHOT (as before).  All the steps below except the last one only apply at the root pom.xml (=mariaDB4j-pom) with is mariaDB4j-core, mariaDB4j & mariaDB4j-app <modules>.  The mariaDB4j-pom-lite & DBs/mariaDB4j-db-* with their manually maintained fixed <version> however are simply deployed manually with a direct mvn deploy as shown in the last step.

When doing a release, here are a few things to do every time:

1. update the Maven version numbers in this README

1. update the dependencies to the latest 3rd-party libraries & Maven plug-in versions available.

2. Make sure the project builds, without pulling anything which should be part of this build from outside:

       mvn clean package; rm -rf ~/.m2/repository/ch/vorburger/mariaDB4j; mvn clean package

3. Make to sure that the JavaDoc is clean.  Check for both errors and any WARNING (until [MJAVADOC-401](http://jira.codehaus.org/browse/MJAVADOC-401)):

   ```shell
   mvn license:update-file-header
   mvn -Dmaven.test.skip=true package
   ```

4. Finalize [CHANGELOG.md](CHANGELOG.md) Release Notes, incl. set today's date, and update the version numbers in this README.

5. Preparing & performing the release (this INCLUDES an mvn deploy):

   ```shell
   mvn release:prepare
   mvn release:perform -Pgpg
   mvn release:clean
   ```

6. Deploy to Maven central, only for the mariaDB4j-pom-lite & DBs/mariaDB4j-db projects:

       mvn clean deploy -Pgpg

In case of any problems: Discard and go back to fix something and re-release e.g. using EGit via Rebase Interactive on the commit before "prepare release" and skip the two commits made by the maven-release-plugin. Use git push --force to remote, and remove local tag using git tag -d mariaDB4j-2.x.y, and remote tag using 'git push origin :mariaDB4j-2.x.y'. (Alternatively try BEFORE release:clean use 'mvn release:rollback', but that leaves ugly commits.)


Who?
----

See the [CONTRIBUTORS.md](CONTRIBUTORS.md) file (also included in each built JAR!) for a list of contributors.

Latest/current also on https://github.com/vorburger/MariaDB4j/graphs/contributors:

Contributions, patches, forks more than welcome - hack it, and add your name! ;-)
