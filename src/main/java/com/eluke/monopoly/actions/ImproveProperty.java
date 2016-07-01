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
public class ImproveProperty implements PlayerPropertyAction {
	private final OwnedProperty propertyToImprove;

	public ImproveProperty(final OwnedProperty propertyToImprove) {
		this.propertyToImprove = propertyToImprove;
	}

	@Override
	public void execute(final Game game) {
		ImprovementType improvementType = propertyToImprove.getNextImprovementType();
		if (!game.moreImprovementsAvailable(improvementType)) {
			throw new IllegalStateException("Trying to improve " + propertyToImprove + " with a " + improvementType + " when no more are available");
		}
		int cashRequired = propertyToImprove.getProperty().getHouseCost();
		propertyToImprove.getOwner().payMoney(cashRequired);
		propertyToImprove.improve();
		game.markPropertyAsUsed(improvementType);
	}

	@Override
	public String toString() {
		return "Improve " + propertyToImprove;
	}

	@Override
	public OwnedProperty getProperty() {
		return propertyToImprove;
	}
}
