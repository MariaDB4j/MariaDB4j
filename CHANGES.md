MariaDB4j Release Notes
=======================

v2.3.0 @ 2018-05-15
---

* @cortiz added dumpXML and dumpSQL
* @marcelvanderperk added setSecurityDisabled()
* @bjornblomqvist empty password string is treated the same as null
* @paulroemer added setDeletingTemporaryBaseAndDataDirsOnShutdown()
* @lde-avaleo and @jai-deep contributed MariaDB 10.2.11 (and @cortiz 10.1.23)
* @vorburger moved code to [ch.vorburger.exec](https://github.com/vorburger/ch.vorburger.exec) and added dependency
* @dependabot bumped various 3rd party libraries, courtesy of https://dependabot.com
* @vorburger now compiles MariaDB4j with Java 8 instead of 6
* @vorburger fixed bug #88 running MariaDb4j on Glassfish


v2.2.3 @ 2017-02-10
---

* @lpearson05 contributed upgrade of older commons-collections with CVE-2011-2092 vulnerability to commons-collections 4.1 (https://issues.apache.org/jira/browse/COLLECTIONS-580)
* @clfsoft contributed [issue #49](https://github.com/vorburger/MariaDB4j/issues/49) upgrade of MariaDB Win 32 version from 10.0.13 to 10.1.20
* @vorburger bumped mariadb-java-client from version 1.4.6 to 1.5.5, and Spring Boot from 1.4.0 to 1.5.1


v2.2.2 @ 2016-08-20
---

* @hanklank contributed [issue #37](https://github.com/vorburger/MariaDB4j/issues/37) upgrade of MariaDB Mac OS X version from 5.5.34 to 10.1.9 (tested by @brendonanderson) 
* Fixed [issue #27](https://github.com/vorburger/MariaDB4j/issues/27) Do not log info messages as errors 
* Upgrade version of Spring Boot from 1.3.6 to 1.4.0


v2.2.1 @ 2016-07-24
---

* Maven central release [issue #21](https://github.com/vorburger/MariaDB4j/issues/21): Finally, as requested for too long by too many... ;-) FYI @nicmon @metawave @krm1312 @alexpanov @jinahya @kedgecomb @lc-nyovchev @tbenedetti-lendico @fleger @chrisbloe @fleger @lc-nyovchev @ollemuhr @laurent-dol
* @anverus fixed [issue #39](https://github.com/vorburger/MariaDB4j/issues/39): If baseDir is set libedir has to be repointed too to make use of bundled native libs
* @ghiron for @honestica contributed upgrade of mariadb linux version from 10.1.8 to 10.1.13 
* README updated with new section re. DB upgrade contributions
* Upgrade version of Spring Framework, Spring Boot, and some Maven plugins
* API extension: class DB has a handy getConfiguration() method to get its original DBConfiguration back
* JARs built include README, CHANGES, LEGAL, LICENSE; and (new!) CONTRIBUTORS, CONTRIBUTING, NOTICE  
* NOTICE file https://github.com/vorburger/MariaDB4j/issues/14
* Fedora 24 related build test failure and README doc update


v2.2.0 @ 2016-05-05
---

* MAJOR Distribution and project org. split up formerly monolithic MariaDB4j into separate core, exec and binaries artifacts; separately versioned
* @CedricGatay: NEW addArg() method in DBConfigurationBuilder to pass additional flags when spawning a new MariaDB/Mysql process (e.g. like lower_case_table_names, in a mixed OS environment)
* @jahewson: Security related fixed Exception if there are spaces in the data directory path (https://github.com/vorburger/MariaDB4j/issues/30)
* @timorohwedder: API extended for setting OS dependent library path to optional binary libraries 
* @timorohwedder: Bumped (upgraded) bundled MariaDB Linux version
* Kevin McLaughlin: Synchronize DB install to try to fix some intermittent test failures when running parallel tests in maven that depend on MariaDB4j
* Bumped (upgraded) versions of some 3rd-party Java libraries; thanks https://www.versioneye.com/java/ch.vorburger.mariadb4j:mariadb4j/
* Src: Tabs to Spaces, and enforced by Checkstyle running in Build


v2.1.3 @ 2014-12-27
----

* FIXED Windows package, now tested; it was completely broken in 2.1.1 (but worked in the original 2.1.0)

v2.1.1 @ 2014-12-03
----

* FIXED bad concurrency bug https://github.com/vorburger/MariaDB4j/issues/10
* Upgraded commons-exec v1.2 => v1.3 & Spring Boot v1.1.6 => v1.1.9
* Less annoyingly verbose logging now
* minor code clean-ups etc.

v2.1.0 @ 2014-09-21
------

* Original first public release
  (project existed before without Maven Bintray release; people just built from source)
 
