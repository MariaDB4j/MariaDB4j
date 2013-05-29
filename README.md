TODO: MarkDown (*.md) formatting

What?

MariaDB is "a backward compatible, drop-in replacement of the MySQL� Database Server" :
	* Homepage: http://mariadb.org
	* FAQ: http://kb.askmonty.org/en/mariadb-faq
	* Wikipedia: http://en.wikipedia.org/wiki/MariaDB

MariaDB4j is a Java "launcher" for MariaDB, allowing to use it from Java without any installation / external dependencies.


How?

MariaDB binaries are in the JAR and extracted to a temporary directory on the fly, then started by Java - with 1 line:

```java
DB db = DBFactory.newEmbeddedTemporaryDB().start();
```
or:
```java
DB db = DBFactory.newEmbeddedDB(new File("myDB").start();
```		
Then get a JDBC Connection directly or via your favourite connection pool... business as usual.
```xml
<groupId>ch.vorburger.mariaDB4j</groupId>
<artifactId>mariaDB4j</artifactId>
<version>1.0.0</version>
```

Why?

Being able to start a database without any installation / external dependencies 
is useful in a number of scenarios, such as all-in-one application packages,
or for running integration tests without depending on the installation,
set-up and up-and-running of an externally managed server.
(Can also easily run DB integration tests in parallel.)

Java developers frequently use pure Java databases such as H2, hsqldb (HyperSQL), Derby / JavaDB for this purpose.
 
This library brings the advantage of the installation-free DB approach, while maintaining MariaDB (and thus MySQL�) compatibility.

It was initially developed for use in Mifos, the "Open Source Technology that accelerates Microfinance", see http://mifos.org.

Build?

[![Build Status](https://secure.travis-ci.org/vorburger/MariaDB4j.png?branch=master)](http://travis-ci.org/vorburger/MariaDB4j/)

Who?

Michael Vorburger, February/March 2012.

Contributions, patches, forks more than welcome - hack it, and add your name here! ;-)
