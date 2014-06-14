package org.zaccasoft.bigpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.zaccasoft.bigpoint.impl.GasStationImpl;

public class TestImpl {

	private static Logger log = Logger.getLogger("my-gasstation");

	private GasStationImpl gsi;

	@Before
	public void setUp() {
		gsi = new GasStationImpl();
	}

	@Test
	public void testPropertiesloading() {
		log.debug("Starting testPropertiesloading()");

		for (GasType t : GasType.values()) {
			double currentValue = gsi.getPrice(t);
			assertEquals(1.0d, currentValue, 0);
			log.debug("Loaded value for " + t + " is " + currentValue);
		}

	}

	@Test
	public void testGasPumps() {
		log.debug("Starting testGasPumps()");

		double initialAmount = 10d;

		double pumpAmount = 1d;

		assertTrue(gsi.getGasPumps().isEmpty());

		GasPump gp1 = new GasPump(GasType.DIESEL, initialAmount);

		gsi.addGasPump(gp1);

		assertFalse(gsi.getGasPumps().isEmpty());

		assertEquals(1, gsi.getGasPumps().size());

		assertNotNull(gsi.getPrice(GasType.DIESEL));

		GasPump gp2 = new GasPump(GasType.REGULAR, initialAmount);
		gp2.pumpGas(pumpAmount);
		assertEquals(initialAmount - pumpAmount, gp2.getRemainingAmount(), 0);

		/**
		 * TODO: GasPump can erogate even if has a remaining amount <=0, an
		 * exception should be thrown
		 */
		gp2.pumpGas(initialAmount);
		log.debug("Remaining amount: " + gp2.getRemainingAmount());

	}

	@Test
	public void testSales() {
		log.debug("Starting testSales()");

		assertEquals(0, gsi.getNumberOfSales());

		assertNotNull(gsi.getNumberOfSales());
	}

	@Test
	public void testBuyGas() {
		log.debug("Starting testBuyGas()");
		
		double price = 1.0d;
		gsi.setPrice(GasType.DIESEL, price);
		
		double amount = 10d;
		double expectedPrice = 1.0d;
		
		try {
			gsi.buyGas(GasType.DIESEL, amount, expectedPrice);			
		} catch(Exception e) {
			assertTrue(e instanceof NotEnoughGasException);
			log.debug("Not enough gas to erogate");
		}
		
		GasPump gp1 = new GasPump(GasType.DIESEL, amount);
		gsi.addGasPump(gp1);
		
		try {
			gsi.buyGas(GasType.DIESEL, amount, expectedPrice);
			log.debug("Yeah, enough gas to erogate!");
		} catch(Exception e) {
			fail();
		}
		
		expectedPrice = 0.9d;
		try {
			gsi.buyGas(GasType.DIESEL, amount, expectedPrice);			
		} catch(Exception e) {
			assertTrue(e instanceof GasTooExpensiveException);
			log.debug("Gas is too expensive, better luck next time");
		}
		
		//to avoid the next denial of service we simply add a new pump
		GasPump gp2 = new GasPump(GasType.DIESEL, amount);
		gsi.addGasPump(gp2);
		
		try {
			expectedPrice = 1.0d;
			amount = 1.0d;
			double testAmount = 1.0d;
			
			double boughtAmount = gsi.buyGas(GasType.DIESEL, amount, expectedPrice);
			assertEquals(testAmount, boughtAmount, 0);
			
			log.debug("Bought for: " + boughtAmount);
		} catch(Exception e) {
			fail();
		}
		
		try {
			expectedPrice = 1.0d;
			amount = 1.0d;
			double testAmount = 1.0d;
			
			double boughtAmount = gsi.buyGas(GasType.DIESEL, amount, expectedPrice);
			assertEquals(testAmount, boughtAmount, 0);
			
			//safe, because we just added 1 pump only
			GasPump currentGasPump = gsi.getGasPumps().iterator().next();
			log.debug("Bought for: " + boughtAmount + " remaining " + currentGasPump.getRemainingAmount() + "litres.");
		} catch(Exception e) {
			fail();
		}
		
	}

}
