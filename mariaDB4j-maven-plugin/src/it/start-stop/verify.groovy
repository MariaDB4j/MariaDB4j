import static org.junit.jupiter.api.Assertions.assertTrue

def file = new File(basedir, "build.log")
assertTrue(file.text.contains("Installing a new embedded database"), "MariaDB4j should have run")
assertTrue(file.text.contains("Database stopped."), "MariaDB4j should attempt to stop")
