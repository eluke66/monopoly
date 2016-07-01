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

public class TestImproveProperty {
	private Player player;
	private Property property;
	private Game game;

	@Before
	public void setup() {
		property = createMock(Property.class);
		player = createMock(Player.class);
		game = createMock(Game.class);
	}

	@Test
	public void improvingAPropertyRequiresCashAndImprovesTheProperty() {
		expect(property.getHouseCost()).andReturn(10);
		expect(property.getRentCosts()).andReturn(new int[]{20,40}).anyTimes();
		expect(game.moreImprovementsAvailable(ImprovementType.House)).andReturn(true);
		game.markPropertyAsUsed(ImprovementType.House);
		player.payMoney(10);
		replay(property);
		replay(player);
		replay(game);

		OwnedProperty owned = new OwnedProperty(property, player);
		ImproveProperty improveProperty = new ImproveProperty(owned);
		improveProperty.execute(game);

		verify(player);
		verify(property);
	}

}
