import static org.junit.jupiter.api.Assertions.assertFalse

def file = new File(basedir, "build.log")
assertFalse(file.text.contains("Installing a new embedded database"), "MariaDB4j should not have run")
assertFalse(file.text.contains("Database stopped."), "MariaDB4j should not attempt to stop")
