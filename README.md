# MariaDB4j <span class="badge-paypal"><a href="https://www.paypal.me/MichaelVorburgerCH?locale.x=en_US" title="Donate to this project using Paypal"><img src="https://img.shields.io/badge/paypal-donate-yellow.svg" alt="PayPal donate button" /></a></span> [![Patreon me!](https://img.shields.io/badge/patreon-donate-yellow.svg)](https://www.patreon.com/vorburger) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/ch.vorburger.mariaDB4j/mariaDB4j/badge.svg)](https://maven-badges.herokuapp.com/maven-central/ch.vorburger.mariaDB4j/mariaDB4j) [![Javadocs](http://www.javadoc.io/badge/ch.vorburger.mariaDB4j/mariaDB4j-core.svg)](http://www.javadoc.io/doc/ch.vorburger.mariaDB4j/mariaDB4j-core) [![JitPack](https://jitpack.io/v/vorburger/MariaDB4j.svg)](https://jitpack.io/#vorburger/MariaDB4j) [![Build Status](https://app.travis-ci.com/vorburger/MariaDB4j.svg?branch=main)](https://app.travis-ci.com/vorburger/MariaDB4j) [![pre-commit.ci status](https://results.pre-commit.ci/badge/github/MariaDB4j/MariaDB4j/main.svg)](https://results.pre-commit.ci/latest/github/MariaDB4j/MariaDB4j/main) [![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/MariaDB4j/MariaDB4j/badge)](https://securityscorecards.dev/viewer/?uri=github.com/MariaDB4j/MariaDB4j) [![OpenSSF Best Practices](https://www.bestpractices.dev/projects/7865/badge)](https://www.bestpractices.dev/projects/7865)

