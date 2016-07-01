package com.eluke.monopoly.actions;

import com.eluke.monopoly.Game;
import com.eluke.monopoly.Player;

public class BankMoneyAction implements GameAction {
	private final int moneyFromBank;
	private final String description;

	public BankMoneyAction(final int income, final String description) {
		this.moneyFromBank = income;
		this.description = description;
	}

	@Override
	public void execute(final Player player,final int currentDiceRoll, final Game game) {
		if (moneyFromBank > 0) {
			player.receiveCash(moneyFromBank);
		}
		else {
			player.payCashToPlayer(-moneyFromBank, Player.BANK, game);
		}
	}

	@Override
	public String getDescription() {
		return description;
	}
}
