package com.eluke.monopoly.actions;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.Before;
import org.junit.Test;

import com.eluke.monopoly.Game;
import com.eluke.monopoly.Player;

public class TestBankMoneyAction {

	private Game game;
	private Player player;

	@Before
	public void setup() {
		game = createMock(Game.class);
		player = createMock(Player.class);
	}

	@Test
	public void positiveCashGetsMoneyFromBank() {
		int income = 5;
		player.receiveCash(income);
		replay(player);

		new BankMoneyAction(income, "").execute(player, 0, game);

		verify(player);
	}

	@Test
	public void negativeCashPaysMoneyToBank() {
		int debt = 5;
		player.payCashToPlayer(debt, Player.BANK, game);
		replay(player);

		new BankMoneyAction(-debt, "").execute(player, 0, game);

		verify(player);
	}
}
