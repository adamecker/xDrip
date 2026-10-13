package com.eveningoutpost.dexdrip.utilitymodels;

import com.eveningoutpost.dexdrip.models.UserError;

/**
 * Thread-safe cache of the loop's current IOB, as reported via Nightscout
 * devicestatus (openaps.iob.iob). Used to gate low BG alarms: a low with no
 * insulin on board is less urgent than one with significant IOB.
 *
 * Fail-safe by design: if no value has been received, or the cached value is
 * older than the caller's maxAgeMs, getFreshIob() returns null and callers
 * must treat that as "unknown" (i.e. do NOT suppress the alarm).
 */
public class LoopIob {

    private static final String TAG = "LoopIob";

    private static Double iob = null;
    private static long timestampMs = 0;

    public static synchronized void setIob(final double value, final long timestamp) {
        iob = value;
        timestampMs = timestamp;
        UserError.Log.d(TAG, "Loop IOB updated: " + value + " U");
    }

    /**
     * @param maxAgeMs maximum acceptable age of the cached value
     * @return cached IOB if present and fresh, otherwise null
     */
    public static synchronized Double getFreshIob(final long maxAgeMs) {
        if (iob == null) {
            return null;
        }
        if (System.currentTimeMillis() - timestampMs > maxAgeMs) {
            UserError.Log.d(TAG, "Loop IOB stale, ignoring");
            return null;
        }
        return iob;
    }
}
