/**
 *
 */
package com.eluke.monopoly.actions;

import com.eluke.monopoly.Game;

/**
 * @author luke
 *Buy Property, Pay Rent, Mortgage Property, Improve Property, Sell Improvement, Get out of Jail by paying
 */
public interface PlayerAction {
	public void execute(Game game);
}
