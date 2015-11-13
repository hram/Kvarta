package hram.kvarta.espresso;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

/**
 * @author Evgeny Khramov
 */
public class CustomViewMatchers {

    /**
     * Проверяет EditText на то что текст пустой
     * @return true если текст не задан
     */
    public static Matcher<View> isEmptyEditText() {
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof EditText)) {
                    return false;
                }
                return TextUtils.isEmpty(((EditText) view).getText());
            }

            @Override
            public void describeTo(Description description) {
            }
        };
    }

    public static Matcher<View> numberPickerHasValue(final int value) {
        return new TypeSafeMatcher<View>() {
            private int actualValue = -1;
            private int expectedValue;
            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof NumberPicker)) {
                    return false;
                }
                expectedValue = value;
                actualValue = ((NumberPicker) view).getValue();
                return expectedValue == actualValue;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("ExpectedValue: %1s, ActualValue: %2s", expectedValue, actualValue));
            }
        };
    }

    public static Matcher<? super View> hasErrorText(String expectedError) {
        return new ErrorTextMatcher(expectedError);
    }

    private static class ErrorTextMatcher extends TypeSafeMatcher<View> {
        private final String expectedError;

        private ErrorTextMatcher(String expectedError) {
            this.expectedError = expectedError;
        }

        @Override
        public boolean matchesSafely(View view) {
            if (!(view instanceof EditText)) {
                return false;
            }
            EditText editText = (EditText) view;
            return expectedError.equals(editText.getError());
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("with error: " + expectedError);
        }
    }

    public static Matcher<View> withDrawable(final int expectedId) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("with drawable from resource id: " + expectedId);
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof ImageView)){
                    return false;
                }
                ImageView imageView = (ImageView) view;
                if (expectedId < 0){
                    return imageView.getDrawable() == null;
                }
                Resources resources = view.getContext().getResources();
                Drawable expectedDrawable = resources.getDrawable(expectedId);
                if (expectedDrawable == null) {
                    return false;
                }
                BitmapDrawable bmd = (BitmapDrawable) imageView.getDrawable();
                Bitmap bitmap = bmd.getBitmap();
                BitmapDrawable expected = (BitmapDrawable) expectedDrawable;
                Bitmap otherBitmap = expected.getBitmap();
                return bitmap.sameAs(otherBitmap);
            }
        };
    }

    public static Matcher<View> withResourceName(String resourceName) {
        return withResourceName(Matchers.is(resourceName));
    }

    public static Matcher<View> withResourceName(final Matcher<String> resourceNameMatcher) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("with resource name: ");
                resourceNameMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                int id = view.getId();
                return id != View.NO_ID && id != 0 && view.getResources() != null && resourceNameMatcher.matches(view.getResources().getResourceName(id));
            }
        };
    }
}
