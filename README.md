TODO: MarkDown (*.md) formatting

What?

MariaDB is "a backward compatible, drop-in replacement of the MySQL® Database Server" :
	* Homepage: http://mariadb.org
	* FAQ: http://kb.askmonty.org/en/mariadb-faq
	* Wikipedia: http://en.wikipedia.org/wiki/MariaDB

MariaDB4j is a Java "launcher" for MariaDB.  
It includes some MariaDB binaries, allowing a Java application to start up a MariaDB without any installation / external dependencies. 


How?

TODO: Review/complete this later... but the idea is to support simply:

		DB db = new EmbeddedDB("target/db1");
		db.start();
		TODO... get a JDBC Connection... or use your favourite connection pool...

TODO LINK - see the MariaDB4jSampleTutorialTest.java for more examples & details. 

TODO: Publish a 1.0.0 on Maven Central when ready...

	<groupId>ch.vorburger.mariaDB4j</groupId>
	<artifactId>mariaDB4j</artifactId>
	<version>1.0.0</version>


Why?

Being able to start a database without any installation / external dependencies 
is useful in a number of scenarios, such as all-in-one application packages,
or for running integration tests without depending on the installation,
set-up and up-and-running of an externally managed server.
(Can also easily run DB integration tests in parallel.)

Java developers frequently use pure Java databases such as H2, hsqldb (HyperSQL), Derby / JavaDB for this purpose.
 
This library brings the advantage of the installation-free DB approach, while maintaining MariaDB (and thus MySQL®) compatibility.

It was initially developed for use in Mifos, the "Open Source Technology that accelerates Microfinance", see http://mifos.org.


Who?

Michael Vorburger, February/March 2012.

Contributions, patches, forks more than welcome - hack it, and add your name here! ;-)
