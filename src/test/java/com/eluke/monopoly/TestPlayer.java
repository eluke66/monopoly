package com.eluke.monopoly;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.eluke.monopoly.actions.ImproveProperty;
import com.eluke.monopoly.actions.MortgageProperty;
import com.eluke.monopoly.actions.PlayerAction;
import com.eluke.monopoly.actions.SellImprovement;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class TestPlayer {
	private static final int STARTING_CASH = 10000;
	private static final int[] RENTS = new int[]{10,20,30,40,50,60,70};
	private Player player;
	private Property property;
	private PlayerStrategy strategy;
	private Game game;

	@Before
	public void setup() {
		property = createMock(Property.class);
		strategy = createMock(PlayerStrategy.class);
		game = createMock(Game.class);
		player = new Player(STARTING_CASH, strategy, "player");
	}
	@Test
	public void buyingAPropertySetsItToOwned() {
		player.addOwnedProperty(property);
		replay(property);

		OwnedProperty ownedProperty = player.getOwnedProperty(property);
		assertNotNull(ownedProperty);
		assertThat(ownedProperty.getProperty(),is(property));
		assertThat(ownedProperty.getOwner(),is(player));
	}

	@Test
	public void noImprovementsMeansPlayerOwnsZeroHouses() {
		player.addOwnedProperty(property);
		replay(property);

		assertThat(player.getHouses(),is(0));
		assertThat(player.getHotels(),is(0));
	}

	@Test
	public void singleImprovementMeansPlayerOwnsZeroHouses() {
		player.addOwnedProperty(property);
		expect(property.getRentCosts()).andReturn(RENTS);
		replay(property);

		OwnedProperty ownedProperty = player.getOwnedProperty(property);
		ownedProperty.improve();

		assertThat(player.getHouses(),is(0));
		assertThat(player.getHotels(),is(0));
	}

	@Test
	public void twoImprovementsMeansPlayerOwnsOneHouse() {
		player.addOwnedProperty(property);
		expect(property.getRentCosts()).andReturn(RENTS).anyTimes();
		replay(property);

		OwnedProperty ownedProperty = player.getOwnedProperty(property);
		ownedProperty.improve();
		ownedProperty.improve();

		assertThat(player.getHouses(),is(1));
		assertThat(player.getHotels(),is(0));
	}

	@Test
	public void fullyImprovedMeansPlayerOwnsOneHotel() {
		player.addOwnedProperty(property);
		expect(property.getRentCosts()).andReturn(RENTS).anyTimes();
		replay(property);

		OwnedProperty ownedProperty = player.getOwnedProperty(property);
		for (int i = 0; i < RENTS.length-1; i++) {
			ownedProperty.improve();
		}

		assertThat(player.getHouses(),is(0));
		assertThat(player.getHotels(),is(1));
	}

	@Test
	public void payingMoneyToBankReducesCash() {
		player.payMoney(50);

		assertThat(player.getCash(),is(STARTING_CASH-50));
	}

	@Test
	public void receivingCashIncreasesCash() {
		player.receiveCash(50);

		assertThat(player.getCash(),is(STARTING_CASH+50));
	}

	@Test
	public void havingCashMeansNotBankrupt() {
		assertThat(player.isBankrupt(),is(false));
	}

	@Test
	public void havingNegativeCashMeansBankrupt() {
		player.payMoney(STARTING_CASH*2);
		assertThat(player.isBankrupt(),is(true));
	}

	@Test
	public void payingCashToAnotherPlayerRaisesCashPaysAndDeducts() {
		Player other = createMock(Player.class);
		other.receiveCash(100);

		PlayerAction singleAction = createMock(PlayerAction.class);
		singleAction.execute(game);

		expect(strategy.raiseCashForExpense(100, player)).andReturn(ImmutableList.of(singleAction));
		replay(other);
		replay(strategy);
		replay(singleAction);

		player.payCashToPlayer(100, other, game);

		assertThat(player.getCash(),is(STARTING_CASH-100));
		verify(other);
	}

	@Test
	public void ifPayingCashBankruptsPlayerThenCreditorIsSet() {
		Player other = createMock(Player.class);
		other.receiveCash(STARTING_CASH*2);

		PlayerAction singleAction = createMock(PlayerAction.class);
		singleAction.execute(game);

		expect(strategy.raiseCashForExpense(STARTING_CASH*2, player)).andReturn(ImmutableList.of(singleAction));
		replay(other);
		replay(strategy);
		replay(singleAction);

		player.payCashToPlayer(STARTING_CASH*2, other, game);
		assertThat(player.isBankrupt(),is(true));
		assertThat(player.getCreditor(),is(other));

		verify(other);
	}

	@Test
	public void payingCashToOtherPlayersRaisesCashPaysAndDeducts() {
		Player other1 = createMock(Player.class);
		other1.receiveCash(100);
		Player other2 = createMock(Player.class);
		other2.receiveCash(100);

		PlayerAction singleAction = createMock(PlayerAction.class);
		singleAction.execute(game);

		expect(strategy.raiseCashForExpense(200, player)).andReturn(ImmutableList.of(singleAction));
		replay(other1);
		replay(other2);
		replay(strategy);
		replay(singleAction);

		player.payCashToPlayers(100, ImmutableList.of(other1,other2,player), game);

		assertThat(player.getCash(),is(STARTING_CASH-200));
		verify(other1);
		verify(other2);
	}

	@Test
	public void ifPayingCashToPlayersBankruptsPlayerThenCreditorIsSet() {
		Player other1 = createMock(Player.class);
		other1.receiveCash(STARTING_CASH);
		Player other2 = createMock(Player.class);
		other2.receiveCash(STARTING_CASH);

		PlayerAction singleAction = createMock(PlayerAction.class);
		singleAction.execute(game);

		expect(strategy.raiseCashForExpense(STARTING_CASH*2, player)).andReturn(ImmutableList.of(singleAction));
		replay(other1);
		replay(other2);
		replay(strategy);
		replay(singleAction);

		player.payCashToPlayers(STARTING_CASH, ImmutableList.of(other1,other2,player), game);
		assertThat(player.isBankrupt(),is(true));
		assertThat(player.getCreditor(),is(other1));

		verify(other1);
		verify(other2);
	}

	@Test
	public void actionsAreExecutedInProperOrder() {
		PlayerAction mort = new MortgageProperty(null);
		PlayerAction unimprove = new SellImprovement(null);
		PlayerAction other = new ImproveProperty(null);

		List<PlayerAction> actions = Lists.newArrayList(mort,unimprove,other);

		Collections.sort(actions, Player::actionExecutionOrder);

		assertThat(actions.get(0),is(unimprove));
		assertThat(actions.get(1),is(mort));
		assertThat(actions.get(2),is(other));
	}
}
