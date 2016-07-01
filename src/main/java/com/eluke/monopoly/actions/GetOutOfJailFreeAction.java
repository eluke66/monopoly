package com.eluke.monopoly.actions;

import com.eluke.monopoly.Game;
import com.eluke.monopoly.Player;

public class GetOutOfJailFreeAction implements GameAction {
	private final String description;

	public GetOutOfJailFreeAction(final String description) {
		this.description = description;
	}

	@Override
	public void execute(final Player current, final int currentDiceRoll, final Game game) {
		// TODO Auto-generated method stub

	}

	public String getDescription() {
		return description;
	}
}
