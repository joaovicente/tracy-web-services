package com.apm4all.tracy.util;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.apm4all.tracy.util.LatencyHistogramRows.LatencyHistogramRow;

public class LatencyHistogramRowsTest {

	@Test
	public void test() {
		int TOLERATING = 200;
		int FRUSTRATED = 800;
		LatencyHistogramRows latencyHistogramRows = new LatencyHistogramRows(TOLERATING, FRUSTRATED);
		List<LatencyHistogramRow> latencyHistogramRowsAsList = latencyHistogramRows.asList();
		LatencyHistogramRow row;
		
		row = latencyHistogramRowsAsList.get(0);
		assertEquals(800, row.getLowerLimit());
		assertEquals(0, row.getUpperLimit());
		assertFalse(row.hasUpperLimit);
		assertEquals(">800", row.getLabel());
		assertEquals(LatencyHistogramRows.FRUSTRATED_ZONE_NAME, row.getRttZone());
//		System.out.println(row.getLowerLimit() + "," + row.getUpperLimit() + "," + row.getLabel());

	
		int binSize = TOLERATING / latencyHistogramRows.getBinSizeFactor();
		int numBins = FRUSTRATED / binSize;

		for (int i=1 ; i<=numBins ; i++)	{
			int expectedUpper = FRUSTRATED - (binSize*(i-1));
			int expectedLower = FRUSTRATED - (binSize*(i));
			String expectedLabel = Integer.toString(expectedLower) + "-" + Integer.toString(expectedUpper);
			row = latencyHistogramRowsAsList.get(i);
			assertEquals(expectedLower, row.getLowerLimit());
			assertEquals(expectedUpper, row.getUpperLimit());
			assertTrue(row.hasUpperLimit);
			assertEquals(expectedLabel, row.getLabel());
			if (expectedLower >= TOLERATING)	{
				assertEquals(LatencyHistogramRows.TOLERATING_ZONE_NAME, row.getRttZone());
				
			}
			else	{
				assertEquals(LatencyHistogramRows.SATISFIED_ZONE_NAME, row.getRttZone());
			}
//			System.out.println(row.getLowerLimit() + "," + row.getUpperLimit() + "," + row.getLabel());
		}
	}

}
