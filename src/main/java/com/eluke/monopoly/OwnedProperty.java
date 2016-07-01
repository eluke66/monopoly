/**
 *
 */
package com.eluke.monopoly;

/**
 * @author luke
 *
 */
public class OwnedProperty {
	private final Property property;
	private Player owner;
	private int numImprovements = 0;
	private boolean isMortgaged;
	private boolean partOfMonopoly = false;

	public enum RentOverride {
		DOUBLE,
		TEN_TIMES
	};

	public OwnedProperty(final Property property) {
		this.property = property;
	}
	public OwnedProperty(final Property property, final Player owner) {
		this.property = property;
		this.owner = owner;
	}

	public Property getProperty() {
		return property;
	}

	public Player getOwner() {
		return owner;
	}

	public int getNumImprovements() {
		return numImprovements;
	}

	public void setOwner(final Player player) {
		this.owner = player;
	}

	public void improve() {
		if (this.isMortgaged) {
			throw new IllegalStateException("Cannot improve " + this + " as it is mortgaged");
		}
		numImprovements++;
		if (numImprovements >= this.property.getRentCosts().length) {
			throw new IllegalStateException("Cannot add improvements on " + this + " as it has no more improvements");
		}
	}

	public void removeImprovement() {
		numImprovements--;
		if (numImprovements < 0) {
			throw new IllegalStateException("Cannot remove improvements on " + this + " as it has no more improvements");
		}
	}
	public void mortgage() {
		if (isMortgaged) {
			throw new IllegalStateException("Cannot mortgage " + this + " as it is already mortgaged");
		}
		this.isMortgaged = true;
		this.owner.receiveCash(property.getMortgageValue());
	}

	public void unmortgage() {
		if (!isMortgaged) {
			throw new IllegalStateException("Cannot unmortgage " + this + " as it is not mortgaged");
		}
		int cashRequired = this.property.getUnMortgageValue();
		this.owner.payMoney(cashRequired);
		this.isMortgaged = false;
	}

	public int getRent(final int diceValue, final RentOverride override) {
		if (isMortgaged) { return 0; }
		 if (override == RentOverride.TEN_TIMES) {
				return diceValue * 10;
		 }
		int baseRent = getRent(diceValue);
		if (override == RentOverride.DOUBLE) {
			return baseRent * 2;
		}
		else {
			return baseRent;
		}

	}
	public int getRent(final int diceValue) {
		if (isMortgaged) { return 0; }

		if (partOfMonopoly && numImprovements == 0 && property.canImprove()) {
			return property.getRentCosts()[0] * 2;
		}
		if (property.getMonopolySet() == MonopolySet.Utility) {
			return diceValue * property.getRentCosts()[numImprovements];
		}
		return property.getRentCosts()[numImprovements];
	}

	public boolean isMortgaged() {
		return this.isMortgaged;
	}

	public void setAsPartOfMonopoly(final boolean value) {
		this.partOfMonopoly = value;
	}

	public boolean isPartOfMonopoly() {
		return partOfMonopoly;
	}

	public ImprovementType getNextImprovementType() {
		if (numImprovements <= property.getRentCosts().length-2) {
			return ImprovementType.House;
		}
		return ImprovementType.Hotel;
	}

	public ImprovementType getCurrentImprovementType() {
		if (numImprovements == property.getRentCosts().length-1) {
			return ImprovementType.Hotel;
		}
		return ImprovementType.House;
	}

}
