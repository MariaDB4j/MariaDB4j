What?
=====

MariaDB is "a backward compatible, drop-in replacement of the MySQL(R) Database Server" :
* Homepage: http://mariadb.org
* FAQ: http://kb.askmonty.org/en/mariadb-faq
* Wikipedia: http://en.wikipedia.org/wiki/MariaDB

MariaDB4j is a Java "launcher" for MariaDB, allowing to use it from Java without ANY installation / external dependencies.  Read again: You do NOT have to have MariaDB binaries installed on your system to use MariaDB4j!

How?
----
MariaDB binaries are in the MariaDB4j JAR and, by default, extracted to a temporary base directory on the fly, then started by Java:

1. Install the database with a particular configuration, using short-cut:

```java
DB db = DB.newEmbeddedDB(3306);
```

2. (Optional) The data directory will, by default, be in a temporary directory too, and will automatically get scratched at every restart; this
is suitable for integration tests.  If you use MariaDB4j for something more permanent (maybe an all-in-one application package?),
then you can simply specify a more durable location of your data directory in the Configuration, like so:
```java
Configuration config = new Configuration();
config.setPort(3306);
// OR: config.detectFreePort(); // == config.setPort(0);
config.setDataDir("/home/theapp/db"); // just an example
DB db = DB.newEmbeddedDB(config);
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

Where from?
-----------

MariaDB4j is not in Maven central (yet; it could be if you asked for it...), 
so for now you (or your build server) have to build it yourself from
source. -- MariaDB4j's Maven coordinates are:

```xml
<groupId>ch.vorburger.mariaDB4j</groupId>
<artifactId>mariaDB4j</artifactId>
<version>1.0.0</version>
```

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

It was initially developed for use in Mifos, the "Open Source Technology that accelerates Microfinance", see http://mifos.org.

Build?
------
[![Build Status](https://secure.travis-ci.org/vorburger/MariaDB4j.png?branch=master)](http://travis-ci.org/vorburger/MariaDB4j/) <== Linux on Travis CI + 
[Mac OS X CI courtesy of CloudBees](https://vorburger.ci.cloudbees.com/job/MariaDB4j.MacOSX/).

_TBD: How-to get a free Jenkins-like CI on a Windows box somewhere?_

Who?
----
* Michael Vorburger, February/March 2012.
* Michael Seaton, October 2013.
* Juhani Simola, March 2014.
* _YourNameHere, if you jump on-board..._

Contributions, patches, forks more than welcome - hack it, and add your name here! ;-)


[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/vorburger/mariadb4j/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

