package hram.kvarta.events;

import hram.kvarta.network.ValuesManager;

/**
 * @author Evgeny Khramov
 */
public class LoadDataEndedEvent {

    private final long[] mHotValues = new long[4];
    private final long[] mColdValues = new long[4];
    private final long[] mElDayValues = new long[4];
    private final long[] mElNightValues = new long[4];

    public LoadDataEndedEvent(long[] hotValues, long[] coldValues, long[] elDayValues, long[] elNightValues) {
        System.arraycopy(hotValues, 0, mHotValues, 0, hotValues.length );
        System.arraycopy(coldValues, 0, mColdValues, 0, coldValues.length );
        System.arraycopy(elDayValues, 0, mElDayValues, 0, elDayValues.length );
        System.arraycopy(elNightValues, 0, mElNightValues, 0, elNightValues.length );
    }

    public long getValue(int value) {
        return getValue(value, 0);
    }

    public long getValue(int value, int index) {
        switch (value) {
            case ValuesManager.WATER_HOT:
                return mHotValues[index];
            case ValuesManager.WATER_COLD:
                return mColdValues[index];
            case ValuesManager.ELECTRICITY_DAY:
                return mElDayValues[index];
            case ValuesManager.ELECTRICITY_NIGHT:
                return mElNightValues[index];
            default:
                throw new IllegalArgumentException();
        }
    }
}
