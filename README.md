What?
=====

MariaDB is "a backward compatible, drop-in replacement of the MySQL(R) Database Server" :
* Homepage: http://mariadb.org
* FAQ: http://kb.askmonty.org/en/mariadb-faq
* Wikipedia: http://en.wikipedia.org/wiki/MariaDB

MariaDB4j is a Java "launcher" for MariaDB, allowing to use it from Java without ANY installation / external dependencies.  
Read again: You do NOT have to have MariaDB binaries installed on your system to use MariaDB4j!


How?
----
MariaDB binaries are in the MariaDB4j JAR and extracted to a temporary directory on the fly, then started by Java - with 1 line:

```java
DB db = DBFactory.newEmbeddedTemporaryDB().start();
```
or:
```java
DB db = DBFactory.newEmbeddedDB(new File("myDB").start();
```		
Then get a JDBC Connection directly or via your favourite connection pool... business as usual.

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
[![Build Status](https://secure.travis-ci.org/vorburger/MariaDB4j.png?branch=master)](http://travis-ci.org/vorburger/MariaDB4j/)

Who?
----
* Michael Vorburger, February/March 2012.
* _YourNameHere, if you jump on-board..._

Contributions, patches, forks more than welcome - hack it, and add your name here! ;-)
