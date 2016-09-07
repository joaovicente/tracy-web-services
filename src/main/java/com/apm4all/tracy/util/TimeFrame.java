package com.apm4all.tracy.util;

import com.apm4all.tracy.apimodel.TaskConfig;

public class TimeFrame {
	long earliest;
	long latest;
	long snap;

    private void timeFrame(String earliest, String latest, String snap, TaskConfig taskConfig)   {
        try	{
            // Prefer creating TimeFrame from REST params (earliest, latest and snap)
            this.earliest = Long.parseLong(earliest);
            this.latest = Long.parseLong(latest);
            this.snap = Long.parseLong(snap);
        }
        catch( NullPointerException | NumberFormatException ex )
        {
            // If not supplied/valid use now and TaskConfig
            long now = System.currentTimeMillis();
            this.snap = taskConfig.getMeasurement().getSnap();
            this.latest = now - now % this.snap;
            this.earliest = this.latest - taskConfig.getMeasurement().getSpan();
        }
    }

	public TimeFrame(String earliest, String latest, String snap, TaskConfig taskConfig) {
        timeFrame(earliest, latest, snap, taskConfig);
    }

    public TimeFrame(String earliest, String latest, TaskConfig taskConfig) {
        final String ANY_VALID_INTEGER_AS_STRING = "0";
        timeFrame(earliest, latest, ANY_VALID_INTEGER_AS_STRING, taskConfig);
    }

	public long getEarliest() {
		return earliest;
	}

	public long getLatest() {
		return latest;
	}

	public long getSnap() {
		return snap;
	}
}
