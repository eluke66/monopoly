/**
 *
 */
package com.eluke.monopoly;

import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.eluke.monopoly.Dice.DiceOutput;
import com.eluke.monopoly.OwnedProperty.RentOverride;
import com.eluke.monopoly.actions.BankMoneyAction;
import com.eluke.monopoly.actions.BuyProperty;
import com.eluke.monopoly.actions.GameAction;
import com.eluke.monopoly.actions.PayRent;
import com.eluke.monopoly.actions.PlayerAction;
import com.eluke.monopoly.actions.PostBail;
import com.eluke.monopoly.cards.DrawCard;
import com.eluke.monopoly.spaces.ChanceSpace;
import com.eluke.monopoly.spaces.CommunityChestSpace;
import com.eluke.monopoly.spaces.FreeParkingSpace;
import com.eluke.monopoly.spaces.GoSpace;
import com.eluke.monopoly.spaces.GoToJailSpace;
import com.eluke.monopoly.spaces.JailSpace;
import com.eluke.monopoly.spaces.PayTheBankSpace;
import com.eluke.monopoly.spaces.PropertySpace;
import com.eluke.monopoly.spaces.Space;
import com.google.common.collect.ImmutableList;

/**
 * @author luke
 *
 */
public class Game {
	private static final Logger logger = LoggerFactory.getLogger("monopoly");
	private static final int ROLLS_FOR_JAIL = 3;
	private static final int MAX_TURNS_IN_JAIL = 3;
	private static final int SALARY = 200;
	private static final int ROLL_NOT_NEEDED = -1;
	private static final int GO_SPACE = 0;
	private static final int MAX_TURNS = 500;
	private final List<Player> players;
	private final List<Space> gameboard;
	private final Queue<DrawCard> chanceCards;
	private final Queue<DrawCard> communityChestCards;
	private final Map<Player,Integer> playersInJail;
	private final Map<Player, Integer> spacesPerPlayer;
	private final Map<Property, Player> ownedProperties;
	private final Set<Property> unownedProperties;
	private int JailSpaceLocation;
	private final List<Player> activePlayers;
	private final Dice dice;
	private Map<ImprovementType,Integer> availableImprovements;

	public Game(final List<Player> players,
			final Set<Property> properties,
			final Collection<DrawCard> chanceCards,
			final Collection<DrawCard> communityChestCards,
			final List<Space> gameboard,
			final Dice dice) {
		this.players = players;
		this.unownedProperties = new HashSet<>(properties);
		this.gameboard = ImmutableList.copyOf(gameboard);
		activePlayers = new LinkedList<>(players);

		this.chanceCards = shuffle(chanceCards);
		this.communityChestCards = shuffle(communityChestCards);

		playersInJail = new HashMap<>();
		spacesPerPlayer = new HashMap<>();
		ownedProperties = new HashMap<>();

		findJailSpace(gameboard);
		this.dice = dice;
		msg("Jail space is at {}",JailSpaceLocation);

		availableImprovements = new HashMap<>();
		availableImprovements.put(ImprovementType.House,32);
		availableImprovements.put(ImprovementType.Hotel,12);
	}

	private void findJailSpace(final List<Space> gameboard) {
		for (int i = 0; i < gameboard.size(); i++) {
			if (gameboard.get(i) instanceof JailSpace) {
				JailSpaceLocation = i;
				break;
			}
		}
	}

	private Queue<DrawCard> shuffle(final Collection<DrawCard> cards) {
		List<DrawCard> tmp = new LinkedList<>(cards);
		Collections.shuffle(tmp);
		return new LinkedList<>(tmp);
	}

	public Player play() {
		int turn = 1;
		PlayerIterator playerIterator = new PlayerIterator(activePlayers);

		while (activePlayers.size() > 1) {
			MDC.put("Turn", Integer.toString(turn));
			doGameTurn(dice, playerIterator);
			turn++;

			if (turn > MAX_TURNS) {
				msg("Too many turns ({}) - game ends in a tie",turn);
				return Player.BANK;
			}
		}

		Player winner = activePlayers.get(0);
		msg("Game winner is {}", winner);
		return winner;
	}

