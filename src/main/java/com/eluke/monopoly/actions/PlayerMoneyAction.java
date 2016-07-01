package com.eluke.monopoly.actions;

import java.util.List;

import com.eluke.monopoly.Game;
import com.eluke.monopoly.Player;

public class PlayerMoneyAction implements GameAction {
	private final int moneyFromPlayers;
	private final String description;

	public PlayerMoneyAction(final int income, final String description) {
		this.moneyFromPlayers = income;
		this.description = description;
	}

	@Override
	public void execute(final Player current, final int currentDiceRoll, final Game game) {
		List<Player> allPlayers = game.getActivePlayers();
		if (moneyFromPlayers > 0) {
			for (Player player : allPlayers) {
				if (!player.equals(current)) {
					player.payCashToPlayer(moneyFromPlayers, current, game);
				}
			}
		}
		else {
			int moneyToPlayers = -moneyFromPlayers;
			current.payCashToPlayers(moneyToPlayers, allPlayers, game);
		}
	}

	@Override
	public String getDescription() {
		return description;
	}
}
