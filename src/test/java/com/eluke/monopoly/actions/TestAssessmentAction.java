package com.eluke.monopoly.actions;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.Before;
import org.junit.Test;

import com.eluke.monopoly.Game;
import com.eluke.monopoly.Player;

public class TestAssessmentAction {
	private static final int HOUSE_COST = 1;
	private static final int HOTEL_COST = 10;
	private Game game;
	private Player player;

	@Before
	public void setup() {
		game = createMock(Game.class);
		player = createMock(Player.class);
	}

	@Test
	public void assessmentsIncorporateHouseAndHotelCosts() {
		expect(player.getHouses()).andReturn(1);
		expect(player.getHotels()).andReturn(1);
		int expectedCost = HOUSE_COST+HOTEL_COST;
		player.payCashToPlayer(expectedCost, Player.BANK, game);
		replay(player);

		new AssessmentAction(HOUSE_COST,HOTEL_COST, "").execute(player, 0, game);

		verify(player);
	}
}
