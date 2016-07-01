package com.eluke.monopoly.strategy;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.Optional;

import org.easymock.IMocksControl;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;

import com.eluke.monopoly.Game;
import com.eluke.monopoly.ImprovementType;
import com.eluke.monopoly.MonopolySet;
import com.eluke.monopoly.OwnedProperty;
import com.eluke.monopoly.Player;
import com.eluke.monopoly.Property;
import com.eluke.monopoly.actions.ImproveProperty;
import com.eluke.monopoly.actions.MortgageProperty;
import com.eluke.monopoly.actions.PlayerAction;
import com.eluke.monopoly.actions.PlayerPropertyAction;
import com.eluke.monopoly.actions.SellImprovement;
import com.eluke.monopoly.actions.UnmortgageProperty;
import com.google.common.collect.ImmutableSet;

public class TestAggressiveBuilder {
	private static final int[] RENT_COSTS = new int[]{10,20,30,40,50};
	private static final int MORTGAGE_VALUE = 50;
	private static final int HOUSE_COST = 40;
	private AggressiveBuilderStrategy strategy;
	private Player player;
	private Property p1;
	private OwnedProperty property1;
	private Property p2;
	private OwnedProperty property2;
	private Property p3;
	private OwnedProperty property3;
	private Property p4;
	private OwnedProperty property4;
	private Property property;
	private Game game;
	private IMocksControl control;

	@Before
	public void setup() {
		strategy = new AggressiveBuilderStrategy();
		control = createControl();
		player = control.createMock(Player.class);
		property = control.createMock(Property.class);
		game = control.createMock(Game.class);

		p1 = new Property("p1", 100, MORTGAGE_VALUE, 55, MonopolySet.Brown, HOUSE_COST, RENT_COSTS, true);
		p2 = new Property("p2", 100, MORTGAGE_VALUE, 55, MonopolySet.Brown, HOUSE_COST, RENT_COSTS, true);
		p3 = new Property("p3", 100, MORTGAGE_VALUE, 55, MonopolySet.Green, HOUSE_COST, RENT_COSTS, true);
		p4 = new Property("p4", 100, MORTGAGE_VALUE, 55, MonopolySet.Green, HOUSE_COST, RENT_COSTS, true);

		property1 = new OwnedProperty(p1, player);
		property2 = new OwnedProperty(p2, player);
		property3 = new OwnedProperty(p3, player);
		property4 = new OwnedProperty(p4, player);
	}

	@Test
	public void shouldBuyPropertiesIfHaveEnoughCash() {
		playerHasCash(100);
		propertyCosts(50);
		control.replay();

		assertThat(strategy.wantsToBuy(property, player),is(true));
	}


	@Test
	public void shouldNotBuyPropertiesIfNotEnoughCash() {
		playerHasCash(100);
		propertyCosts(500);
		control.replay();

		MatcherAssert.assertThat(strategy.wantsToBuy(property, player),is(false));
	}

	@Test
	public void playerGetsThirdHouseWhenPossible() {
		playerHasCash(100);
		playerOwnsAllProperties();
		property1.setAsPartOfMonopoly(true);
		expect(game.moreImprovementsAvailable(ImprovementType.House)).andReturn(true);
		control.replay();

		Optional<PlayerAction> action = strategy.getPreTurnAction(player, game);

		assertThat(action.isPresent(),is(true));
		assertThat(action.get(),improvedProperty(property1));
	}

	@Test
	public void playerDoesNotTryToOverImproveMonopolySet() {
		playerHasCash(100);
		playerOwnsAllProperties();
		property1.setAsPartOfMonopoly(true);
		property2.setAsPartOfMonopoly(true);
		expect(game.moreImprovementsAvailable(ImprovementType.House)).andReturn(true);
		control.replay();

		property1.improve();

		Optional<PlayerAction> action = strategy.getPreTurnAction(player, game);

		assertThat(action.isPresent(),is(true));
		assertThat(action.get(),improvedProperty(property2));
	}

	@Test
	public void playerDoesNotTryToImprovePastHotels() {
		playerHasCash(100);
		playerOwnsAllProperties();
		property1.setAsPartOfMonopoly(true);
		property2.setAsPartOfMonopoly(true);
		control.replay();

		improvePropertyNTimes(property1, RENT_COSTS.length-1);
		improvePropertyNTimes(property2, RENT_COSTS.length-1);


		Optional<PlayerAction> action = strategy.getPreTurnAction(player, game);
		assertThat(action.isPresent(),is(false));
	}

	@Test
	public void playerDoesNotTryToImproveWithNoHousesLeft() {
		playerHasCash(100);
		playerOwnsAllProperties();
		property1.setAsPartOfMonopoly(true);
		expect(game.moreImprovementsAvailable(ImprovementType.House)).andReturn(false);
		control.replay();

		Optional<PlayerAction> action = strategy.getPreTurnAction(player, game);

		assertThat(action.isPresent(),is(false));
	}

	@Test
	public void playerUnmortgagesMonopolyPropertiesWhenPossible() {
		playerHasCash(100);
		playerOwnsAllProperties();
		property1.setAsPartOfMonopoly(true);
		property2.setAsPartOfMonopoly(true);
		property1.mortgage();
		property2.mortgage();
		control.replay();

		Optional<PlayerAction> action = strategy.getPreTurnAction(player, game);

		assertThat(action.isPresent(),is(true));
		assertThat(action.get(),unmortgagedProperty(property1));
	}

