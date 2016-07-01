package com.eluke.monopoly;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.eluke.monopoly.cards.DrawCard;
import com.eluke.monopoly.spaces.Space;
import com.eluke.monopoly.strategy.AggressiveBuilderStrategy;
import com.google.common.collect.ImmutableList;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class ProfileGame {
	private static final int GAMES = 10000;
	private static final String PLAYER_2 = "Player 2";
	private static final String PLAYER_1 = "Player 1";
	private static final int STARTING_CASH = 2000;

	public static void main(final String[] args) {
		Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.ERROR);
		AggressiveBuilderStrategy strategy = new AggressiveBuilderStrategy();

		ConfigLoader config = new ConfigLoader();
		Set<Property> properties = config.getProperties();
		Set<DrawCard> chanceCards = config.getChanceCards();
		Set<DrawCard> communityChestCards = config.getCommunityChestCards();
		List<Space> spaces = config.getSpaces();

		Map<String,Integer> victoriesPerPlayer = new HashMap<>();
		long start = System.nanoTime();
		for (int i = 0; i < GAMES; i++) {
			Player player1 = new Player(STARTING_CASH, strategy, PLAYER_1);
			Player player2 = new Player(STARTING_CASH, strategy, PLAYER_2);
			Player winner = new Game(
					ImmutableList.of(player1,player2),
					properties,
					chanceCards,
					communityChestCards,
					spaces, new Dice()).play();

			Integer currentWins = victoriesPerPlayer.getOrDefault(winner.toString(), 0);
			victoriesPerPlayer.put(winner.toString(), currentWins+1);
		}
		long end = System.nanoTime();

		System.err.println("Final wins: " + victoriesPerPlayer);
		long ms = (end-start)/1000000;
		System.err.println("Played " + GAMES + " games in " + ms  +" ms, or " + (1000.0*GAMES/ms) + " games/sec");
	}

}
