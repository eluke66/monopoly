/**
 *
 */
package com.eluke.monopoly.actions;

import com.eluke.monopoly.Game;
import com.eluke.monopoly.Player;

/**
 * @author luke
 *
 */
public interface GameAction {
	public void execute(Player current, int currentDiceRoll, Game game);
	public String getDescription();
}
