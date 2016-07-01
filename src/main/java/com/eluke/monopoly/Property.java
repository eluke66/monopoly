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

	@Override
	public String toString() {
		return name;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Property other = (Property) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
