package com.apm4all.tracy;

import java.util.ArrayList;

public class Applications {
	ArrayList<Application> applicationList = new ArrayList<Application>();
	
	public Applications() {
		Task bursty = new Task("Bursty");
		Task notSoFast = new Task("Not so fast");
		Application demoApp = new Application("demoApp");
		demoApp.addTask(bursty);
		demoApp.addTask(notSoFast);
		applicationList.add(demoApp);
	}
	
	public Application[] getApplications() {
		Application[] a = new Application[applicationList.size()];
		applicationList.toArray(a);
		return a;
	}

	private class Application {
		String name;
		private ArrayList<Task> taskList = new  ArrayList<Task>();
		
		public Application(String name)	{
			this.name = name;
		}
		public void addTask(Task task) {
			taskList.add(task);
		}
		@SuppressWarnings("unused")
		public String getName() {
			return name;
		}
		@SuppressWarnings("unused")
		public Task[] getTasks() {
			Task[] t = new Task[taskList.size()];
			taskList.toArray(t);
			return t;
		}
	}
	
	private class Task {
		private String name;

		public Task(String name) {
			this.name = name;
		}

		@SuppressWarnings("unused")
		public String getName() {
			return name;
		}
	}
}
