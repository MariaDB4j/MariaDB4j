package ch.vorburger.mariadb4j.tests;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfiguration;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

/**
 * Reproduces issue #10.
 * 
 * @see <a href="https://github.com/vorburger/MariaDB4j/issues/10">MariaDB4j issue #10</a>
 * 
 * @author Michael Vorburger
 */
public class StartManyTimes {

    // TODO Fix this problem (and remove this)

    public static void main(String[] args) throws ManagedProcessException {
        DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
        configBuilder.detectFreePort();
        DBConfiguration config = configBuilder.build();

        for (int i = 0; i < 100000; i++) {
            DB db = DB.newEmbeddedDB(config);
            db.start();
            db.stop();
            System.out.println(i);
        }
    }

}
