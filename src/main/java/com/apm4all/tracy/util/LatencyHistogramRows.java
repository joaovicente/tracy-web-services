package com.apm4all.tracy.util;

import java.util.ArrayList;
import java.util.List;

public class LatencyHistogramRows {
	public static final int BIN_SIZE_FACTOR_DEFAULT = 2;
	public static final String FRUSTRATED_ZONE_NAME = "Frustrated";
	public static final String TOLERATING_ZONE_NAME = "Tolerating";
	public static final String SATISFIED_ZONE_NAME = "Satisfied";
	List<LatencyHistogramRow> rows;
	int binSize;
	int binSizeFactor = BIN_SIZE_FACTOR_DEFAULT;
	
	public LatencyHistogramRows(int rttTolerating, int rttFrustrated) {
		binSize = rttTolerating/BIN_SIZE_FACTOR_DEFAULT;
		rows = new ArrayList<LatencyHistogramRow>(rttFrustrated/binSize+1);
		int binLowerLimit = rttFrustrated;
		String rttZone;
		while (binLowerLimit>=0)	{
			if (binLowerLimit == rttFrustrated)	{
				// Frustrated
				rttZone = FRUSTRATED_ZONE_NAME;
				String label = ">" + Integer.toString(binLowerLimit);
				rows.add(new LatencyHistogramRow(binLowerLimit, label, rttZone));
			}
			else	{
				int binUpperLimit = binLowerLimit+binSize;
				String label = Integer.toString(binLowerLimit) + "-" + Integer.toString(binUpperLimit);
				if (binLowerLimit >= rttTolerating)	{
					rttZone = TOLERATING_ZONE_NAME;
				}
				else	{
					rttZone = SATISFIED_ZONE_NAME;
				}
				rows.add(new LatencyHistogramRow(binLowerLimit, binUpperLimit, label, rttZone));
			}
			binLowerLimit-=binSize;
		}
	}
	
	public List<LatencyHistogramRow> asList() {
		return rows;
	}

	public int getBinSizeFactor() {
		return binSizeFactor;
	}

	public static class LatencyHistogramRow {
		String label;
		boolean hasUpperLimit = true;
		int upperLimit = 0;
		int lowerLimit = 0;
		String rttZone;
		
		public LatencyHistogramRow(int lowerLimit, int upperLimit, String label, String rttZone) {
			super();
			this.label = label;
			this.upperLimit = upperLimit;
			this.lowerLimit = lowerLimit;
			this.hasUpperLimit = true;
			this.rttZone = rttZone;
		}

		public LatencyHistogramRow(int lowerLimit, String label, String rttZone) {
			super();
			this.label = label;
			this.lowerLimit = lowerLimit;
			this.hasUpperLimit = false;
			this.rttZone = rttZone;
		}

		public String getLabel() {
			return label;
		}
		public int getUpperLimit() {
			return upperLimit;
		}
		public int getLowerLimit() {
			return lowerLimit;
		}

		public boolean hasUpperLimit() {
			return hasUpperLimit;
		}

		public String getRttZone() {
			return rttZone;
		}

	}


}
