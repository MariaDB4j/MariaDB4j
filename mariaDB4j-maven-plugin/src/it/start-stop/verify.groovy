def file = new File(basedir, "build.log")
assert file.text.contains("Installing a new embedded database") : 'MariaDB4j should have run'
assert file.text.contains("Database stopped.") : 'MariaDB4j Should attempt to stop'
