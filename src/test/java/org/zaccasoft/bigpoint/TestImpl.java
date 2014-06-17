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
import org.junit.Test;
import org.zaccasoft.bigpoint.impl.GasStationImpl;

/**
 * Purpose of the class is to test methods and implementation of Gas Station
 * 
 * @author frza
 *
 */
public class TestImpl {

	/*
	 * Application logger 
	 */
	private static Logger log = Logger.getLogger("my-gasstation");

	/*
	 * The implementation of Gas Station interface
	 */
	private GasStationImpl gsi;

	/**
	 * We get a fresh new Gas Station at each test iteration
	 */
	@Before
	public void setUp() {
		gsi = new GasStationImpl();
	}

	/**
	 * Test properties (only loading, then prints out the values)
	 */
	@Test
	public void testPropertiesloading() {
		log.debug("Starting testPropertiesloading()");

		for (GasType t : GasType.values()) {
			double currentValue = gsi.getPrice(t);
			assertEquals(1.0d, currentValue, 0);
			log.debug("Loaded value for " + t + " is " + currentValue);
		}

	}

	/**
	 * Test if gas pump can erogate
	 */
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

	/**
	 * Test the sales
	 * 
	 * TODO: to be tested in a full cycle
	 */
	@Test
	public void testSales() {
		log.debug("Starting testSales()");

		assertEquals(0, gsi.getNumberOfSales());

		assertNotNull(gsi.getNumberOfSales());
	}

	/**
	 * We try to buy gas when there's no pump and when the price is higher than expected
	 */
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
			assertEquals(1, gsi.getNumberOfCancellationsNoGas());
			log.debug("Yes, the first cancellation for no gas available");
		}
		
		GasPump gp1 = new GasPump(GasType.DIESEL, amount);
		gsi.addGasPump(gp1);
		double sold = 0d;
		try {
			sold = gsi.buyGas(GasType.DIESEL, amount, expectedPrice);
			log.debug("Yeah, enough gas to erogate!");
			
			assertEquals(gsi.getRevenue(), sold, 0);
			log.debug("Our first gas sale and our firse revenue!");
			
		} catch(Exception e) {
			fail();
		}
		
		expectedPrice = 0.9d;
		try {
			gsi.buyGas(GasType.DIESEL, amount, expectedPrice);			
		} catch(Exception e) {
			assertTrue(e instanceof GasTooExpensiveException);
			log.debug("Gas is too expensive, better luck next time");
			assertEquals(1, gsi.getNumberOfCancellationsTooExpensive());
			log.debug("Yes, the first cancellation for too expensive gas");
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
			double revenue = gsi.getRevenue();
			assertEquals(boughtAmount + sold, revenue, 0);
			log.debug("Yeah, 2 sales for revenue: "+ revenue);
			
		} catch(Exception e) {
			fail();
		}
		
		try {
			expectedPrice = 1.0d;
			amount = 1.0d;
			double testAmount = 1.0d;
			
			double boughtAmount = gsi.buyGas(GasType.DIESEL, amount, expectedPrice);
			assertEquals(testAmount, boughtAmount, 0);
			
			//we are serving from pump 2, because pump 1 is empty now
			log.debug("Bought for: " + boughtAmount + " remaining " + gp2.getRemainingAmount() + "litres.");
		} catch(Exception e) {
			fail();
		}
		
	}

}
