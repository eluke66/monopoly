package com.eluke.monopoly;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import com.eluke.monopoly.Dice.DiceOutput;
import com.eluke.monopoly.actions.BuyProperty;
import com.eluke.monopoly.actions.GameAction;
import com.eluke.monopoly.actions.PostBail;
import com.eluke.monopoly.cards.DrawCard;
import com.eluke.monopoly.spaces.ChanceSpace;
import com.eluke.monopoly.spaces.CommunityChestSpace;
import com.eluke.monopoly.spaces.GoSpace;
import com.eluke.monopoly.spaces.GoToJailSpace;
import com.eluke.monopoly.spaces.JailSpace;
import com.eluke.monopoly.spaces.PropertySpace;
import com.eluke.monopoly.spaces.Space;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class TestGameScenarios {
	private static final int RAILROAD_BUY_PRICE = 200;
	private List<Player> players;
	private Set<Property> properties;
	private Collection<DrawCard> chanceCards;
	private Collection<DrawCard> communityChestCards;
	private List<Space> gameboard;

	private IMocksControl control;
	private Player playerOne;
	private Player playerTwo;
	private Property railroad;
	private Property utility;
	private Property propertyOne;
	private Property propertyTwo;
	private DrawCard gotoJail;
	private DrawCard gotoGo;
	private DrawCard getMoney;
	private Space goSpace;
	private Space jailSpace;
	private Space goToJailSpace;
	private Dice dice;

	@Before
	public void setup() {
		control = createControl();

		playerOne = control.createMock(Player.class);
		expect(playerOne.getCash()).andReturn(2000).anyTimes();
		playerTwo = control.createMock(Player.class);
		expect(playerTwo.getCash()).andReturn(2000).anyTimes();
		players = ImmutableList.of(playerOne, playerTwo);

		properties = ImmutableSet.<Property>builder()
		.add(railroad = control.createMock(Property.class))
		.add(utility = control.createMock(Property.class))
		.add(propertyOne = control.createMock(Property.class))
		.add(propertyTwo = control.createMock(Property.class))
		.build();

		expect(railroad.getMonopolySet()).andReturn(MonopolySet.Railroad).anyTimes();
		expect(railroad.canImprove()).andReturn(false).anyTimes();
		expect(railroad.getBuyPrice()).andReturn(RAILROAD_BUY_PRICE).anyTimes();
		expect(utility.getMonopolySet()).andReturn(MonopolySet.Utility).anyTimes();
		expect(utility.getBuyPrice()).andReturn(200).anyTimes();
		expect(propertyOne.getMonopolySet()).andReturn(MonopolySet.Brown).anyTimes();
		expect(propertyOne.getBuyPrice()).andReturn(200).anyTimes();
		expect(propertyTwo.getMonopolySet()).andReturn(MonopolySet.Brown).anyTimes();
		expect(propertyTwo.getBuyPrice()).andReturn(200).anyTimes();

		chanceCards = ImmutableSet.of(
				gotoJail = control.createMock(DrawCard.class),
				gotoGo = control.createMock(DrawCard.class),
				getMoney = control.createMock(DrawCard.class) );
		communityChestCards = ImmutableSet.of(getMoney, gotoGo, gotoJail);

		jailSpace = new JailSpace();
		goToJailSpace = new GoToJailSpace();
		gameboard = ImmutableList.<Space>builder()
				.add(goSpace = new GoSpace())
				.build();
		dice = control.createMock(Dice.class);
	}

	@Test
	public void givenTwoPlayers_WhenOneGoesBankrupt_ThenGameEndsWithWinner() {
		Game game = new Game(players, properties, chanceCards, communityChestCards, gameboard,dice);
		neverPreTurnActionsFor(playerOne,game);
		neverPreTurnActionsFor(playerTwo,game);

		expect(playerOne.getLocation()).andReturn(0).times(2);
		playerOne.setLocation(0);
		playerTwo.setLocation(0);
		expect(playerOne.isBankrupt()).andReturn(true);
		expect(playerTwo.getLocation()).andReturn(0).times(2);
		expect(playerOne.getCreditor()).andReturn(Player.BANK);
		expect(playerOne.getOwnedProperties()).andReturn(ImmutableSet.of(new OwnedProperty(railroad)));
		expect(dice.roll()).andReturn(new DiceOutput(0,2));
		expect(playerOne.getOwnedProperties()).andReturn(ImmutableSet.of());
		control.replay();

		Player winner = game.play();

		assertThat(winner, is(playerTwo));
	}

	@Test
	public void givenTwoPlayers_WhenOneLandsOnGoToJail_ThenTheyGoToJail() {
		gameboard = ImmutableList.of(goSpace, jailSpace, goToJailSpace);
		Game game = new Game(players, properties, chanceCards, communityChestCards, gameboard,dice);
		neverPreTurnActionsFor(playerOne,game);
		neverPreTurnActionsFor(playerTwo,game);

		expect(playerOne.getLocation()).andReturn(0);
		playerOne.setLocation(0);
		playerTwo.setLocation(0);
		expect(dice.roll()).andReturn(new DiceOutput(0,2));
		playerOne.setLocation(2);
		expect(playerOne.getLocation()).andReturn(2);
		playerOne.setLocation(1); // Jail
		expect(playerOne.isBankrupt()).andReturn(false);
		expect(playerOne.getOwnedProperties()).andReturn(ImmutableSet.of());
		control.replay();

		PlayerIterator playerIterator = new PlayerIterator(players);
		game.doGameTurn(dice, playerIterator);

		assertThat(game.isPlayerInJail(playerOne), is(true));
	}

	@Test
	public void givenTwoPlayers_WhenOneIsInJail_ThenDoublesGetsThemOut() {
		gameboard = ImmutableList.of(goSpace, jailSpace, goToJailSpace);
		Game game = new Game(players, properties, chanceCards, communityChestCards, gameboard,dice);
		neverPreTurnActionsFor(playerOne,game);
		neverPreTurnActionsFor(playerTwo,game);

		expect(playerOne.getLocation()).andReturn(1);
		playerOne.setLocation(1);
		expect(dice.roll()).andReturn(new DiceOutput(0,2));
		expect(dice.roll()).andReturn(new DiceOutput(1,1)); // Doubles
		playerOne.receiveCash(200);
		playerOne.setLocation(0);
		expect(playerOne.getLocation()).andReturn(0);
		expect(playerOne.isBankrupt()).andReturn(false);
		expect(playerOne.getOwnedProperties()).andReturn(ImmutableSet.of());
		control.replay();

		PlayerIterator playerIterator = new PlayerIterator(players);
		game.sendPlayerToJail(playerOne);
		game.doGameTurn(dice, playerIterator);

		assertThat(game.isPlayerInJail(playerOne), is(false));
	}

	@Test
	public void givenTwoPlayers_WhenOneIsInJail_ThenThreeNonDoublesKeepsThemInJail() {
		gameboard = ImmutableList.of(goSpace, jailSpace, goToJailSpace);
		Game game = new Game(players, properties, chanceCards, communityChestCards, gameboard,dice);
		neverPreTurnActionsFor(playerOne,game);
		neverPreTurnActionsFor(playerTwo,game);

		expect(playerOne.getLocation()).andReturn(1);
		playerOne.setLocation(1);
		expect(dice.roll()).andReturn(new DiceOutput(0,2));
		expect(dice.roll()).andReturn(new DiceOutput(0,1));
		expect(dice.roll()).andReturn(new DiceOutput(0,1));

		expect(playerOne.isBankrupt()).andReturn(false);
		expect(playerOne.getOwnedProperties()).andReturn(ImmutableSet.of());
		control.replay();

		PlayerIterator playerIterator = new PlayerIterator(players);
		game.sendPlayerToJail(playerOne);
		game.doGameTurn(dice, playerIterator);

		assertThat(game.isPlayerInJail(playerOne), is(true));
	}

	@Test
	public void givenTwoPlayers_WhenOneIsInJail_ThenTheThirdTryOfNoDoublesForcesThemToLeave() {
		gameboard = ImmutableList.of(goSpace, jailSpace, goToJailSpace);
		Game game = new Game(players, properties, chanceCards, communityChestCards, gameboard,dice);
		neverPreTurnActionsFor(playerOne,game);
		neverPreTurnActionsFor(playerTwo,game);

		expect(playerOne.getLocation()).andReturn(1);
		playerOne.setLocation(1);
		expect(dice.roll()).andReturn(new DiceOutput(0,2));
		expect(dice.roll()).andReturn(new DiceOutput(0,1));
		expect(dice.roll()).andReturn(new DiceOutput(0,1));

		expect(dice.roll()).andReturn(new DiceOutput(0,2));
		playerOne.receiveCash(200);
		playerOne.setLocation(0);
		expect(playerOne.getLocation()).andReturn(0);

		expect(playerOne.isBankrupt()).andReturn(false).anyTimes();

		playerOne.payCashToPlayer(PostBail.BAIL, Player.BANK, game);
		expect(playerOne.getOwnedProperties()).andReturn(ImmutableSet.of()).anyTimes();
		control.replay();

		PlayerIterator playerIterator = new PlayerIterator(players);
		game.sendPlayerToJail(playerOne);
		game.playerHasAnotherTurnInJail(playerOne);
		game.playerHasAnotherTurnInJail(playerOne);
		game.doGameTurn(dice, playerIterator);
		playerIterator.nextPlayer();
		game.doGameTurn(dice, playerIterator);

		assertThat(game.isPlayerInJail(playerOne), is(false));
	}

	@Test
	public void givenTwoPlayers_ThenThreeDoublesSendsThemToJail() {
		gameboard = ImmutableList.of(goSpace, jailSpace, goToJailSpace);
		Game game = new Game(players, properties, chanceCards, communityChestCards, gameboard,dice);
		neverPreTurnActionsFor(playerOne,game);
		neverPreTurnActionsFor(playerTwo,game);

		expect(playerOne.getLocation()).andReturn(0);
		expect(dice.roll()).andReturn(new DiceOutput(2,2));
		expect(dice.roll()).andReturn(new DiceOutput(1,1));
		expect(dice.roll()).andReturn(new DiceOutput(1,1));
		playerOne.setLocation(1);
		expect(playerOne.isBankrupt()).andReturn(false);
		expect(playerOne.getOwnedProperties()).andReturn(ImmutableSet.of());
		control.replay();

		PlayerIterator playerIterator = new PlayerIterator(players);
		game.doGameTurn(dice, playerIterator);

		assertThat(game.isPlayerInJail(playerOne), is(true));
	}

	@Test
	public void when_LandingOnAChanceSpace_ThenGetsAChanceCard() {
		Space chanceSpace = new ChanceSpace();
		gameboard = ImmutableList.of(goSpace, chanceSpace, jailSpace, goToJailSpace);
		Game game = new Game(players, properties, chanceCards, communityChestCards, gameboard,dice);
		neverPreTurnActionsFor(playerOne,game);
		neverPreTurnActionsFor(playerTwo,game);

		expect(playerOne.getLocation()).andReturn(0);
		expect(dice.roll()).andReturn(new DiceOutput(0,1));
		playerOne.setLocation(1);
		expect(playerOne.getLocation()).andReturn(1);

		GameAction action = control.createMock(GameAction.class);
		chanceCards.stream().forEach(card -> expect(card.play()).andReturn(action).anyTimes());
		action.execute(playerOne, 1, game);
		expect(action.getDescription()).andReturn("action");

		expect(playerOne.isBankrupt()).andReturn(false);

		expect(playerOne.getOwnedProperties()).andReturn(ImmutableSet.of());
		control.replay();

		PlayerIterator playerIterator = new PlayerIterator(players);
		game.doGameTurn(dice, playerIterator);

		control.verify();
	}

	@Test
	public void when_LandingOnACommunityChestSpace_ThenGetsACommunityChestCard() {
		Space communityChestSpace = new CommunityChestSpace();
		gameboard = ImmutableList.of(goSpace, communityChestSpace, jailSpace, goToJailSpace);
		Game game = new Game(players, properties, chanceCards, communityChestCards, gameboard,dice);
		neverPreTurnActionsFor(playerOne,game);
		neverPreTurnActionsFor(playerTwo,game);

		expect(playerOne.getLocation()).andReturn(0);
		expect(dice.roll()).andReturn(new DiceOutput(0,1));
		playerOne.setLocation(1);
		expect(playerOne.getLocation()).andReturn(1);

		GameAction action = control.createMock(GameAction.class);
		chanceCards.stream().forEach(card -> expect(card.play()).andReturn(action).anyTimes());
		action.execute(playerOne, 1, game);
		expect(action.getDescription()).andReturn("action");

		expect(playerOne.isBankrupt()).andReturn(false);
		expect(playerOne.getOwnedProperties()).andReturn(ImmutableSet.of());
		control.replay();

		PlayerIterator playerIterator = new PlayerIterator(players);
		game.doGameTurn(dice, playerIterator);

		control.verify();
	}

	@Test
	public void when_LandingOnAnUnownedProperty_ThenGetsAChanceToBuy() {
		Space railroadSpace = new PropertySpace(railroad);
		gameboard = ImmutableList.of(goSpace, railroadSpace, jailSpace, goToJailSpace);
		Game game = new Game(players, properties, chanceCards, communityChestCards, gameboard,dice);
		neverPreTurnActionsFor(playerOne,game);
		neverPreTurnActionsFor(playerTwo,game);

		expect(playerOne.getLocation()).andReturn(0);
		expect(dice.roll()).andReturn(new DiceOutput(0,1));
		playerOne.setLocation(1);
		expect(playerOne.getLocation()).andReturn(1);

		expect(playerOne.wantsToBuy(railroad)).andReturn(true);
		playerOne.addOwnedProperty(railroad);
		expect(playerOne.getOwnedProperties()).andReturn(ImmutableSet.of(new OwnedProperty(railroad,playerOne))).anyTimes();

		expect(playerOne.isBankrupt()).andReturn(false);

		playerOne.payCashToPlayer(RAILROAD_BUY_PRICE, Player.BANK, game);
		control.replay();

		PlayerIterator playerIterator = new PlayerIterator(players);
		game.doGameTurn(dice, playerIterator);

		control.verify();
	}

	@Test
	public void when_LandingOnAnOwnedProperty_ThenPaysRent() {
		final int RENT = 25;
		Space railroadSpace = new PropertySpace(railroad);
		gameboard = ImmutableList.of(goSpace, railroadSpace, jailSpace, goToJailSpace);
		Game game = new Game(players, properties, chanceCards, communityChestCards, gameboard,dice);
		neverPreTurnActionsFor(playerOne,game);
		neverPreTurnActionsFor(playerTwo,game);

		expectThatPlayerOwnsProperty(playerTwo, railroad);

		expect(playerOne.getLocation()).andReturn(0);
		expect(dice.roll()).andReturn(new DiceOutput(0,1));
		playerOne.setLocation(1);
		expect(playerOne.getLocation()).andReturn(1);

		expect(railroad.getRentCosts()).andReturn(new int[]{RENT}).anyTimes();
		playerOne.payCashToPlayer(RENT, playerTwo, game);
		expect(playerOne.isBankrupt()).andReturn(false).anyTimes();

		playerTwo.payCashToPlayer(RAILROAD_BUY_PRICE, Player.BANK, game);
		expect(playerOne.getOwnedProperties()).andReturn(ImmutableSet.of());
		control.replay();

		new BuyProperty(railroad, playerTwo, game.getOwnedProperties(), game.getUnownedProperties()).execute(game);
		PlayerIterator playerIterator = new PlayerIterator(players);
		game.doGameTurn(dice, playerIterator);

		control.verify();
	}

	@Test
	public void when_LandingOnOwnProperty_ThenPaysNoRent() {
		Space railroadSpace = new PropertySpace(railroad);
		gameboard = ImmutableList.of(goSpace, railroadSpace, jailSpace, goToJailSpace);
		Game game = new Game(players, properties, chanceCards, communityChestCards, gameboard,dice);
		neverPreTurnActionsFor(playerOne,game);
		neverPreTurnActionsFor(playerTwo,game);
		expectThatPlayerOwnsProperty(playerOne, railroad);

		expect(playerOne.getLocation()).andReturn(0);
		expect(dice.roll()).andReturn(new DiceOutput(0,1));
		playerOne.setLocation(1);
		expect(playerOne.getLocation()).andReturn(1);

		expect(playerOne.isBankrupt()).andReturn(false).anyTimes();

		playerOne.payCashToPlayer(RAILROAD_BUY_PRICE, Player.BANK, game);
		control.replay();

		new BuyProperty(railroad, playerOne, game.getOwnedProperties(), game.getUnownedProperties()).execute(game);
		PlayerIterator playerIterator = new PlayerIterator(players);
		game.doGameTurn(dice, playerIterator);

		control.verify();
	}

	private static void expectThatPlayerOwnsProperty(final Player player, final Property property) {
		player.addOwnedProperty(property);
		OwnedProperty owned = new OwnedProperty(property,player);
		expect(player.getOwnedProperties()).andReturn(ImmutableSet.of(owned)).anyTimes();
		expect(player.getOwnedProperty(property)).andReturn(owned).anyTimes();
	}

	private void neverPreTurnActionsFor(final Player player, final Game game) {
		expect(player.getPreTurnAction(game)).andReturn(Optional.empty()).anyTimes();
	}
}
