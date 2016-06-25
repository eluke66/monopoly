/**
 * 
 */
package com.eluke.monopoly;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eluke.monopoly.Dice.DiceOutput;
import com.eluke.monopoly.cards.ChanceCard;
import com.eluke.monopoly.cards.CommunityChestCard;
import com.eluke.monopoly.spaces.Space;

/**
 * @author luke
 *
 */
public class Game {
	private static final int ROLLS_FOR_JAIL = 3;
	private static final int MAX_TURNS_IN_JAIL = 3;
	private final List<Player> players;
	private final Set<Property> properties;
	private final List<Space> gameboard;
	private final List<ChanceCard> chanceCards;
	private final List<CommunityChestCard> communityChestCards;
	private final Map<Player,Integer> playersInJail;
	private final Map<Player, Integer> spacesPerPlayer;

	public Game(final List<Player> players, final Set<Property> properties,
			final List<Space> gameboard) {
		this.players = players;
		this.properties = properties;
		this.gameboard = gameboard;

		playersInJail = new HashMap<Player,Integer>();
		spacesPerPlayer = new HashMap<Player,Integer>();
	}

	private static class PlayerIterator {
		private final List<Player> players;
		private Iterator<Player> iterator;
		private Player current;

		public PlayerIterator(final List<Player> players) {
			this.players = players;
			iterator = players.iterator();
			current = iterator.next();
		}

		public Player currentPlayer() {
			return current;
		}

		public void nextPlayer() {
			if (!iterator.hasNext()) {
				iterator = players.iterator();
			}
			current = iterator.next();
		}
	}
	public void play() {
		final List<Player> activePlayers = new LinkedList<>(players);
		final Dice dice = new Dice();
		PlayerIterator playerIterator = new PlayerIterator(activePlayers);

		while (!activePlayers.isEmpty()) {

			doPreTurnActions(activePlayers);

			doPlayerTurn(playerIterator.currentPlayer(), dice);

			playerIterator.nextPlayer();
		}
	}

	/**
	 * @param next
	 */
	private void doPlayerTurn(final Player player, final Dice dice) {
		DiceOutput roll;
		if (playerMustFirstLeaveJail(player)) {
			forcePlayerToLeaveJail(player);
		}
		if (playerIsInJail(player)) {
			for (int i = 0; i < ROLLS_FOR_JAIL; i++) {
				roll = dice.roll();
				if (roll.isDouble()) {
					break;
				}
			}
			if (!roll.isDouble()) {
				playerHasAnotherTurnInJail(player);
				return; // Didn't get out of jail.
			}
			else {
				playerIsOutOfJail(player);
			}
		}
		else {
			for (int i = 0; i < ROLLS_FOR_JAIL; i++) {
				roll = dice.roll();
				if (!roll.isDouble()) {
					break;
				}
			}
			if (roll.isDouble()) {
				sendPlayerToJail(player);
				return;
			}
		}

		moveToSpace(player, roll);
		executeActionsForSpace(player);

		if (playerIsBroke(player)) {
			removePlayerFromGame(player);
		}
	}

	private void sendPlayerToJail(final Player player) {
		spacesPerPlayer.put(player, JAIL_SPACE);
		playersInJail.put(player, 0);
	}

	private void playerIsOutOfJail(final Player player) {
		playersInJail.remove(player);
	}

	private void playerHasAnotherTurnInJail(final Player player) {
		Integer currentTurns = playersInJail.get(player);
		playersInJail.put(player, currentTurns+1);
	}

	private boolean playerIsInJail(final Player player) {
		return playersInJail.containsKey(player);
	}

	private void forcePlayerToLeaveJail(final Player player) {
		// TODO: Create and execute game action to pay $50

	}

	private boolean playerMustFirstLeaveJail(final Player player) {
		Integer currentTurnsInJail = playersInJail.get(player);
		return (currentTurnsInJail != null && currentTurnsInJail == MAX_TURNS_IN_JAIL);
	}

	private void doPreTurnActions(final List<Player> activePlayers) {
		// (mortgage, improve, sell improvement, get out of jail)

	}
}
