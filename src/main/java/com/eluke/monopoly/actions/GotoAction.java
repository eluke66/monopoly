package com.eluke.monopoly.actions;

import com.eluke.monopoly.Game;
import com.eluke.monopoly.MonopolySet;
import com.eluke.monopoly.OwnedProperty.RentOverride;
import com.eluke.monopoly.Player;

public class GotoAction implements GameAction {
	private final String location;
	private final String description;

	public GotoAction(final String location, final String description) {
		this.location = location;
		this.description = description;
	}

	@Override
	public void execute(final Player current, final int currentDiceRoll, final Game game) {
		if (location.equals("utility")) {
			goToNearestUtility(current, currentDiceRoll, game);
		}
		else if (location.equals("railroad")) {
			goToNearestRailroad(current, currentDiceRoll, game);
		}
		else if (location.equals("go")) {
			goToGo(current, currentDiceRoll, game);
		}
		else if (location.equals("jail")) {
			game.sendPlayerToJail(current);
		}
		else {
			try {
				if (location.charAt(0) == '-' || Character.digit(location.charAt(0),10) > 0) {
					Integer offset = Integer.parseInt(location);
					moveToRelativeOffset(current, currentDiceRoll, game, offset);
				}
				else {
					// Go to that property name
					moveToNamedProperty(current, currentDiceRoll, game);
				}

			} catch (NumberFormatException e) {
				// Go to that property name
				moveToNamedProperty(current, currentDiceRoll, game);
			}
		}
	}

	private void moveToNamedProperty(final Player current, final int currentDiceRoll, final Game game) {
		game.moveToNamedSpace(current, location);
		game.executeActionsForSpace(current, currentDiceRoll);
	}

	private void moveToRelativeOffset(final Player current, final int currentDiceRoll, final Game game, final Integer offset) {
		game.moveToRelativeOffset(current, offset);
		game.executeActionsForSpace(current, currentDiceRoll);
	}

	private void goToGo(final Player current, final int currentDiceRoll, final Game game) {
		game.movePlayerToGo(current);
	}

	private void goToNearestRailroad(final Player current, final int currentDiceRoll, final Game game) {
		game.goToNearestSpaceOfType(current,currentDiceRoll,MonopolySet.Railroad,RentOverride.DOUBLE);
	}

	private void goToNearestUtility(final Player current, final int currentDiceRoll, final Game game) {
		game.goToNearestSpaceOfType(current,currentDiceRoll,MonopolySet.Utility,RentOverride.TEN_TIMES);
	}

	@Override
	public String getDescription() {
		return description;
	}
}
