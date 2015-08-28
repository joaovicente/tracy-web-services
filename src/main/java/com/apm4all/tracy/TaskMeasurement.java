package com.apm4all.tracy;

@SuppressWarnings("unused")
public class TaskMeasurement {
	private DapdexTimechart dapdexTimechart = new DapdexTimechart(); 
	private VitalsTimechart vitalsTimechart = new VitalsTimechart();
	private LatencyHistogram latencyHistogram = new LatencyHistogram();
    private String measurement =  
    "{"
    + "'dapdexTimechart':{"
    	+ "'timeSequence':[1439312400000,1439312401900,1439312403800,1439312405700,1439312407600,1439312409500,1439312411400,1439312413300,1439312415200,1439312417100,1439312419000,1439312420900,1439312422800,1439312424700,1439312426600,1439312428500],"
    	+ "'dapdexScores':[0.99,0.98,0.99,0.93,0.94,0.97,0.95,0.97,0.83,0.93,0.97,0.94,0.96,0.98,0.95,0.96]"
    + "},"
    + "'vitalsTimechart\":{"
    	+ "'timeSequence':[1439312400000,1439312401900,1439312403800,1439312405700,1439312407600,1439312409500,1439312411400,1439312413300,1439312415200,1439312417100,1439312419000,1439312420900,1439312422800,1439312424700,1439312426600,1439312428500],"
    	+ "'count':[200,243,254,234,253,265,245,247,765,243,265,273,247,256,236,245],"
    	+ "'errors':[1,2,1,2,1,2,1,2,4,1,2,1,2,1,2,1],"
    	+ "'p95':[110,132,141,143,151,134,123,131,111,125,123,143,122,156,116,145]},"
    + "'latencyHistogram':{"
    	+ "'bins':['>1200','1140-1200','1080-1139','1020-1079','960-1019','900-959','840-899','780-839','720-779','660-719','600-659','540-599','480-539','420-479','360-419','300-359','240-299','180-239','120-179','60-119','0-59'],"
    	+ "'rttZone':['Frustrated','Tolerating','Tolerating','Tolerating','Tolerating','Tolerating','Tolerating','Tolerating','Tolerating','Tolerating','Tolerating','Tolerating','Tolerating','Tolerating','Tolerating','Tolerating','Satisfied','Satisfied','Satisfied','Satisfied','Satisfied'],"
    	+ "'count':[4,8,22,22,22,22,76,89,44,134,134,134,178,178,268,447,670,1548,312,89,44]}"
    + "}";
    
    private class DapdexTimechart {
    	private Long[] timeSequence = {1439312400000L,1439312401900L,1439312403800L,1439312405700L,1439312407600L,1439312409500L,1439312411400L,1439312413300L,1439312415200L,1439312417100L,1439312419000L,1439312420900L,1439312422800L,1439312424700L,1439312426600L,1439312428500L};
    	private Double[] dapdexScores = {0.99,0.98,0.99,0.93,0.94,0.97,0.95,0.97,0.83,0.93,0.97,0.94,0.96,0.98,0.95,0.96};
		public Long[] getTimeSequence() {
			return timeSequence;
		}
		public Double[] getDapdexScores() {
			return dapdexScores;
		}
    }
    private class VitalsTimechart {
    	private Long[] timeSequence = {1439312400000L,1439312401900L,1439312403800L,1439312405700L,1439312407600L,1439312409500L,1439312411400L,1439312413300L,1439312415200L,1439312417100L,1439312419000L,1439312420900L,1439312422800L,1439312424700L,1439312426600L,1439312428500L};
    	private int[] count = {200,243,254,234,253,265,245,247,765,243,265,273,247,256,236,245};
    	private int[] errors = {1,2,1,2,1,2,1,2,4,1,2,1,2,1,2,1};
    	private int[] p95 = {110,132,141,143,151,134,123,131,111,125,123,143,122,156,116,145};
		public Long[] getTimeSequence() {
			return timeSequence;
		}
		public int[] getCount() {
			return count;
		}
		public int[] getErrors() {
			return errors;
		}
		public int[] getP95() {
			return p95;
		}
    }
    private class LatencyHistogram {
    	private String[] bins = {"1200","1140-1200","1080-1139","1020-1079","960-1019","900-959","840-899","780-839","720-779","660-719","600-659","540-599","480-539","420-479","360-419","300-359","240-299","180-239","120-179","60-119","0-59"};
    	private String[] rttZone = {"Frustrated","Tolerating","Tolerating","Tolerating","Tolerating","Tolerating","Tolerating","Tolerating","Tolerating","Tolerating","Tolerating","Tolerating","Tolerating","Tolerating","Tolerating","Tolerating","Satisfied","Satisfied","Satisfied","Satisfied","Satisfied"};
    	private int[] count = {4,8,22,22,22,22,76,89,44,134,134,134,178,178,268,447,670,1548,312,89,44};
		public String[] getBins() {
			return bins;
		}
		public String[] getRttZone() {
			return rttZone;
		}
		public int[] getCount() {
			return count;
		}
    }
	public DapdexTimechart getDapdexTimechart() {
		return dapdexTimechart;
	}
	public VitalsTimechart getVitalsTimechart() {
		return vitalsTimechart;
	}
	public LatencyHistogram getLatencyHistogram() {
		return latencyHistogram;
	}
}