	void doGameTurn(final Dice dice, final PlayerIterator playerIterator) {
		Player currentPlayer = playerIterator.currentPlayer();
		MDC.put("Player", currentPlayer.toString());
		msg("Starting turn with $" + currentPlayer.getCash() + " and " + currentPlayer.getOwnedProperties().size() + " properties");

		doPreTurnActions(activePlayers);
		doPlayerTurn(currentPlayer, dice);

		if (currentPlayer.isBankrupt()) {
			msg("Player is bankrupt");
			removePlayerFromGame(activePlayers, currentPlayer);
		}

		playerIterator.nextPlayer();
	}

	/**
	 * @param next
	 */
	private void doPlayerTurn(final Player player, final Dice dice) {
		DiceOutput roll = null;
		if (playerMustFirstLeaveJail(player)) {
			forcePlayerToLeaveJail(player);
		}
		if (playerIsInJail(player)) {
			for (int i = 0; i < ROLLS_FOR_JAIL; i++) {
				roll = dice.roll();
				if (roll.isDouble()) {
					msg("Player rolled {} to get out of jail",roll);
					break;
				}
			}
			if (!roll.isDouble()) {
				playerHasAnotherTurnInJail(player);
				return; // Didn't get out of jail.
			}
			else {
				playerIsOutOfJail(player);
			}
		}
		else {
			for (int i = 0; i < ROLLS_FOR_JAIL; i++) {
				roll = dice.roll();
				if (!roll.isDouble()) {
					break;
				}
			}
			if (roll.isDouble()) {
				sendPlayerToJail(player);
				return;
			}
		}

		msg("Player rolls {}", roll);
		moveToRelativeOffset(player, roll.firstDie + roll.secondDie);
		executeActionsForSpace(player, roll.firstDie + roll.secondDie);
	}

	public void executeActionsForSpace(final Player player, final int diceRoll) {
		Space currentSpace = gameboard.get(player.getLocation());
		if (currentSpace instanceof ChanceSpace) {
			DrawCard card = chanceCards.remove();
			GameAction action = card.play();
			msg("Player lands on chance and draws {}",action.getDescription());
			action.execute(player, diceRoll, this);
			this.chanceCards.offer(card);
		}
		else if (currentSpace instanceof CommunityChestSpace) {
			DrawCard card = communityChestCards.remove();
			GameAction action = card.play();
			msg("Player lands on community chest and draws {}",action.getDescription());
			action.execute(player, diceRoll, this);
			this.communityChestCards.offer(card);
		}
		else if (currentSpace instanceof FreeParkingSpace) {
			// Noop
		}
		else if (currentSpace instanceof GoSpace) {
			// Noop
		}
		else if (currentSpace instanceof JailSpace) {
			// noop
		}
		else if (currentSpace instanceof GoToJailSpace) {
			sendPlayerToJail(player);
		}
		else if (currentSpace instanceof PropertySpace) {
			landOnProperty((PropertySpace)currentSpace, player, diceRoll);
		}
		else if (currentSpace instanceof PayTheBankSpace) {
			payTheBank((PayTheBankSpace)currentSpace, player);
		}
		else {
			throw new IllegalStateException("Unhandled space type " + currentSpace.getClass());
		}

	}

	private void payTheBank(final PayTheBankSpace currentSpace, final Player player) {
		int required = currentSpace.getAmount();
		msg("Player lands on {} and must pay the bank ${}",currentSpace.toString(), required);
		player.payCashToPlayer(required, Player.BANK, this);
	}

