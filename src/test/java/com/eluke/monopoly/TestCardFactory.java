package com.eluke.monopoly;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import org.junit.Test;

import com.eluke.monopoly.actions.AssessmentAction;
import com.eluke.monopoly.actions.BankMoneyAction;
import com.eluke.monopoly.actions.GameAction;
import com.eluke.monopoly.actions.GetOutOfJailFreeAction;
import com.eluke.monopoly.actions.GotoAction;
import com.eluke.monopoly.actions.PlayerMoneyAction;
import com.eluke.monopoly.cards.DrawCard;
import com.google.common.collect.ImmutableMap;

public class TestCardFactory {

	@Test
	public void assessmentsAreSupported() {
		ImmutableMap.Builder<String,Object> builder = type("assessment");
		builder
		.put("house",1.0)
		.put("hotel",2.0);
		assertGameActionIsA(builder,AssessmentAction.class);
	}

	@Test
	public void bankMoneyIsSupported() {
		ImmutableMap.Builder<String,Object> builder = 
				type("bankMoney")
				.put("income",1.0);
		assertGameActionIsA(builder,BankMoneyAction.class);
	}
	
	@Test
	public void playerMoneyIsSupported() {
		ImmutableMap.Builder<String,Object> builder = 
				type("playerMoney")
				.put("income",1.0);
		assertGameActionIsA(builder,PlayerMoneyAction.class);
	}
	
	@Test
	public void gotoIsSupported() {
		ImmutableMap.Builder<String,Object> builder = 
				type("goto")
				.put("location","somewhere");
		assertGameActionIsA(builder,GotoAction.class);
	}
	@Test
	public void getOutOfJailFreeIsSupported() {
		ImmutableMap.Builder<String,Object> builder = 
				type("getOutOfJailFree");
		assertGameActionIsA(builder,GetOutOfJailFreeAction.class);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void unknownTypesThrow() {
		ImmutableMap.Builder<String,Object> builder = 
				type("something");
		CardFactory.makeCard(builder.build());
	}
	
	private ImmutableMap.Builder<String,Object> type(String type) {
		ImmutableMap.Builder<String,Object> builder = new ImmutableMap.Builder<String,Object>();
		builder.put("type",type).put("description", "");
		return builder;
	}

	private void assertGameActionIsA(ImmutableMap.Builder<String,Object> builder, Class<? extends GameAction> expectedClass) {
		DrawCard card = CardFactory.makeCard(builder.build());
		GameAction action = card.play();

		assertThat(action, instanceOf(expectedClass));
	}
}
