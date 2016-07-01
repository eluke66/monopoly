package com.eluke.monopoly.actions;

import com.eluke.monopoly.Game;
import com.eluke.monopoly.Player;

public class AssessmentAction implements GameAction {
	private final int houseCost;
	private final int hotelCost;
	private final String description;

	public AssessmentAction(final int houseCost, final int hotelCost, final String description) {
		this.houseCost = houseCost;
		this.hotelCost = hotelCost;
		this.description = description;
	}

	@Override
	public void execute(final Player player, final int currentDiceRoll, final Game game) {
		int numHouses = player.getHouses();
		int numHotels = player.getHotels();

		int totalCashRequired = houseCost*numHouses + hotelCost*numHotels;
		player.payCashToPlayer(totalCashRequired, Player.BANK, game);
	}

	@Override
	public String getDescription() {
		return description;
	}
}
