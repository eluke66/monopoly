package com.eluke.monopoly.actions;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.Before;
import org.junit.Test;

import com.eluke.monopoly.Game;
import com.eluke.monopoly.ImprovementType;
import com.eluke.monopoly.OwnedProperty;
import com.eluke.monopoly.Player;
import com.eluke.monopoly.Property;

public class TestSellImprovement {
	private static final Integer HOUSE_COST = 10;
	private Player player;
	private Property property;
	private OwnedProperty owned;
	private Game game;

	@Before
	public void setup() {
		owned = createMock(OwnedProperty.class);
		player = createMock(Player.class);
		property = createMock(Property.class);
		game = createMock(Game.class);
	}

	@Test
	public void sellingImprovementRaisesHalfHouseCostAndRemovesImprovements() {
		expect(owned.getOwner()).andReturn(player).anyTimes();
		expect(owned.getProperty()).andReturn(property).anyTimes();
		expect(property.getHouseCost()).andReturn(HOUSE_COST);
		player.receiveCash(HOUSE_COST/2);
		owned.removeImprovement();
		expect(owned.getCurrentImprovementType()).andReturn(ImprovementType.House);
		game.markPropertyAsUnused(ImprovementType.House);
		replay(player);
		replay(owned);
		replay(property);
		replay(game);

		new SellImprovement(owned).execute(game);

		verify(owned);
		verify(player);
		verify(property);
		verify(game);
	}

}
