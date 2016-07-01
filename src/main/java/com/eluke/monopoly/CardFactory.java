package com.eluke.monopoly;

import java.util.Map;

import com.eluke.monopoly.actions.AssessmentAction;
import com.eluke.monopoly.actions.BankMoneyAction;
import com.eluke.monopoly.actions.GameAction;
import com.eluke.monopoly.actions.GetOutOfJailFreeAction;
import com.eluke.monopoly.actions.GotoAction;
import com.eluke.monopoly.actions.PlayerMoneyAction;
import com.eluke.monopoly.cards.DrawCard;

public class CardFactory {

	public static DrawCard makeCard(Map<String, Object> t) {
		final String type = t.get("type").toString();
		final String description = t.get("description").toString();
		if (type.equals("assessment")) {
			return new DrawCard() {
				@Override
				public GameAction play() {
					int houseCost = ((Double)t.get("house")).intValue();
					int hotelCost = ((Double)t.get("hotel")).intValue();
					return new AssessmentAction(houseCost, hotelCost, description);
				}
			};
		}
		else if (type.equals("bankMoney")) {
			return new DrawCard() {
				@Override
				public GameAction play() {
					int income = ((Double)t.get("income")).intValue();
					return new BankMoneyAction(income, description);
				}
				
			};
		}
		else if (type.equals("playerMoney")) {
			return new DrawCard() {
				@Override
				public GameAction play() {
					int income = ((Double)t.get("income")).intValue();
					return new PlayerMoneyAction(income, description);
				}
			};
		}
		else if (type.equals("goto")) {
			return new DrawCard() {
				@Override
				public GameAction play() {
					String location = t.get("location").toString();
					return new GotoAction(location, description);
				}
			};
		}
		else if (type.equals("getOutOfJailFree")) {
			return new DrawCard() {
				@Override
				public GameAction play() {
					return new GetOutOfJailFreeAction(description);
				}
			};
		}
		else {
			throw new IllegalArgumentException("Unknown card type " + type);
		}
	}
}
