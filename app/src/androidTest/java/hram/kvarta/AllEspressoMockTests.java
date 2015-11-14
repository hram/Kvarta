package hram.kvarta;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import hram.kvarta.espresso.MainActivityLaunchesLoginActivityMockTest;
import hram.kvarta.espresso.MainActivityMockTest;
import hram.kvarta.espresso.SettingsActivityMockTest;

/**
 * @author Evgeny Khramov
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({MainActivityMockTest.class, MainActivityLaunchesLoginActivityMockTest.class, SettingsActivityMockTest.class})
public class AllEspressoMockTests {
}
