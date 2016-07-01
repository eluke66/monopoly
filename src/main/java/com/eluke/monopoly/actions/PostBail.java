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
public class PostBail implements PlayerAction {

	public static final int BAIL = 50;
	private final Player player;

	public PostBail(final Player player) {
		this.player = player;
	}

	@Override
	public void execute(final Game game) {
		player.payCashToPlayer(BAIL, Player.BANK, game);
	}

}
