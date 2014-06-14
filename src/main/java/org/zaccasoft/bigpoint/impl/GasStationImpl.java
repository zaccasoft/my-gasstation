package org.zaccasoft.bigpoint.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.zaccasoft.bigpoint.exceptions.PriceNotSetException;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasStation;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;

public class GasStationImpl implements GasStation {

	private Collection<GasPump> gasPumps = new ArrayList<GasPump>();
	private Map<GasType, Double> gasPrices = new HashMap<GasType, Double>();

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
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * TODO: getPrice should throw an exception if the price is not set, and it couldn't be set to a default value
	 * A simple workaround, without touching the interface would be to set a default value by properties for all gas types.
	 */
	public double getPrice(GasType gasType) throws PriceNotSetException {
		Double response = gasPrices.get(gasType);
		if(response == null) {
			throw new PriceNotSetException();
		} else {
			return response.doubleValue();
		}
	}

	public double getRevenue() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setPrice(GasType arg0, double arg1) {
		// TODO Auto-generated method stub

	}

}
