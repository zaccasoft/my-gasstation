package org.zaccasoft.bigpoint.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasStation;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

/**
 * GasStationImpl implements GasStation. Usage is simple: instantiate the class
 * and then add gas pumps. A global price can be set for each type of gas.
 * 
 * @author frza
 * 
 */
public class GasStationImpl implements GasStation {
	/*
	 * Standard java logger
	 */
	private static Logger log = Logger.getLogger("my-gasstation");

	/*
	 * The collection of Gas Pumps available at the Gas Station
	 */
	private Collection<GasPump> gasPumps = new ArrayList<GasPump>();

	/*
	 * Gas type prices map
	 */
	private Map<GasType, Double> gasPrices = new HashMap<GasType, Double>();

	/*
	 * field to store the number of successful gas sales
	 */
	private int sales;

	/*
	 * field to collect the total revenue
	 */
	private double revenue;

	/*
	 * field to collect the cancellations due to high price of gas
	 */
	private int cancelTooExpensive;

	/*
	 * field to collect the cancellations due to depleted reserves of gas
	 */
	private int cancelNoGas;

	/*
	 * public constructor, to instantiate the class
	 */
	public GasStationImpl() {
		// calls super constructor
		super();

		// init properties
		init();
	}

	/**
	 * public method init() can be called to reset the initial status of gas
	 * station
	 */
	public void init() {
		// we'll read default properties from application.properties file
		try {
			Properties p = loadProperties();
			setPrice(GasType.DIESEL, Double.valueOf(p.getProperty("diesel.default.price", "-2")));
			setPrice(GasType.REGULAR, Double.valueOf(p.getProperty("regular.default.price", "-2")));
			setPrice(GasType.SUPER, Double.valueOf(p.getProperty("super.default.price", "-2")));
		} catch (FileNotFoundException fnfe) {
			log.fatal("Properties file is missing", fnfe);
		} catch (IOException ioe) {
			log.fatal("Could not access to properties file", ioe);
		} catch (NullPointerException npe) {
			log.fatal("File not found", npe);
		}

		// init variables
		sales = 0;
		revenue = 0;
		cancelNoGas = 0;
		cancelTooExpensive = 0;
	}

	/**
	 * Private method loadProperties() loads properties from a file (should be
	 * put in a field to be injected)
	 * 
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NullPointerException
	 */
	private Properties loadProperties() throws FileNotFoundException, IOException, NullPointerException {
		InputStream i = getClass().getResourceAsStream("/application.properties");
		Properties p = new Properties();
		if (i == null) {
			log.fatal("Null input stream for properties file");
		}
		p.load(i);
		// remember to close the stream
		i.close();
		return p;
	}

	/**
	 * Simply add a GasPump
	 */
	public void addGasPump(GasPump gasPump) {
		gasPumps.add(gasPump);
	}

	/**
	 * Buys gas and throws exceptions if not met requirements (enough gas and
	 * sold at a reasonable price).
	 * 
	 * @return The amount to be paid
	 * 
	 * @throws NotEnoughGasException
	 * @throws GasTooExpensiveException
	 */
	public double buyGas(GasType type, double amountInLiters, double maxPricePerLiter) throws NotEnoughGasException,
			GasTooExpensiveException {
		double priceToPay = -1d;

		// if the gas price could be variable we should syncronize getPrice on
		// the prices map
		double currentPrice = getPrice(type);

		// the customer goes away due to the high price of gas
		if (currentPrice > maxPricePerLiter) {
			// we should remember to record the cancellation
			cancelTooExpensive++;
			throw new GasTooExpensiveException();
		}

		// let's cycle through the pumps
		for (GasPump p : gasPumps) {
			synchronized (p) {
				/*
				 * TODO: there's no isPumpFree method to check if it is free or
				 * not. Locking the pump leads to queue too and several pumps
				 * could not scale, due to first come first served queue
				 */
				if (p.getGasType().equals(type)) {
					log.debug("Ok, found a " + type + " pump!");
					// the current pump is locked, so concurrent customers must
					// queue and wait the gas sale
					if (p.getRemainingAmount() >= amountInLiters) {
						double originalAmount = p.getRemainingAmount();

						// serve
						priceToPay = amountInLiters * currentPrice;
						p.pumpGas(amountInLiters);
						log.debug("From " + originalAmount + " asked for " + amountInLiters + " left "
								+ p.getRemainingAmount());

						log.debug("Current revenue: " + revenue);
						revenue += priceToPay;
						log.debug("Updated revenue " + revenue + " due to the selling of " + priceToPay);

						return priceToPay;
					}

				} else {
					//th customer was looking for another type of gas
					log.debug("Was looking for " + type + " and skipped " + p.getGasType());
					continue;
				}
			}
		}

		//the customer cannot be satisfied due to the end of gas
		//remember to increment the cancellation counter
		cancelNoGas++;
		throw new NotEnoughGasException();
	}

	/**
	 * Simple accessor to the Gas Pumps installed in the Gas Station
	 */
	public Collection<GasPump> getGasPumps() {
		return gasPumps;
	}

	/**
	 * Get the number of cancellations due to depletion of gas stocks
	 */
	public int getNumberOfCancellationsNoGas() {
		return cancelNoGas;
	}

	/**
	 * Get the number of cancellations due to the high price of gas
	 */
	public int getNumberOfCancellationsTooExpensive() {
		return cancelTooExpensive;
	}

	/**
	 * Get the total number of sales
	 */
	public int getNumberOfSales() {
		return sales;
	}

	/**
	 * Get the price related to a gas type.
	 * 
	 * TODO: getPrice should throw an exception if the price is not set, and it
	 * couldn't be set to a default value.
	 * 
	 * A simple workaround, without touching the interface would be to set a
	 * default value by properties for all gas types.
	 */
	public double getPrice(GasType gasType) {
		Double response = gasPrices.get(gasType);
		if (response == null) {
			//should throw an exception
			log.fatal("Prices not initialized");
			return -1d;
		}
		return response.doubleValue();
	}

	/**
	 * Get the total revenue of sells
	 */
	public double getRevenue() {
		return revenue;
	}

	/**
	 * Set the price of a gas type.
	 * 
	 * TODO: Does not check if there's a correspondent pump that sells the gas type
	 */
	public void setPrice(GasType gasType, double price) {
		gasPrices.put(gasType, price);
	}

}
