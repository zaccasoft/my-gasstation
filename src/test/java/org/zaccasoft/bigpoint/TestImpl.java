package org.zaccasoft.bigpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasType;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.zaccasoft.bigpoint.impl.GasStationImpl;

public class TestImpl {

	private static Logger log = Logger.getLogger("my-gasstation");

	@Test
	public void testImpl() {
		assertTrue(true);
		log.debug("yeah, it logs");
	}
	
	
	@Test
	public void testPropertiesloading() {
		log.debug("Starting testPropertiesloading()");
		
		GasStationImpl gsi = new GasStationImpl();
		
		for(GasType t : GasType.values()) {
			double currentValue = gsi.getPrice(t);
			assertEquals(1.0d, currentValue, 0);
			log.debug("Loaded value for "+t+" is "+currentValue);
		}
		
	}
	@Test
	public void testGSI() {
		GasStationImpl gsi = new GasStationImpl();
		
		assertTrue(gsi.getGasPumps().isEmpty());
		
		GasPump gp1 = new GasPump(GasType.DIESEL, 10d);		
		
		gsi.addGasPump(gp1);
		
		assertFalse(gsi.getGasPumps().isEmpty());
		
		assertEquals(1, gsi.getGasPumps().size());
		
		assertNotNull(gsi.getPrice(GasType.DIESEL));
		
	}

}
