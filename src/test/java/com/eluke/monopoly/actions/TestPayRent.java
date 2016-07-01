package com.eluke.monopoly.actions;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.Before;
import org.junit.Test;

import com.eluke.monopoly.Game;
import com.eluke.monopoly.OwnedProperty;
import com.eluke.monopoly.OwnedProperty.RentOverride;
import com.eluke.monopoly.Player;

public class TestPayRent {
	private static final int DICE = 6;
	private static final Integer RENT = 99;
	private static final RentOverride NO_OVERRIDE = null;
	private OwnedProperty owned;
	private Player player;
	private Player other;
	private Game game;

	@Before
	public void setup() {
		owned = createMock(OwnedProperty.class);
		player = createMock(Player.class);
		other = createMock(Player.class);
	}

	@Test
	public void payingRentRequiresRaisingCashAndGivingToPlayer() {
		expect(owned.getRent(DICE, NO_OVERRIDE)).andReturn(RENT);
		expect(owned.getOwner()).andReturn(other);
		player.payCashToPlayer(RENT, other, game);
		replay(owned);
		replay(player);

		PayRent action = new PayRent(player, owned, DICE);
		action.execute(game);

		verify(owned);
		verify(player);
	}

	@Test
	public void rentOverrideIsRespected() {
		expect(owned.getRent(DICE, RentOverride.DOUBLE)).andReturn(RENT);
		expect(owned.getOwner()).andReturn(other);
		player.payCashToPlayer(RENT, other, game);
		replay(owned);
		replay(player);

		PayRent action = new PayRent(player, owned, DICE, RentOverride.DOUBLE);
		action.execute(game);

		verify(owned);
		verify(player);
	}
}
