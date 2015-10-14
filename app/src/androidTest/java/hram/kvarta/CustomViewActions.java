package hram.kvarta;

import android.support.test.espresso.PerformException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.support.test.espresso.util.TreeIterables;
import android.text.Layout;
import android.text.SpannableString;
import android.view.View;
import android.widget.TextView;

import org.hamcrest.Matcher;

import java.security.InvalidParameterException;
import java.util.concurrent.TimeoutException;

import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;

/**
 * Created by ellen on 4/15/15.
 *
 * This class provides application-specific ViewActions which can be used for testing.
 */
public class CustomViewActions {

    /**
     * A custom ViewAction which allows the system to wait for a view matching a passed in matcher
     * @param aViewMatcher The matcher to wait for
     * @param timeout How long, in milliseconds, to wait for this match.
     * @return The constructed @{link ViewAction}.
     */
    public static ViewAction waitForMatch(final Matcher<View> aViewMatcher, final long timeout) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Waiting for view matching " + aViewMatcher;
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadUntilIdle();

                //What time is it now, and what time will it be when this has timed out?
                final long startTime = System.currentTimeMillis();
                final long endTime = startTime + timeout;

                do {
                    for (View child : TreeIterables.breadthFirstViewTraversal(view)) {
                        if (aViewMatcher.matches(child)) {
                            //we found it! Yay!
                            return;
                        }
                    }

                    //Didn't find it, loop around a bit.
                    uiController.loopMainThreadForAtLeast(50);
                } while (System.currentTimeMillis() < endTime);

                //The action has timed out.
                throw new PerformException.Builder()
                        .withActionDescription(getDescription())
                        .withViewDescription("")
                        .withCause(new TimeoutException())
                        .build();
            }
        };
    }
}