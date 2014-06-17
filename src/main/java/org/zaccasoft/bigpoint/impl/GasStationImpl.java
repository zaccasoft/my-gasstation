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

public class GasStationImpl implements GasStation {

	private static Logger log = Logger.getLogger("my-gasstation");

	private Collection<GasPump> gasPumps = new ArrayList<GasPump>();
	private Map<GasType, Double> gasPrices = new HashMap<GasType, Double>();

	private int sales;

	private double revenue;

	private int cancelTooExpensive;

	private int cancelNoGas;

	public GasStationImpl() {
		super();

		init();
	}

	public void init() {
		// init properties
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

	private Properties loadProperties() throws FileNotFoundException, IOException, NullPointerException {
		InputStream i = getClass().getResourceAsStream("/application.properties");
		Properties p = new Properties();
		if (i == null) {
			log.fatal("Null input stream for poroperties file");
		}

		p.load(i);
		i.close();
		return p;
	}

	public void addGasPump(GasPump gasPump) {
		gasPumps.add(gasPump);
	}

	public double buyGas(GasType type, double amountInLiters, double maxPricePerLiter) throws NotEnoughGasException,
			GasTooExpensiveException {
		double priceToPay = -1d;

		double currentPrice = getPrice(type);

		if (currentPrice > maxPricePerLiter) {
			cancelTooExpensive++;
			throw new GasTooExpensiveException();
		}

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
					log.debug("Was looking for " + type + " and skipped " + p.getGasType());
					continue;
				}
			}
		}

		cancelNoGas++;
		throw new NotEnoughGasException();
	}

	public Collection<GasPump> getGasPumps() {
		return gasPumps;
	}

	public int getNumberOfCancellationsNoGas() {
		return cancelNoGas;
	}

	public int getNumberOfCancellationsTooExpensive() {
		return cancelTooExpensive;
	}

	public int getNumberOfSales() {
		return sales;
	}

	/**
	 * TODO: getPrice should throw an exception if the price is not set, and it
	 * couldn't be set to a default value.
	 * 
	 * A simple workaround, without touching the interface would be to set a
	 * default value by properties for all gas types.
	 */
	public double getPrice(GasType gasType) {
		Double response = gasPrices.get(gasType);
		if (response == null) {
			log.fatal("Prices not initialized");
			return -1d;
		}
		return response.doubleValue();
	}

	public double getRevenue() {
		return revenue;
	}

	public void setPrice(GasType gasType, double price) {
		gasPrices.put(gasType, price);
	}

}
