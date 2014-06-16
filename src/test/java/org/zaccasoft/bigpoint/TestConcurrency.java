package org.zaccasoft.bigpoint;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.zaccasoft.bigpoint.impl.GasStationImpl;

public class TestConcurrency {

	private static Logger log = Logger.getLogger("my-gasstation");

	private GasStationImpl gsi;

	private int threadCount = 1000;

	private double serveEach = 0.5d;

	private double price = 1.0d;

	private static List<GasType> gasType = new ArrayList<GasType>();

	@Before
	public void setUp() {
		gsi = new GasStationImpl();
		gsi.addGasPump(new GasPump(GasType.DIESEL, threadCount * serveEach));
		gsi.addGasPump(new GasPump(GasType.REGULAR, threadCount * serveEach));
		gsi.addGasPump(new GasPump(GasType.SUPER, threadCount * serveEach));
		gsi.setPrice(GasType.DIESEL, price);
		gsi.setPrice(GasType.REGULAR, price);
		gsi.setPrice(GasType.SUPER, price);

		gasType = new ArrayList<GasType>();

		gasType.add(GasType.DIESEL);
		gasType.add(GasType.REGULAR);
		gasType.add(GasType.SUPER);

	}

	@Test
	public void testALotOfPeople() throws InterruptedException, ExecutionException {
		askForGas(threadCount);
	}

	private void askForGas(final int threadCount) throws InterruptedException, ExecutionException {
		long setup = System.currentTimeMillis();
		final CustomerNumberGenerator customerId = new CustomerNumberGenerator();
		final RandomGasTypeGenerator gas = new RandomGasTypeGenerator();

		Callable<Double> task = new Callable<Double>() {
			public Double call() throws NotEnoughGasException, GasTooExpensiveException {
				Long response = customerId.nextId();
				log.debug("I'm customer " + response);
				double amountToPay = gsi.buyGas(gas.pickRandomGasType(), serveEach, price);
				log.debug("Amount to pay for customer " + response + " is " + amountToPay);
				return amountToPay;
			}
		};

		List<Callable<Double>> tasks = Collections.nCopies(threadCount, task);
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

		long start = System.currentTimeMillis();
		log.debug("-- Invoking all threads");
		long setupTime = System.currentTimeMillis() - setup;
		executorService.invokeAll(tasks);
		log.debug("-- Total revenue:" + gsi.getRevenue());
		long elapsed = System.currentTimeMillis() - start;
		double estimated = 100 /* ms pump busy */* threadCount * serveEach / gasType.size();
		log.debug("elapsed: " + elapsed + "ms - estimated: " + estimated + "ms - setupTime: " + setupTime + "ms ");
		assertTrue((estimated + setupTime) * 1.2d >= elapsed);
	}

	/**
	 * Purpose of the class is to get a unique id for each customer
	 * 
	 * @author frza
	 * 
	 */
	static class CustomerNumberGenerator {
		private final AtomicLong counter = new AtomicLong();

		public long nextId() {
			return counter.incrementAndGet();
		}
	}

	/**
	 * Purpose of the class is to get a random gas type on each call
	 * 
	 * @author frza
	 * 
	 */
	static class RandomGasTypeGenerator {
		public GasType pickRandomGasType() {
			Random r = new Random();
			return gasType.get(r.nextInt(gasType.size()));
		}
	}
}
