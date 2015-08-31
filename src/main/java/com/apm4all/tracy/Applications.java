package com.apm4all.tracy;

import java.util.ArrayList;
import java.util.LinkedHashMap;

//{
//	  "applications": [
//	    {
//	      "name": "Demo App",
//	      "type": "application",
//	      "children": [
//	        {
//	          "name": "bursty",
//	          "type": "task"
//	        },
//	        {
//	          "name": "Not so fast",
//	          "type": "task"
//	        }
//	      ]
//	    }
//	  ]
//}


public class Applications {
	ArrayList<Object> applications;
	public Applications()	{
		
		// Initialize applications
		applications = new ArrayList<Object>();
		
		// Create demoApp
		LinkedHashMap<String,Object> demoApp = new LinkedHashMap<String,Object>();
		demoApp.put("name", "Demo App");
		demoApp.put("type", "application");
		
		// Now create demoAppTasks
		ArrayList<Object> demoAppTasks = new ArrayList<Object>();
		
		// Create bursty and add it to the demoAppTasks list
		LinkedHashMap<String,Object> bursty = new LinkedHashMap<String,Object>();
		bursty.put("name", "bursty");
		bursty.put("type", "task");
		demoAppTasks.add(bursty);
		
		// Create notSoFast and add it to the demoAppTasks list
		LinkedHashMap<String,Object> notSoFast = new LinkedHashMap<String,Object>();
		notSoFast.put("name", "Not so fast");
		notSoFast.put("type", "task");
		demoAppTasks.add(notSoFast);
	
		// Now set demoAppTasks as children of demoApp
		demoApp.put("children", demoAppTasks);
		
		// Finally add demoApp to applications list
		applications.add(demoApp);
		
	}
	
	public ArrayList<Object> getApplications()	{
		return applications;
	}
}
