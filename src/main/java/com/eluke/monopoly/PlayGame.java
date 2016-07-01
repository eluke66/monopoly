package com.eluke.monopoly;

import com.eluke.monopoly.strategy.AggressiveBuilderStrategy;
import com.google.common.collect.ImmutableList;

public class PlayGame {
	private static final int STARTING_CASH = 2000;

	public static void main(final String[] args) {
		AggressiveBuilderStrategy strategy = new AggressiveBuilderStrategy();
		Player player1 = new Player(STARTING_CASH, strategy, "Player 1");
		Player player2 = new Player(STARTING_CASH, strategy, "Player 2");

		ConfigLoader config = new ConfigLoader();
		Player winner = new Game(
				ImmutableList.of(player1,player2),
				config.getProperties(),
				config.getChanceCards(),
				config.getCommunityChestCards(),
				config.getSpaces(), new Dice()).play();

		System.err.println("Winner is " + winner);
	}

}
