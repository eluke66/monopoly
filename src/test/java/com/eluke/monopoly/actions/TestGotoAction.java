package com.eluke.monopoly.actions;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.Before;
import org.junit.Test;

import com.eluke.monopoly.Game;
import com.eluke.monopoly.MonopolySet;
import com.eluke.monopoly.OwnedProperty.RentOverride;
import com.eluke.monopoly.Player;

public class TestGotoAction {
	private Game game;
	private Player player;
	
	@Before
	public void setup() {
		game = createMock(Game.class);
		player = createMock(Player.class);
	}
	
	@Test
	public void utilityGoesToNearestUtility() {
		game.goToNearestSpaceOfType(player, 0, MonopolySet.Utility, RentOverride.TEN_TIMES);
		replay(game);
		
		new GotoAction("utility", "").execute(player, 0, game);
		
		verify(game);
	}
	
	@Test
	public void railroadGoesToNearestRailroad() {
		game.goToNearestSpaceOfType(player, 0, MonopolySet.Railroad, RentOverride.DOUBLE);
		replay(game);
		
		new GotoAction("railroad", "").execute(player, 0, game);
		
		verify(game);
	}
	
	@Test
	public void goGoesToGo() {
		game.movePlayerToGo(player);
		replay(game);
		
		new GotoAction("go", "").execute(player, 0, game);
		
		verify(game);
	}
	
	@Test
	public void jailGoesToJail() {
		game.sendPlayerToJail(player);
		replay(game);
		
		new GotoAction("jail", "").execute(player, 0, game);
		
		verify(game);
	}
	
	@Test
	public void relativeOffsetGoesToRelativeOffset() {
		game.moveToRelativeOffset(player, -1);
		game.executeActionsForSpace(player, 0);
		replay(game);
		
		new GotoAction("-1", "").execute(player, 0, game);
		verify(game);
	}
	
	@Test
	public void namedPropertyGoesToProperty() {
		game.moveToNamedSpace(player, "someplace");
		game.executeActionsForSpace(player, 0);
		replay(game);
		
		new GotoAction("someplace", "").execute(player, 0, game);
		verify(game);
	}
}
