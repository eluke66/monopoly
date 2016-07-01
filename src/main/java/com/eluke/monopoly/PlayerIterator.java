package com.eluke.monopoly;

import java.util.Iterator;
import java.util.List;

class PlayerIterator {
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