	@Test
	public void raisingCashDoesNothingIfWeHaveEnoughCash() {
		playerHasCash(100);
		control.replay();

		Collection<PlayerAction> actions = strategy.raiseCashForExpense(50, player);

		assertThat(actions,is(empty()));
	}

	@Test
	public void raisingCashFirstSelectsNonMonopolyPropertiesToMortgage() {
		playerHasCash(10);
		playerOwnsThreeProperties();
		property1.setAsPartOfMonopoly(true);
		property2.setAsPartOfMonopoly(true);
		control.replay();

		Collection<PlayerAction> actions = strategy.raiseCashForExpense(50, player);

		assertThat(actions.size(),is(equalTo(1)));
		assertThat(actions,contains(mortgagedProperty(property3)));
	}

	@Test
	public void raisingCashPrefersKeepingThreeHousesIntact() {
		playerHasCash(0);
		playerOwnsThreeProperties();
		property1.setAsPartOfMonopoly(true);
		property2.setAsPartOfMonopoly(true);
		property2.improve();
		property1.improve();
		property1.improve();
		property1.improve();
		control.replay();

		Collection<PlayerAction> actions = strategy.raiseCashForExpense(MORTGAGE_VALUE+HOUSE_COST/2, player);

		assertThat(actions,containsInAnyOrder(improvementSold(property2), mortgagedProperty(property3)));
	}

	@Test
	public void raisingCashWillSellAllHouses() {
		playerHasCash(0);
		playerOwnsThreeProperties();
		property1.setAsPartOfMonopoly(true);
		property2.setAsPartOfMonopoly(true);
		property2.improve();
		property1.improve();
		control.replay();

		Collection<PlayerAction> actions = strategy.raiseCashForExpense(MORTGAGE_VALUE+HOUSE_COST, player);

		assertThat(actions,containsInAnyOrder(improvementSold(property2), improvementSold(property1), mortgagedProperty(property3)));
	}

	@Test
	public void raisingCashWillSellAllHousesAndMortgageAllProperties() {
		playerHasCash(0);
		playerOwnsThreeProperties();
		property1.setAsPartOfMonopoly(true);
		property2.setAsPartOfMonopoly(true);
		property2.improve();
		property1.improve();
		control.replay();

		Collection<PlayerAction> actions = strategy.raiseCashForExpense(3*MORTGAGE_VALUE+HOUSE_COST, player);

		assertThat(actions,containsInAnyOrder(
				improvementSold(property2),
				improvementSold(property1),
				mortgagedProperty(property3),
				mortgagedProperty(property2),
				mortgagedProperty(property1)));
	}

	@Test
	public void willSellAllHousesIfNeeded() {
		playerHasCash(0);
		playerOwnsThreeProperties();
		property1.setAsPartOfMonopoly(true);
		property2.setAsPartOfMonopoly(true);
		improvePropertyNTimes(property2,3);
		improvePropertyNTimes(property1,3);
		control.replay();

		Collection<PlayerAction> actions = strategy.raiseCashForExpense(MORTGAGE_VALUE+3*HOUSE_COST, player);

		assertThat(actions,containsInAnyOrder(
				improvementSold(property2),
				improvementSold(property2),
				improvementSold(property2),
				improvementSold(property1),
				improvementSold(property1),
				improvementSold(property1),
				mortgagedProperty(property3)));
	}

	private void improvePropertyNTimes(final OwnedProperty property, final int n) {
		for (int i = 0; i < n; i++) {
			property.improve();
		}
	}
	private void playerOwnsAllProperties() {
		expect(player.getOwnedProperties()).andReturn(ImmutableSet.of(property1,property2,property3,property4)).anyTimes();
	}
	private void playerOwnsThreeProperties() {
		expect(player.getOwnedProperties()).andReturn(ImmutableSet.of(property1,property2,property3)).anyTimes();
	}
	private void playerHasCash(final int cash) {
		expect(player.getCash()).andReturn(cash).anyTimes();
	}
	private void propertyCosts(final int cash) {
		expect(property.getBuyPrice()).andReturn(cash).anyTimes();
	}

	public static PlayerActionPropertyMatcher improvedProperty(final OwnedProperty property) {
		return new PlayerActionPropertyMatcher(property, ImproveProperty.class, "Improve");
	}
	public static PlayerActionPropertyMatcher mortgagedProperty(final OwnedProperty property) {
		return new PlayerActionPropertyMatcher(property, MortgageProperty.class, "Mortgage");
	}
	public static PlayerActionPropertyMatcher improvementSold(final OwnedProperty property) {
		return new PlayerActionPropertyMatcher(property, SellImprovement.class, "Sell improvement on");
	}
	public static PlayerActionPropertyMatcher unmortgagedProperty(final OwnedProperty property) {
		return new PlayerActionPropertyMatcher(property, UnmortgageProperty.class, "Unmortgage");
	}

	public static class PlayerActionPropertyMatcher extends BaseMatcher<PlayerAction> {
		private final OwnedProperty property;
		private final Class<? extends PlayerPropertyAction> actionClass;
		private final String description;

		public PlayerActionPropertyMatcher(final OwnedProperty property, final Class<? extends PlayerPropertyAction> actionClass, final String description) {
			this.property = property;
			this.actionClass = actionClass;
			this.description = description;
		}

		@Override
		public boolean matches(final Object item) {
			if (item.getClass().equals(actionClass)) {
				PlayerPropertyAction sp = (PlayerPropertyAction)item;
				return property.equals(sp.getProperty());
			}
			return false;
		}

		@Override
		public void describeTo(final Description description) {
			description.appendText(this.description + " " + property.getProperty().getName());

		}
	}
}
