What?
=====

MariaDB4j is a Java "launcher" for MariaDB (=MySQL(R)), allowing to use it from Java without ANY installation / external dependencies.  Read again: You do NOT have to have MariaDB binaries installed on your system to use MariaDB4j! -- If you like/use this project, a Star / Watch / Follow me on GitHub is appreciated.

Background: MariaDB is "a backward compatible, drop-in replacement of the MySQL(R) Database Server" :
* Homepage: http://mariadb.org
* FAQ: http://kb.askmonty.org/en/mariadb-faq
* Wikipedia: http://en.wikipedia.org/wiki/MariaDB

[ ![Bintray](https://api.bintray.com/packages/vorburger/maven/MariaDB4j/images/download.svg) ](https://bintray.com/vorburger/maven/MariaDB4j/_latestVersion)
[![JitPack](https://jitpack.io/v/vorburger/MariaDB4j.svg)](https://jitpack.io/#vorburger/MariaDB4j)
[![Dependency Status](https://www.versioneye.com/java/ch.vorburger.mariadb4j:mariadb4j/2.1.3/badge?style=flat)](https://www.versioneye.com/java/ch.vorburger.mariadb4j:mariadb4j/2.1.3)
[![Build Status](https://secure.travis-ci.org/vorburger/MariaDB4j.png?branch=master)](http://travis-ci.org/vorburger/MariaDB4j/)


How? (Java)
----
The MariaDB native binaries are in the MariaDB4j-DB-win*/linux*/mac*.JARs on which the main MariaDB4j JAR depends on by Maven transitive dependencies and, by default, are extracted from the classpath to a temporary base directory on the fly, then started by Java:

1. Install the database with a particular configuration, using short-cut:

```java
DB db = DB.newEmbeddedDB(3306);
```

2. (Optional) The data directory will, by default, be in a temporary directory too, and will automatically get scratched at every restart; this
is suitable for integration tests.  If you use MariaDB4j for something more permanent (maybe an all-in-one application package?),
then you can simply specify a more durable location of your data directory in the DBConfiguration, like so:
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

4. Use the database
```java
Connection conn = db.getConnection();
```
or:
Use your your favourite connection pool... business as usual.

4. If desired, load data from a SQL resource
```java
db.source("path/to/resource.sql");
```

If you would like to / need to start a specific DB version you already have, instead of the version currently
packaged in the JAR, you can use DBConfigurationBuilder setUnpackingFromClasspath(false) & setBaseDir("/my/db/") or -DmariaDB4j.unpack=false -DmariaDB4j.baseDir=/home/you/stuff/myFavouritemMariadDBVersion.   Similarly, you can also pack your own version in a JAR and put it on the classpath, and @Override getBinariesClassPathLocation() in DBConfigurationBuilder to return where to find it (check the source of the default implementation).

How (Spring)
----
MariaDB4j can be used in any Java environment, and is not dependent on dependency injection and the Spring Framework (the dependency to the spring-core*.jar is for a utility, and is unrelated to DI).

If the application in which you use MariaDB4j is anyway based on Spring already however, then the ready-made MariaDB4jSpringService could possibly be useful to you.

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

1. https://jitpack.io: [master-SNAPSHOT](https://jitpack.io/#vorburger/MariaDB4j/master-SNAPSHOT), [releases](https://jitpack.io/#vorburger/MariaDB4j)
2. Bintray at https://bintray.com/vorburger/maven/MariaDB4j/view.
3. Bintray's jCenter, meaning that you can get it if you [add the jCenter repository to your Maven's conf/settings.xml](https://github.com/bintray/bintray-examples/blob/master/maven-example/settings.xml).
4. MariaDB4j is not in Maven central, yet; it could be if you asked for it... ;) Watch [Issue 21](https://github.com/vorburger/MariaDB4j/issues/21).

For bleeding edge SNAPSHOT versions, you (or your build server) can easily build it yourself from
source; just git clone this and then mvn install or deploy. -- MariaDB4j's Maven then coordinates are:

```xml
<groupId>ch.vorburger.mariaDB4j</groupId>
<artifactId>mariaDB4j</artifactId>
<version>2.2.*-SNAPSHOT</version>
```

If you use your own packaged versions of MariaDB native binaries, then the mariaDB4j-core artifact JAR,
which contains only the launcher Java code but no embedded native binaries, will be more suitable for you.  
Similarly, you could also exclude one of artifacts of the currently 3 packaged OS platform.

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

_Please feel free to send a PR linking to your-own-cool-project-using-MariaDB4j_.

As of May 2016, a [Git Hub Code search shows about 300 hits for MariaDB4j usages](https://github.com/search?p=3&q=MariaDB4j&type=Code&utf8=%E2%9C%93).

Anything else?
--------------

Security nota bene: Per default, the MariaDB4j install() creates a new DB with a 'root' user without a password.

FAQ
---
Q: Is MariaDB4j stable enough for production? I need the data to be safe, and performant.
A: Try it out, and if you do find any problem, raise an issue here and let's see if we can fix it. You probably don't risk much in terms of data to be safe and performance - remember MariaDB4j is just a wrapper launching MariaDB (which is a MySQL(R) fork) - so it's as safe and performant as the underlying native DB it uses.

Q: ERROR ch.vorburger.exec.ManagedProcess - mysql: /tmp/MariaDB4j/base/bin/mysql: error while loading shared libraries: libncurses.so.5: cannot open shared object file: No such file or directory 
A: This could happen e.g. on Fedora 24 (if you have not previous installed any other software package which requires libncurses), and can be finding the package which provides `libncurses.so.5` via `sudo dnf provides libncurses.so.5` and then installed that via `sudo dnf install ncurses-compat-libs`.


Release?
--------

    mvn license:update-file-header
    mvn -Dmaven.test.skip=true package

Must be done first make to sure that the JavaDoc is clean.  Check for both errors and any WARNING (until [MJAVADOC-401](http://jira.codehaus.org/browse/MJAVADOC-401)).

    mvn release:prepare (-Dresume=false)
    mvn release:perform
    mvn release:clean

Discard and go back to fix something and re-release (before Publishing in Bintray) e.g. using EGit via Rebase Interactive on the commit before "prepare release" and skip the two commits made by the maven-release-plugin. Use git push --force to remote, and remove local tag using git tag -d mariaDB4j-2.x.y, and remote tag using 'git push ssh :mariaDB4j-2.x.y'. (Alternatively try BEFORE release:clean use 'mvn release:rollback', but that leaves ugly commits.)


Who?
----

Here are some of the early contributors, latest/current also on https://github.com/vorburger/MariaDB4j/graphs/contributors:

* Michael Vorburger, February/March 2012.
* Michael Seaton, October 2013.
* Juhani Simola, March 2014.
* Cedric Gatay, March 2015.
* _YourNameHere, if you jump on-board..._

Contributions, patches, forks more than welcome - hack it, and add your name here! ;-)
