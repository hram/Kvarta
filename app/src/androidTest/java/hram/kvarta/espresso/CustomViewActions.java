package hram.kvarta.espresso;

import android.support.test.espresso.PerformException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.espresso.util.HumanReadables;
import android.support.test.espresso.util.TreeIterables;
import android.view.View;
import android.widget.NumberPicker;

import org.hamcrest.Matcher;

import java.lang.reflect.Method;
import java.util.concurrent.TimeoutException;

import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by ellen on 4/15/15.
 * <p/>
 * This class provides application-specific ViewActions which can be used for testing.
 */
public class CustomViewActions {

    /**
     * Perform action of waiting for a specific view id.
     */
    public static ViewAction waitId(final int viewId, final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "wait for a specific view with id <" + viewId + "> during " + millis + " millis.";
            }

            @Override
            public void perform(final UiController uiController, final View view) {
                uiController.loopMainThreadUntilIdle();
                final long startTime = System.currentTimeMillis();
                final long endTime = startTime + millis;
                final Matcher<View> viewMatcher = withId(viewId);

                do {
                    for (View child : TreeIterables.breadthFirstViewTraversal(view)) {
                        // found view with required ID
                        if (viewMatcher.matches(child)) {
                            return;
                        }
                    }

                    uiController.loopMainThreadForAtLeast(50);
                }
                while (System.currentTimeMillis() < endTime);

                // timeout happens
                throw new PerformException.Builder()
                        .withActionDescription(this.getDescription())
                        .withViewDescription(HumanReadables.describe(view))
                        .withCause(new TimeoutException())
                        .build();
            }
        };
    }

    /**
     * A custom ViewAction which allows the system to wait for a view matching a passed in matcher
     *
     * @param aViewMatcher The matcher to wait for
     * @param timeout      How long, in milliseconds, to wait for this match.
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

    public static ViewAction numberPickerSetValue(final int value) {
        return new ViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                NumberPicker np = (NumberPicker) view;
                try {
                    Method method = np.getClass().getDeclaredMethod("setValueInternal", int.class, boolean.class);
                    method.setAccessible(true);
                    method.invoke(np, value, true);
                } catch (Exception e) {
                }
            }

            @Override
            public String getDescription() {
                return "Set the value into the NumberPicker";
            }

            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(NumberPicker.class);
            }
        };
    }

    public static ViewAction numberPickerNotifyChange() {
        return new ViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                NumberPicker np = (NumberPicker) view;
                try {
                    Method method = np.getClass().getDeclaredMethod("notifyChange", int.class, int.class);
                    method.setAccessible(true);
                    method.invoke(np, 0, 1);
                } catch (Exception e) {
                    int f = 0;
                }
            }

            @Override
            public String getDescription() {
                return "Notify change value into the NumberPicker";
            }

            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(NumberPicker.class);
            }
        };
    }

    public static ViewAction numberPickerScrollUp() {
        return new ViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                NumberPicker np = (NumberPicker) view;
                try {
                    Method method = np.getClass().getDeclaredMethod("changeValueByOne", boolean.class);
                    method.setAccessible(true);
                    method.invoke(np, true);
                } catch (Exception e) {
                }
            }

            @Override
            public String getDescription() {
                return "Scroll Up into the NumberPicker";
            }

            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(NumberPicker.class);
            }
        };
    }

    public static ViewAction numberPickerScrollDown() {
        return new ViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                NumberPicker np = (NumberPicker) view;
                try {
                    Method method = np.getClass().getDeclaredMethod("changeValueByOne", boolean.class);
                    method.setAccessible(true);
                    method.invoke(np, false);
                } catch (Exception e) {
                }
            }

            @Override
            public String getDescription() {
                return "Scroll Down into the NumberPicker";
            }

            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(NumberPicker.class);
            }
        };
    }

    public static ViewAction withCustomConstraints(final ViewAction action, final Matcher<View> constraints) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return constraints;
            }

            @Override
            public String getDescription() {
                return action.getDescription();
            }

            @Override
            public void perform(UiController uiController, View view) {
                action.perform(uiController, view);
            }
        };
    }

    private static final float EDGE_FUZZ_FACTOR = 0.083f;

    public static ViewAction swipeUpSlow() {
        return ViewActions.actionWithAssertions(new GeneralSwipeAction(Swipe.SLOW, translate(GeneralLocation.BOTTOM_CENTER, 0, -EDGE_FUZZ_FACTOR), GeneralLocation.TOP_CENTER, Press.THUMB));
    }

    static CoordinatesProvider translate(final CoordinatesProvider coords, final float dx, final float dy) {
        return new CoordinatesProvider() {
            @Override
            public float[] calculateCoordinates(View view) {
                float xy[] = coords.calculateCoordinates(view);
                xy[0] += dx * view.getWidth();
                xy[1] += dy * view.getHeight();
                return xy;
            }
        };
    }
}