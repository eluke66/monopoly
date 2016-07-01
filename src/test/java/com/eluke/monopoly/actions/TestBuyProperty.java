package com.eluke.monopoly.actions;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.eluke.monopoly.Game;
import com.eluke.monopoly.MonopolySet;
import com.eluke.monopoly.OwnedProperty;
import com.eluke.monopoly.Player;
import com.eluke.monopoly.Property;
import com.google.common.collect.ImmutableSet;

public class TestBuyProperty {
	private static final int HOUSE_COST = 10;
	private Player player;
	private Property property;
	private Game game;

	@Before
	public void setup() {
		property = new Property("property", HOUSE_COST, 20, 22, MonopolySet.Brown, 10, new int[]{10,20}, true);
		player = createMock(Player.class);
	}

	@Test
	public void buyingAnUnownedPropertyGivesItToPlayer() {
		Map<Property,Player> ownedProperties = new HashMap<>();
		Set<Property> unownedProperties = new HashSet<>();
		unownedProperties.add(property);
		player.addOwnedProperty(property);
		player.payCashToPlayer(HOUSE_COST, Player.BANK, game);
		expect(player.getOwnedProperties()).andReturn(ImmutableSet.of(new OwnedProperty(property,player)));
		replay(player);

		new BuyProperty(property, player, ownedProperties, unownedProperties).execute(game);

		assertThat(unownedProperties,is(empty()));
		assertThat(ownedProperties,hasEntry(property, player));
		verify(player);
	}

}
