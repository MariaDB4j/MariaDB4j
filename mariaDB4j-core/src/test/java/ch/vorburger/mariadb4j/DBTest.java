package ch.vorburger.mariadb4j;

import ch.vorburger.exec.ManagedProcess;
import ch.vorburger.exec.ManagedProcessBuilder;
import ch.vorburger.exec.ManagedProcessException;
import java.io.IOException;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;


public class DBTest {

  @Test
  public void testInstallWillOnlyAttemptOneTimeWithMaxWaitTimeIfNotConfigured()
      throws ManagedProcessException, IOException {
    // given
    ManagedProcess mockManagedProcess = new ManagedProcessBuilder("sleep").addArgument("0.01s").build();
    DB dbSpy = Mockito.spy(new DB(new DBConfigurationBuilder().build()));
    Mockito.doReturn(mockManagedProcess).when(dbSpy).createMysqlInstallProcess();

    // when
    dbSpy.install();

    // then
    Mockito.verify(dbSpy, Mockito.times(0)).attemptToInstallNTimesWithTimeoutInMs(Mockito.anyInt(), Mockito.anyInt());
  }

  @Test
  public void testInstallWillOnlyAttemptToLoopInstallationIfConfiguredTo()
      throws ManagedProcessException, IOException {
    // given
    ManagedProcess slowMockManagedProcess = new ManagedProcessBuilder("sleep").addArgument("20s").build();
    ManagedProcess fastMockManagedProcess = new ManagedProcessBuilder("sleep").addArgument("0.01s").build();
    DB dbSpy = Mockito.spy(new DB(new DBConfigurationBuilder().setInstallationTimeoutInMs(50).setTimesToAttemptInstall(4).build()));
    Mockito.doReturn(slowMockManagedProcess) // first return a process that wont finish
        .doReturn(fastMockManagedProcess) // process will finish now
        .when(dbSpy).createMysqlInstallProcess();

    // when
    dbSpy.install();

    // then
    Mockito.verify(dbSpy, Mockito.times(1)).attemptToInstallNTimesWithTimeoutInMs(Mockito.anyInt(), Mockito.anyInt());
  }

  @Test
  public void testInstallWillOnlyAttemptToInstallUntilSuccessful()
      throws ManagedProcessException, IOException {
    // given
    ManagedProcess slowMockManagedProcess = new ManagedProcessBuilder("sleep").addArgument("20s").build();
    ManagedProcess mediumMockManagedProcessButStillTooSlow = new ManagedProcessBuilder("sleep").addArgument("10s").build();
    ManagedProcess fastMockManagedProcess = new ManagedProcessBuilder("sleep").addArgument("0.01s").build();
    DB dbSpy = Mockito.spy(new DB(new DBConfigurationBuilder().build()));
    Mockito.doReturn(slowMockManagedProcess) // first return a process that wont finish
        .doReturn(mediumMockManagedProcessButStillTooSlow) // process still wont finish
        .doReturn(fastMockManagedProcess) // process will finish now
        .when(dbSpy).createMysqlInstallProcess();

    // when
    int attempts = dbSpy.attemptToInstallNTimesWithTimeoutInMs(5, 50);

    // then
    Mockito.verify(dbSpy, Mockito.times(1)).attemptToInstallNTimesWithTimeoutInMs(Mockito.anyInt(), Mockito.anyInt());
    assertEquals(3, attempts);
  }


  @Test(expected = ManagedProcessException.class)
  public void testInstallWillOnlyAttemptToInstallTheConfiguredAmountOfTimes()
      throws ManagedProcessException, IOException {
    // given
    ManagedProcess slowMockManagedProcess = new ManagedProcessBuilder("sleep").addArgument("2s").build();
    ManagedProcess alsoVerySlowMockManagedProcess = new ManagedProcessBuilder("sleep").addArgument("2s").build();
    ManagedProcess mediumMockManagedProcessButStillTooSlow = new ManagedProcessBuilder("sleep").addArgument("1s").build();
    DB dbSpy = Mockito.spy(new DB(new DBConfigurationBuilder().setInstallationTimeoutInMs(50).setTimesToAttemptInstall(3).build()));
    Mockito.doReturn(slowMockManagedProcess) // first return a process that wont finish
        .doReturn(alsoVerySlowMockManagedProcess) // process still wont finish
        .doReturn(mediumMockManagedProcessButStillTooSlow) // process still wont finish
        .when(dbSpy).createMysqlInstallProcess();

    // when
    try {
      dbSpy.install();
    } catch (ManagedProcessException e) {
      assertEquals("Unable to install mysql after 3 attempts", e.getCause().getMessage());
      throw e;
    }
  }

}