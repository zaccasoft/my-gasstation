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

	private int sales = -1;

	public GasStationImpl() {
		super();

		init();
	}
	
	public void init() {
		//init properties
		try {
			Properties p = loadProperties();
			gasPrices.put(GasType.DIESEL, Double.valueOf(p.getProperty("diesel.default.price", "-2")));
			gasPrices.put(GasType.REGULAR, Double.valueOf(p.getProperty("regular.default.price", "-2")));
			gasPrices.put(GasType.SUPER, Double.valueOf(p.getProperty("super.default.price", "-2")));
		} catch (FileNotFoundException fnfe) {
			log.fatal("Properties file is missing", fnfe);
		} catch (IOException ioe) {
			log.fatal("Could not access to properties file", ioe);
		} catch(NullPointerException npe) {
			log.fatal("File not found", npe);
		}
		
		//init variables
		sales = 0;
		
	}

	private Properties loadProperties() throws FileNotFoundException, IOException, NullPointerException {
		InputStream i = getClass().getResourceAsStream("/application.properties");
		Properties p = new Properties();
		if(i == null) {
			log.fatal("Null input stream for poroperties file");
		}
		
		p.load(i);
		i.close();
		return p;
	}

	public void addGasPump(GasPump gasPump) {
		gasPumps.add(gasPump);
	}

	public double buyGas(GasType arg0, double arg1, double arg2)
			throws NotEnoughGasException, GasTooExpensiveException {
		// TODO Auto-generated method stub
		return 0;
	}

	public Collection<GasPump> getGasPumps() {
		return gasPumps;
	}

	public int getNumberOfCancellationsNoGas() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getNumberOfCancellationsTooExpensive() {
		// TODO Auto-generated method stub
		return 0;
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
		// TODO Auto-generated method stub
		return 0;
	}

	public void setPrice(GasType arg0, double arg1) {
		// TODO Auto-generated method stub

	}

}
