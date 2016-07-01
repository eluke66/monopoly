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
public class UnmortgageProperty implements PlayerPropertyAction {
	private final OwnedProperty propertyToUnmortgage;

	public UnmortgageProperty(final OwnedProperty propertyToUnmortgage) {
		this.propertyToUnmortgage = propertyToUnmortgage;
	}

	@Override
	public void execute(final Game game) {
		if (!propertyToUnmortgage.isMortgaged()) {
			throw new IllegalStateException("Can't unmortage " + propertyToUnmortgage + " as it is not mortgaged!");
		}

		propertyToUnmortgage.unmortgage();
	}

	@Override
	public OwnedProperty getProperty() {
		return propertyToUnmortgage;
	}

	@Override
	public String toString() {
		return "Unmortgage " + propertyToUnmortgage.getProperty().getName();
	}
}