	public void landOnProperty(final PropertySpace currentSpace, final Player player, final int diceRoll, final RentOverride override) {
		PropertySpace space = currentSpace;
		Property property = space.getProperty();
		msg("Player lands on {}",property);

		if (unownedProperties.contains(property)) {
			offerToBuyProperty(player,property);
			if (unownedProperties.contains(property)) {
				auctionProperty(property);
			}
		}
		else {
			Player owningPlayer = ownedProperties.get(property);
			if (!owningPlayer.equals(player)) {
				OwnedProperty ownedProperty = owningPlayer.getOwnedProperty(property);
				if (!ownedProperty.isMortgaged()) {
					msg("Player must pay {} rent to {}", ownedProperty.getRent(diceRoll, override), owningPlayer);
					new PayRent(player, ownedProperty, diceRoll, override).execute(this);
				}
			}
		}
	}

	public void landOnProperty(final PropertySpace currentSpace, final Player player, final int diceRoll) {
		landOnProperty(currentSpace, player, diceRoll, null);
	}

	public BuyProperty buyProperty(final Player player, final Property property) {
		msg("Player buys {}",property);
		return new BuyProperty(property, player, ownedProperties, unownedProperties);
	}

	private void offerToBuyProperty(final Player player, final Property property) {
		if (player.wantsToBuy(property)) {
			buyProperty(player,property).execute(this);
		}
		else {
			msg("Player does not buy {}",property);
		}
	}

	public void movePlayerToGo(final Player player) {
		collectSalary(player);
		movePlayerToLocation(player, GO_SPACE);
	}

	public void moveToRelativeOffset(final Player player, final int numSpacesFromCurrent) {
		int currentLocation = player.getLocation();
		int newLocation = (currentLocation + numSpacesFromCurrent) % gameboard.size();
		if (newLocation < currentLocation) {
			// Passed go.
			collectSalary(player);
		}
		movePlayerToLocation(player, newLocation);
	}

	public void moveToNamedSpace(final Player player, final String location) {
		int currentLocation = player.getLocation();
		for (int i = 1; i < gameboard.size(); i++) {
			int nextLocation = (currentLocation + i)% gameboard.size();
			if (gameboard.get(nextLocation) instanceof PropertySpace) {
				PropertySpace ps = (PropertySpace)gameboard.get(nextLocation);
				if (ps.getProperty().getName().equals(location)) {
					moveToRelativeOffset(player, i);
					return;
				}
			}
		}
		throw new IllegalArgumentException("No location named " + location);
	}

	public void goToNearestSpaceOfType(final Player current, final int currentDiceRoll, final MonopolySet set, final RentOverride override) {
		int currentLocation = current.getLocation();
		List<Space> gameBoard = gameboard;
		currentLocation = (currentLocation+1) % gameBoard.size();
		Space currentSpace;
		while( true ) {
			currentSpace = gameBoard.get(currentLocation);
			if (currentSpace instanceof PropertySpace) {
				PropertySpace ps = (PropertySpace)currentSpace;
				if (ps.getProperty().getMonopolySet() == set) {
					moveToRelativeOffset(current, current.getLocation()+currentLocation);
					landOnProperty(ps, current, currentDiceRoll, override);
					return;
				}

			}
			currentLocation = (currentLocation+1) % gameBoard.size();
		}
	}
	public void movePlayerToLocation(final Player player, final int newLocation) {
		msg("Player moves to {}",gameboard.get(newLocation));
		spacesPerPlayer.put(player, newLocation);
		player.setLocation(newLocation);
	}

	private void collectSalary(final Player player) {
		msg("Player collects {}",SALARY);
		player.receiveCash(SALARY);
	}

	public void sendPlayerToJail(final Player player) {
		msg("Player is sent to jail");
		movePlayerToLocation(player, JailSpaceLocation);
		playersInJail.put(player, 0);
	}

	private void playerIsOutOfJail(final Player player) {
		msg("Player leaves jail");
		playersInJail.remove(player);
	}

	void playerHasAnotherTurnInJail(final Player player) {
		msg("Player has another turn in jail");
		Integer currentTurns = playersInJail.get(player);
		playersInJail.put(player, currentTurns+1);
	}

