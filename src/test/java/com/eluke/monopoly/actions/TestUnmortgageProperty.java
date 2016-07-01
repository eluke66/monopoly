package com.eluke.monopoly.actions;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.Before;
import org.junit.Test;

import com.eluke.monopoly.Game;
import com.eluke.monopoly.OwnedProperty;

public class TestUnmortgageProperty {
	private OwnedProperty owned;
	private Game game;

	@Before
	public void setup() {
		owned = createMock(OwnedProperty.class);
	}

	@Test
	public void whenExecutedCausesAPropertyUnmortgage() {
		expect(owned.isMortgaged()).andReturn(true);
		owned.unmortgage();
		replay(owned);

		UnmortgageProperty action = new UnmortgageProperty(owned);
		action.execute(game);

		verify(owned);
	}

	@Test(expected=IllegalStateException.class)
	public void cannotUnmortgageANonMortgagedProperty() {
		expect(owned.isMortgaged()).andReturn(false);
		replay(owned);

		UnmortgageProperty action = new UnmortgageProperty(owned);
		action.execute(game);

	}
}
