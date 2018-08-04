import static org.junit.Assert.assertFalse

def file = new File(basedir, "build.log")
assertFalse 'MariaDB4j should not have run', file.text.contains("Installing a new embedded database")
assertFalse 'MariaDB4j Should not attempt to stop', file.text.contains('Shutdown Hook Deletion Thread for Temporary DB')
