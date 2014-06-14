package org.zaccasoft.bigpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

import org.apache.log4j.Logger;
import org.junit.Before;
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

		try {
			double boughtAmount = gsi.buyGas(GasType.DIESEL, 10d, 11d);
			log.debug("Bought for: " + boughtAmount);
		} catch (NotEnoughGasException nege) {
			log.debug("Not enough gas to erogate");
		} catch (GasTooExpensiveException gtee) {
			log.debug("Gas is too expensive, better luck next time");
		}

	}

}
