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


Legal?

IANAL IANAL IANAL IANAL IANAL IANAL IANAL IANAL IANAL IANAL IANAL IANAL IANAL IANAL IANAL 
AKA: "This section is not a substitute for professional legal advice. It is intended to provide general guidance to developers, but it was not prepared by an attorney nor is it in any way legal advice."

This library is dual licensed under the Apache License 2.0 & the Eclipse Public License v1.0.
The ASL and EPL licenses, instead of (L)GPL, have been chosen so that non-GPL Java applications
can use this.

As such, note that this library must never "combine" with GPL licensed code.
BThe pure "distribution" of the GPL licensed MariaDB binaries with this JAR is OK, as 
this Java library never "links" to those binaries, but merely 
"launches" them a separate OS process (non shared address space).

Here is a link to MariaDB's GPL license:
http://kb.askmonty.org/en/mariadb-license

Here is a link allowing you to download MariaDB's source code:
http://kb.askmonty.org/en/getting-the-mariadb-source-code

Note that you have to figure out yourself whether you can use the MySQL® JDBC driver (Connector/J) from Oracle in your application.
The MySQL® JDBC driver (Connector/J) is GPL licensed, but due to the MySQL FLOSS License Exception,
it "permits linking with other components that are licensed under OSI-approved open source licenses".

Something called "MySQL Connector/MXJ" fulfills a functionally similar goal.
As MySQL Connector/MXJ is GPL licensed, without a clear public statement of
the FLOSS License Exception applicability, it can not not be used with e.g. ASL projects.
This project has no relationship to MySQL Connector/MXJ.

AKA: "This section is not a substitute for professional legal advice. It is intended to provide general guidance to developers, but it was not prepared by an attorney nor is it in any way legal advice."
IANAL IANAL IANAL IANAL IANAL IANAL IANAL IANAL IANAL IANAL IANAL IANAL IANAL IANAL IANAL 


Who?

Michael Vorburger, February/March 2012.

Contributions, patches, forks more than welcome - hack it, and add your name here! ;-)
