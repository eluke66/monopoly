package com.eluke.monopoly.actions;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.Before;
import org.junit.Test;

import com.eluke.monopoly.Game;
import com.eluke.monopoly.OwnedProperty;

public class TestMortgageProperty {
	private OwnedProperty owned;
	private Game game;

	@Before
	public void setup() {
		owned = createMock(OwnedProperty.class);
	}

	@Test
	public void whenExecutedCausesAPropertyMortgage() {
		expect(owned.getNumImprovements()).andReturn(0);
		owned.mortgage();
		replay(owned);

		MortgageProperty action = new MortgageProperty(owned);
		action.execute(game);

		verify(owned);
	}

	@Test(expected=IllegalStateException.class)
	public void cannotMortgageAnImprovedProperty() {
		expect(owned.getNumImprovements()).andReturn(1).anyTimes();
		replay(owned);

		MortgageProperty action = new MortgageProperty(owned);
		action.execute(game);

	}
}
