package com.apm4all.tracy.services;

import com.apm4all.tracy.measurement.application.ApplicationMeasurement;
import com.apm4all.tracy.simulations.StaticApplicationMeasurement;

public class ApplicationMeasurementService {
    /**
     * Gets a measurement for a given application, task
     *
     * @param application the application name
     * @param task the task name
     * @return the measurement, or <tt>null</tt> if the application or task are invalid 
     */
    public ApplicationMeasurement getApplicationMeasurement(String application) {
    	ApplicationMeasurement applicationMeasurement = null;
    	if (application.equals("SimulatedApp"))	{
    		applicationMeasurement = new StaticApplicationMeasurement(application);
    	}
    	return applicationMeasurement;
    }
}