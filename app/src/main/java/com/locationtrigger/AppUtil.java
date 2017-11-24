package com.locationtrigger;

import com.firebase.jobdispatcher.JobTrigger;
import com.firebase.jobdispatcher.Trigger;

/**
 * Created by nikunj on 22/11/17.
 */

public class AppUtil {
    public static JobTrigger periodicTrigger(int frequency, int tolerance) {
        return Trigger.executionWindow(frequency - tolerance, frequency);
    }
}
