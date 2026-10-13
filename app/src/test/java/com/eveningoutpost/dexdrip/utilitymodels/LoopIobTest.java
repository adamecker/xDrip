package com.eveningoutpost.dexdrip.utilitymodels;

import com.eveningoutpost.dexdrip.RobolectricTestWithConfig;

import org.junit.Test;

import static com.google.common.truth.Truth.assertWithMessage;

/**
 * Unit tests for LoopIob — the thread-safe cache of loop IOB from Nightscout
 * devicestatus, used to gate low BG alarms.
 */
public class LoopIobTest extends RobolectricTestWithConfig {

    @Test
    public void freshValueReturned() {
        LoopIob.setIob(1.23, System.currentTimeMillis());
        final Double result = LoopIob.getFreshIob(15 * 60 * 1000L);
        assertWithMessage("fresh IOB should be returned").that(result).isNotNull();
        assertWithMessage("IOB value should match").that(result).isWithin(0.001).of(1.23);
    }

    @Test
    public void staleValueReturnsNull() {
        LoopIob.setIob(1.23, System.currentTimeMillis() - (16 * 60 * 1000L));
        assertWithMessage("stale IOB should return null")
                .that(LoopIob.getFreshIob(15 * 60 * 1000L)).isNull();
    }

    @Test
    public void negativeIobCached() {
        // The loop cuts basal when BG drops; IOB can legitimately go negative.
        LoopIob.setIob(-0.45, System.currentTimeMillis());
        final Double result = LoopIob.getFreshIob(15 * 60 * 1000L);
        assertWithMessage("negative IOB should be cached and returned").that(result).isNotNull();
        assertWithMessage("negative IOB value should match").that(result).isWithin(0.001).of(-0.45);
    }

    @Test
    public void newerValueOverwrites() {
        LoopIob.setIob(2.0, System.currentTimeMillis() - (10 * 60 * 1000L));
        LoopIob.setIob(0.5, System.currentTimeMillis());
        final Double result = LoopIob.getFreshIob(15 * 60 * 1000L);
        assertWithMessage("newest IOB should win").that(result).isWithin(0.001).of(0.5);
    }
}
