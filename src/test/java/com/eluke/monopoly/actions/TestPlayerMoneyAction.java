package com.eluke.monopoly.actions;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.eluke.monopoly.Game;
import com.eluke.monopoly.Player;
import com.google.common.collect.ImmutableList;

public class TestPlayerMoneyAction {
	private static final int CASH = 10;
	private Player player;
	private Player other;
	private Game game;
	private List<Player> allPlayers;

	@Before
	public void setup() {
		game = createMock(Game.class);
		player = createMock(Player.class);
		other = createMock(Player.class);

		allPlayers = ImmutableList.of(player,other);
		expect(game.getActivePlayers()).andReturn(allPlayers).anyTimes();
	}

	@Test
	public void receivingMoneyGetsFromAllPlayers() {
		other.payCashToPlayer(CASH, player, game);
		replay(player);
		replay(game);
		replay(other);

		new PlayerMoneyAction(CASH, "").execute(player, 0, game);

		verify(player);
		verify(other);
		verify(game);
	}

	@Test
	public void payingMoneyGoesToOthers() {
		player.payCashToPlayers(CASH, allPlayers, game);
		replay(player);
		replay(game);
		replay(other);

		new PlayerMoneyAction(-CASH, "").execute(player, 0, game);

		verify(player);
		verify(other);
		verify(game);
	}
}
