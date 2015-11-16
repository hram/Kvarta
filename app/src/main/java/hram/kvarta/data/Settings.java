package hram.kvarta.data;

import droidkit.content.BoolValue;
import droidkit.content.IntValue;
import droidkit.content.LongValue;
import droidkit.content.StringValue;
import droidkit.content.Value;

/**
 * @author Evgeny Hramov
 */
public interface Settings {
    @Value
    StringValue tsgId();

    @Value
    StringValue accountId();

    @Value
    StringValue password();

    @Value
    BoolValue demo();

    @Value
    StringValue address();

    @Value
    StringValue userInfo();

    @Value
    StringValue lastTime();

    @Value(boolValue = true)
    BoolValue enableUserInfo();

    @Value(boolValue = true)
    BoolValue enableRemind();

    @Value(intValue = 26)
    IntValue remindDate();

    @Value
    LongValue lastRebootTime();
}
