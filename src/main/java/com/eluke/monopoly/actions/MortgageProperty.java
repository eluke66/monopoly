/**
 *
 */
package com.eluke.monopoly.actions;

import com.eluke.monopoly.Game;
import com.eluke.monopoly.OwnedProperty;

/**
 * @author luke
 *
 */
public class MortgageProperty implements PlayerPropertyAction {
	private final OwnedProperty propertyToMortgage;

	public MortgageProperty(final OwnedProperty propertyToMortgage) {
		this.propertyToMortgage = propertyToMortgage;
	}

	@Override
	public void execute(final Game game) {
		if (propertyToMortgage.getNumImprovements() > 0) {
			throw new IllegalStateException("Can't mortage " + propertyToMortgage + " as it still has " + propertyToMortgage.getNumImprovements() + " improvements");
		}

		propertyToMortgage.mortgage();
	}

	@Override
	public OwnedProperty getProperty() {
		return propertyToMortgage;
	}

	@Override
	public String toString() {
		return "Mortgage " + propertyToMortgage.getProperty().getName();
	}
}
