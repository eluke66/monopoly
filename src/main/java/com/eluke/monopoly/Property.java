/**
 * 
 */
package com.eluke.monopoly;

/**
 * @author luke
 *
 */
public class Property {
	private final String name;
	private final int buyPrice;
	private final int mortgageValue;
	private final int unMortgageValue;
	private final MonopolySet monopolySet;
	private final int houseCost;
	private final int[] rentCosts;
	private final boolean canImprove;

	public Property(final String name, final int buyPrice, final int mortgageValue, final int unMortgageValue,
			final MonopolySet monopolySet, final int houseCost,
			final int[] rentCosts, final boolean canImprove) {
		this.name = name;
		this.buyPrice = buyPrice;
		this.mortgageValue = mortgageValue;
		this.unMortgageValue = unMortgageValue;
		this.monopolySet = monopolySet;
		this.houseCost = houseCost;
		this.rentCosts = rentCosts;
		this.canImprove = canImprove;
	}
	public int getBuyPrice() {
		return buyPrice;
	}
	public int getMortgageValue() {
		return mortgageValue;
	}
	public int getUnMortgageValue() {
		return unMortgageValue;
	}
	public MonopolySet getMonopolySet() {
		return monopolySet;
	}
	public int getHouseCost() {
		return houseCost;
	}
	public int[] getRentCosts() {
		return rentCosts;
	}
	public boolean canImprove() {
		return canImprove;
	}
	public String getName() {
		return name;
	}
}
