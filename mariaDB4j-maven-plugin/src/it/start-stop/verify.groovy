import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

def file = new File(basedir, "build.log")
assertFalse 'MariaDB4j should have run', file.text.contains("Installing a new embedded database")
assertFalse 'MariaDB4j Should attempt to stop', file.text.contains('Shutdown Hook Deletion Thread for Temporary DB')
