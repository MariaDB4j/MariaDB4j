import static org.junit.Assert.assertTrue

def file = new File(basedir, "build.log")
assertTrue 'MariaDB4j should have run', file.text.contains("Installing a new embedded database")
assertTrue 'MariaDB4j Should attempt to stop', file.text.contains("Database stopped.")
