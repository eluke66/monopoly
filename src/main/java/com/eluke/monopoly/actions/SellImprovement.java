/**
 *
 */
package com.eluke.monopoly.actions;

import com.eluke.monopoly.Game;
import com.eluke.monopoly.ImprovementType;
import com.eluke.monopoly.OwnedProperty;

/**
 * @author luke
 *
 */
public class SellImprovement implements PlayerPropertyAction {
	private final OwnedProperty propertyToAffect;

	public SellImprovement(final OwnedProperty propertyToImprove) {
		this.propertyToAffect = propertyToImprove;
	}

	@Override
	public void execute(final Game game) {
		int cashRequired = propertyToAffect.getProperty().getHouseCost() / 2;
		propertyToAffect.getOwner().receiveCash(cashRequired);
		propertyToAffect.removeImprovement();
		ImprovementType improvementType = propertyToAffect.getCurrentImprovementType();
		game.markPropertyAsUnused(improvementType);
	}

	@Override
	public OwnedProperty getProperty() {
		return propertyToAffect;
	}

	@Override
	public String toString() {
		return "Unimprove " + propertyToAffect.getProperty().getName();
	}
}