_Please :star: Star on GitHub and **💸 support [on OpenCollective](https://opencollective.com/mariadb4j), via [GitHub Sponsoring](https://github.com/sponsors/vorburger) or through [a Tidelift subscription](https://tidelift.com)** to ensure active maintenance of this project [used by hundreds](https://github.com/MariaDB4j/MariaDB4j/network/dependents), since [2011](#stars--history)! 🫶_

- [What?](#what)
- [Usage](#usage)
  - [`DB` API](#db-api)
  - [Spring](#spring)
  - [JUnit](#junit)
  - [Maven Plugin](#maven-plugin)
  - [CLI](#cli)
  - [Maven Artifacts](#maven-artifacts)
  - [Binaries](#binaries)
- [Why?](#why)
- [FAQ](#faq)
- [Related Projects](#related-projects)
- [Release Notes](#release-notes)
- [Contributors](#contributors)
- [End Users](#users)
- [Sponsors](#sponsors)

## What?

MariaDB4j is a Java (!) "launcher" for [MariaDB](http://mariadb.org) (the "backward compatible, drop-in replacement of the MySQL® Database Server", see [Wikipedia](http://en.wikipedia.org/wiki/MariaDB)), allowing you to use MariaDB (MySQL®) from Java without ANY installation / external dependencies.  Read again: You do NOT have to have MariaDB binaries installed on your system to use MariaDB4j!

## Usage

### `DB` API

The MariaDB native binaries are in the MariaDB4j-DB-win*/linux*/mac*.JARs on which the main MariaDB4j JAR depends on by Maven transitive dependencies and, by default, are extracted from the classpath to a temporary base directory on the fly, then started by Java.

An example of this can be found in the source tree, in [`MariaDB4jSampleTutorialTest.java`](https://github.com/MariaDB4j/MariaDB4j/blob/main/mariaDB4j/src/test/java/ch/vorburger/mariadb4j/tests/MariaDB4jSampleTutorialTest.java).  Basically, you can simply:

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

### Spring

MariaDB4j can be used in any Java Application on its own. It is not dependent on dependency injection or the Spring Framework (the dependency to the spring-core*.jar is for a utility, and is unrelated to DI).

If you want to use MariaDB4j with Spring-boot the opinionated presets for spring applications, then you can easily use this the ready-made MariaDB4jSpringService to reduce your coding/configuration to get you going, we have an example application ([mariaDB4j-app](<https://github.com/MariaDB4j/MariaDB4j/blob/main/mariaDB4j-app/>)) which illustrates how to wire it up or as an alternative approach via the [MariaDB4jSpringServiceTestSpringConfiguration](<https://github.com/MariaDB4j/MariaDB4j/blob/main/mariaDB4j/src/test/java/ch/vorburger/mariadb4j/tests/springframework/MariaDB4jSpringServiceTestSpringConfiguration.java>).

The DataSource initialization have to wait until MariaDB is ready to receive connections, so we provide `mariaDB4j-springboot` to implement it. You can use it by :

```groovy
dependencies {
   testCompile("ch.vorburger.mariaDB4j:mariaDB4j-springboot:3.1.0")
}
```

In the module, bean name of MariaDB4jSpringService is mariaDB4j, and dataSource depends on it by name. So if you want to customize your mariaDB4j, please make sure the name is correctly.

In [issue #64](<https://github.com/MariaDB4j/MariaDB4j/issues/64>) there is also a discussion about it and pointing to a TestDbConfig.java gist.

### JUnit

Using the JUnit feature of [Rules](https://github.com/junit-team/junit4/wiki/rules) a MariaDB4JRule class is available to be used in your tests.

Add it as a `@Rule` to your test class

```java
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

- getURL() - Get the JDBC connection string to the running DB

  ```java
  @Test
  public void test() {
      Connection conn = DriverManager.getConnection(dbRule.getURL(), "root", "");
  }
  ```

- getDBConfiguration() - Get the Configuration object of the running DB, exposing properties such as DB Port, Data directory, Lib Directory and even a reference to the ProcessListener for the DB process.

  ```java
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

### Maven Plugin

`mariadb4j-maven-plugin` is a Maven plugin that starts and stops a MariaDB instance for the integration test phase.

See POM and integration test in <https://github.com/MariaDB4j/MariaDB4j/tree/mariaDB4j-maven-plugin/mariaDB4j-maven-plugin/src/it/mariadb4j-maven-plugin-test-basic> for usage example.

#### Example

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

### CLI

Because the MariaDB4j JAR is executable, you can also quickly fire up a database from a command line interface:

```sh
java [-DmariaDB4j.port=3718] [-DmariaDB4j.baseDir=/home/theapp/bin/mariadb4j] [-DmariaDB4j.dataDir=/home/theapp/db] -jar mariaDB4j-app*.jar
```

Note the use of the special mariaDB4j-app*.jar for this use-case, its a fat/shaded/über-JAR, based on a Spring Boot launcher.

### Maven Artifacts

MariaDB4j JARs are available from:

1. [Maven Central](https://repo.maven.apache.org/maven2/ch/vorburger/mariaDB4j/):

   ```xml
   <dependency>
       <groupId>ch.vorburger.mariaDB4j</groupId>
       <artifactId>mariaDB4j</artifactId>
       <version>3.1.0</version>
   </dependency>
   ```

1. <https://jitpack.io>: [main-SNAPSHOT](https://jitpack.io/#vorburger/MariaDB4j/main-SNAPSHOT), [releases](https://jitpack.io/#vorburger/MariaDB4j), see also [issue #41 discussion](https://github.com/MariaDB4j/MariaDB4j/issues/41)

1. Not Bintray! (Up to version 2.1.3 MariaDB4j was on Bintray.  Starting with version 2.2.1 we’re only using Maven central.  The 2.2.1 that is on Bintray is broken.)

1. Local build: For bleeding edge `-SNAPSHOT` versions, you (or your build server) can easily build it yourself from
source; just `git clone` this repo, and then `./mvnw install` (or `deploy`) it. -- MariaDB4j's Maven then coordinates are:

### Binaries

MariaDB4j also supports using existing native MariaDB binaries on the host system rather than unpacking MariaDB from the
classpath. This is useful if you need a newer version than is currently distributed. You can control this via the `DBConfigurationBuilder`:

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

#### Core

If you use your own packaged versions of MariaDB native binaries, then the `mariaDB4j-core` artifact JAR,
which contains only the launcher Java code but no embedded native binaries, will be more suitable for you.

You can also exclude one of artifacts of the currently 3 packaged OS platforms to save download if your project / community is mono-platform.

You could also override the version(s) of the respective (transitive) `mariaDB4j-db-*` dependency to downgrade it, and should so be able to use the latest `mariaDB4j-core` artifact JARs, even with older`versions of the JAR archives containing the native mariaDB executables etc. This may be useful if your project for some reason needs a fixed older DB version, but wants to get the latest MariaDB4j launcher Java code.

## Why?

Being able to start a database without any installation / external dependencies
is useful in a number of scenarios, such as all-in-one application packages,
or for running integration tests without depending on the installation,
set-up and up-and-running of an externally managed server.
You could also use this easily run some DB integration tests in parallel but completely isolated,
as the MariaDB4j API explicitly support this.

Java developers frequently use pure Java databases such as H2, hsqldb (HyperSQL), Derby / JavaDB for this purpose.
This library brings the advantage of the installation-free DB approach, while maintaining MariaDB (and thus MySQL) compatibility.

## FAQ

Q: Is MariaDB4j _"secure"?_
A: It's a Java wrapper around MariaDB, so it's kind of as secure as MariaDB. But nota bene that, per default it creates a database called `test`, and the `install()` method creates a new DB with a 'root' user without a password. This may be changed in the future, watch [issue #819](https://github.com/MariaDB4j/MariaDB4j/issues/819). In the meantime, see the `MariaDB4jSampleTutorialTest.testEmbeddedMariaDB4jWithSecurity()` for how to programmatically set a password.

Q: Does MariaDB4j install additional binary non-Java software?
A: If you are using the provided MariaDB database Maven artifacts, then you are pulling platform specific native binaries which will be executed on your system from a remote repository, not just regular Java JARs with classes running in the JVM, through this project.  If you are completely security paranoid, this may worry you (or someone else in your organization).  If that is the case, note that you could still use only the mariadb4j-core artifact from this project, but use a JAR file containing the binaries which you have created and deployed to your organization's Maven repository yourself.  Alternatively, you also use `mariadb4j-core` to launch and control MariaDB binaries installed by other means, e.g. an OS package manager, or perhaps in a (Docker) Container image.  This project's sweet spot and main original intended usage scenario is for integration tests, development environments, and possibly simple all-in-one evaluation kind of packages. It's NOT recommended for serious production environments with security awareness and hot fix patch-ability requirements.

Q: Is MariaDB4j stable enough for production? I need the data to be safe, and performant.
A: Try it out, and if you do find any problem, raise an issue here and let's see if we can fix it. You probably don't risk much in terms of data to be safe and performance - remember MariaDB4j is just a wrapper launching MariaDB (which is a MySQL(R) fork) - so it's as safe and performant as the underlying native DB it uses.

Q: `dyld[23092]: Library not loaded: /opt/homebrew/opt/openssl@3/lib/libssl.3.dylib Referenced from: <F56C2AF5-D763-3960-A454-40591B10F714> /Users/flow/MariaDB4j/mariaDB4j/target/MariaDB4jSpringServiceOverrideBySpringValueTest/baseDir/bin/mariadbd Reason: tried: '/opt/homebrew/opt/openssl@3/lib/libssl.3.dylib' (no such file), '/System/Volumes/Preboot/Cryptexes/OS/opt/homebrew/opt/openssl@3/lib/libssl.3.dylib' (no such file), '/opt/homebrew/opt/openssl@3/lib/libssl.3.dylib' (no such file)`
A: This can happen on MacOS, and can be fixed by [installing Homebrew](https://brew.sh), and then (one time) doing `brew install openssl@3`. (See [issue #497](https://github.com/MariaDB4j/MariaDB4j/issues/497#issuecomment-2762820549) for technical background.)

Q: `ERROR ch.vorburger.exec.ManagedProcess - mysql: /tmp/MariaDB4j/base/bin/mysql: error while loading shared libraries: libncurses.so.5: cannot open shared object file: No such file or directory`
A: This could happen e.g. on Fedora 24 if you have not previous installed any other software package which requires libncurses, and can be fixed by finding the RPM package which provides `libncurses.so.5` via `sudo dnf provides libncurses.so.5` and then install that via `sudo dnf install ncurses-compat-libs`. On Ubuntu Focal 20.04, you need to `sudo apt update && sudo apt install libncurses5`.
(This is fixed if you use the 11.4.5 instead of 10.11.5 MariaDB binaries, which [are released](https://github.com/MariaDB4j/MariaDB4j/issues/1137) with MariaDB4j v3.2.0.)

Q: `/tmp/MariaDB4j/base/bin/mariadbd: error while loading shared libraries: libcrypt.so.1: cannot open shared object file: No such file or directory`
A: Similar to above, and using e.g. https://pkgs.org/search/?q=libcrypt.so.1 we can see that e.g. `sudo dnf install libxcrypt-compat` does the trick for Fedora 39.

## Related Projects

- [liquibase-maven-plugin](https://github.com/openmrs/openmrs-contrib-liquibase-maven-plugin) from OpenMRS builds on MariaDB4j
- [embedded-mariadb-clj](https://github.com/ruroru/embedded-mariadb-clj) is a Clojure 🌯 wrapper of MariaDB4j

- [Testcontainers'](https://www.testcontainers.org/modules/databases/mariadb/) is a somewhat similar solution,  which you could use if you can run 🫙 containers _("Docker")_ - which MariaDB4j does not require. The "world 🌍 is big enough" for both projects!

- [wix-embedded-mysql](https://github.com/wix/wix-embedded-mysql) was yet another take on this space (but it's no longer maintained).

## Release Notes

[Release Notes are in CHANGELOG.md](CHANGELOG.md).

## Contributors

See the [CONTRIBUTORS.md](CONTRIBUTORS.md) file (also included in each built JAR!) for a list of contributors.

Latest/current also on <https://github.com/MariaDB4j/MariaDB4j/graphs/contributors>:

Contributions, patches, forks more than welcome - hack it, and add your name! ;-)

[`docs/contrib.md`](docs/contrib.md) has more tech info on how to contribute.

## Users

MariaDB4j was initially developed for use in Mifos, the "Open Source Technology that accelerates Microfinance", see <http://mifos.org>. Coincidentally, OpenMRS the "Open Source Medical Records System" (see <http://openmrs.org>), another Humanitarian Open Source (HFOSS) project, also uses MariaDB4j (see <https://github.com/MariaDB4j/MariaDB4j/pull/1>).

See the [`USERS.md`](USERS.md) file (also included in each built JAR!) for a list of publicly known users.
Do send a PR adding your name/organization to `USERS.md` to show your appreciation for this free project!

As of April 2025, [630 other GitHub Repositories 🔗 depend on MariaDB4j](https://github.com/MariaDB4j/MariaDB4j/network/dependents).

We have 870 🌟 stars - Thank 🙏 You!

[![Star History Chart](https://api.star-history.com/svg?repos=MariaDB4j/MariaDB4j&type=Date)](https://star-history.com/#MariaDB4j/MariaDB4j&Date)

## Sponsors

_Please support this library **[on 💸 OpenCollective](https://opencollective.com/mariadb4j), via [GitHub Sponsoring](https://github.com/sponsors/vorburger) or through [a Tidelift subscription](https://tidelift.com)** to ensure active maintenance (since 🕰️ 2011!) of this project! 🫶_

- [Flow.swiss](https://flow.swiss/mac-bare-metal) 🇨🇭 "Maas" _(Macs 🍏 as a Service!)_ sponsors macOS support
- Various companies sponsor MariaDB4j via [Tidelift](https://tidelift.com/)
- [Airbnb](https://www.airbnb.com) sponsors MariaDB4j on [OpenCollective](https://opencollective.com/mariadb4j)

Please contact [@vorburger](https://github.com/vorburger/) for consulting support requests.
