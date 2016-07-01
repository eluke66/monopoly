package com.eluke.monopoly.actions;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.Before;
import org.junit.Test;

import com.eluke.monopoly.Game;
import com.eluke.monopoly.Player;

public class TestPostBail {
	private Player player;
	private Game game;

	@Before
	public void setup() {
		player = createMock(Player.class);
	}

	@Test
	public void postingBailRaisesCashFromPlayer() {
		player.payCashToPlayer(PostBail.BAIL, Player.BANK, game);
		replay(player);

		new PostBail(player).execute(game);

		verify(player);
	}

}
