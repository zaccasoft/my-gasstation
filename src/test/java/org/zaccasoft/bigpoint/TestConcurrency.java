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

/**
 * Purpose of the class is to test concurrency on Gas Station and Gas Pumps
 * 
 * @author frza
 *
 */
public class TestConcurrency {

	/*
	 * Application logger
	 */
	private static Logger log = Logger.getLogger("my-gasstation");

	/*
	 * The implementation of Gas Station interface
	 */
	private GasStationImpl gsi;

	/*
	 * Total threads to create during the test
	 */
	private int threadCount = 1000;

	/*
	 * The amount to serve to each customer (keep it low to achieve a shot testing time, due the fixed time of erogation in GasPump)
	 */
	private double serveEach = 0.5d;

	/*
	 * Simple and straightforward price.
	 */
	private double price = 1.0d;

	/*
	 * A dynamic list of gas types
	 */
	private static List<GasType> gasType = new ArrayList<GasType>();
	
	/**
	 * Let's fill the Gas Station with enough pumps and gas to serve all the expected customers 
	 */
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

	/**
	 * Try to test the concurrency with threads
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testALotOfPeople() throws InterruptedException, ExecutionException {
		askForGas(threadCount);
	}

	/**
	 * Instead of writing whole test is cleaner to call the threaded tests with a counter
	 * 
	 * @param threadCount
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private void askForGas(final int threadCount) throws InterruptedException, ExecutionException {
		long setup = System.currentTimeMillis();
		final CustomerNumberGenerator customerId = new CustomerNumberGenerator();
		final RandomGasTypeGenerator gas = new RandomGasTypeGenerator();
		
		/*
		 * The task is: create a customer, pick a random gas type and try to serve the customer.
		 */
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
		//a simple way to check if concurrency is ok
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