	private boolean playerIsInJail(final Player player) {
		return playersInJail.containsKey(player);
	}

	private void forcePlayerToLeaveJail(final Player player) {
		msg("Player is forced to leave jail");
		BankMoneyAction action = new BankMoneyAction(-PostBail.BAIL, "Get out of jail via bail");
		action.execute(player, ROLL_NOT_NEEDED, this);
		playerIsOutOfJail(player);
	}

	private boolean playerMustFirstLeaveJail(final Player player) {
		Integer currentTurnsInJail = playersInJail.get(player);
		return (currentTurnsInJail != null && currentTurnsInJail == MAX_TURNS_IN_JAIL);
	}

	private void doPreTurnActions(final List<Player> activePlayers) {
		// (mortgage, improve, sell improvement, get out of jail)
		for (Player player : activePlayers) {
			Optional<PlayerAction> action;
			do {
				action = player.getPreTurnAction(this);
				if (action.isPresent()) {
					action.get().execute(this);
				}
			} while (action.isPresent());
		}
	}

	public List<Player> getActivePlayers() {
		return activePlayers;
	}

	private void removePlayerFromGame(final List<Player> activePlayers, final Player player) {
		activePlayers.remove(player);
		// Give all the player's assets to their creditor
		Player creditor = player.getCreditor();
		if (creditor == null) {
			throw new IllegalStateException("Bankrupt player " + player + " must have a creditor!");
		}
		transferAssetsToCreditor(player, creditor);
	}

	private void transferAssetsToCreditor(final Player player, final Player creditor) {
		// Sell all improvements on all properties.
		// TODO - this causes concurrent modification exceptions!
		int improvementValue = 0;

		try {
			Iterator<OwnedProperty> it = player.getOwnedProperties().iterator();
			while (it.hasNext()) {
				OwnedProperty property = it.next();
				improvementValue += transferAsset(creditor, property);
			}
		} catch (ConcurrentModificationException e) {
			System.err.println("Transferring assets from " + player + " to " + creditor);
			throw e;
		}

		if (creditor != Player.BANK) {
			creditor.receiveCash(player.getCash() + improvementValue);
		}
	}

	private int transferAsset(final Player creditor, final OwnedProperty property) {
		int improvementValue = 0;
		int improvements = property.getNumImprovements();
		Property theProperty = property.getProperty();
		for (int i = 0; i < improvements; i++) {
			improvementValue += theProperty.getHouseCost();
			property.removeImprovement();
		}
		if (creditor == Player.BANK) {
			unownedProperties.add(theProperty);
		}
		else {
			creditor.addOwnedProperty(theProperty);
			ownedProperties.put(theProperty, creditor);
		}
		return improvementValue;
	}

	private void auctionProperty(final Property property) {
		// TODO Not implemented yet
		msg("Auctioning {}",property);
	}

	boolean isPlayerInJail(final Player player) {
		return playersInJail.get(player) != null;
	}

	Map<Property, Player> getOwnedProperties() {
		return ownedProperties;
	}

	Set<Property> getUnownedProperties() {
		return unownedProperties;
	}

	private void msg(final String string, final Object...objects) {
		logger.info(string,objects);
	}

	public List<Player> getPlayers() {
		return players;
	}

	public boolean moreImprovementsAvailable(final ImprovementType improvementType) {
		return availableImprovements.get(improvementType) > 0;
	}

	public void markPropertyAsUsed(final ImprovementType improvementType) {
		Integer currentlyAvailable = this.availableImprovements.get(improvementType);
		this.availableImprovements.put(improvementType,currentlyAvailable-1);
	}
	public void markPropertyAsUnused(final ImprovementType improvementType) {
		Integer currentlyAvailable = this.availableImprovements.get(improvementType);
		this.availableImprovements.put(improvementType,currentlyAvailable+1);
	}
}
