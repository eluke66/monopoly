/**
 * 
 */
package com.eluke.monopoly;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

/**
 * @author luke
 *
 */
public class PropertyFactory {

	/**
	 * @param o
	 * @return
	 */
	public static Property makeProperty(final Map<String,Object> rawPropertyInformation) {
		int price = intValue(rawPropertyInformation.get("price"));
		int mortgageValue = (int)(price * 0.5);
		int unMortgageValue = (int)(mortgageValue * 1.1);
		int houseCosts = rawPropertyInformation.containsKey("houseCost")?intValue(rawPropertyInformation.get("houseCost")):0;
		boolean canImprove = houseCosts > 0;
		List<Double> rentCostsList = (ArrayList<Double>)rawPropertyInformation.get("rentCosts");

		int[] rentCosts = rentCostsList.stream().mapToInt(new ToIntFunction<Double>() {
			@Override
			public int applyAsInt(final Double value) {
				return value.intValue();
			}}).toArray();

		return new Property(
				(String)rawPropertyInformation.get("name"),
				price,
				mortgageValue,
				unMortgageValue,
				MonopolySet.valueOf((String)rawPropertyInformation.get("monopolySet")),
				houseCosts,
				rentCosts,
				canImprove);
	}

	private static int intValue(final Object doubleObject) {
		return ((Double)doubleObject).intValue();
	}
}
