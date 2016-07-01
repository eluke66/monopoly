package com.eluke.monopoly;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.eluke.monopoly.OwnedProperty.RentOverride;

public class TestOwnedProperty {
	private static final int DICE_ROLL = 3;
	private static final int UNMORTGAGE_VALUE = 40;
	private static final int MORTGAGE_VALUE = 30;
	private static final int NEXT_RENT = 20;
	private static final int BASE_RENT = 8;
	private static final int ANY_DICE_VALUE = 0;
	private Property theProperty;
	private Player owner;
	private OwnedProperty property;


	@Test
	public void canImproveAndRemoveProperty() {
		setup();
		replay();

		property.improve();
		property.removeImprovement();
		assertThat(property.getNumImprovements(),is(0));
	}


	@Test(expected=IllegalStateException.class)
	public void canImproveButNotPastLimit() {
		setup();
		replay();

		property.improve();
		property.improve();
	}

	@Test(expected=IllegalStateException.class)
	public void canRemoveImprovementsButNotPastLimit() {
		setup();
		replay();

		property.improve();
		property.removeImprovement();
		property.removeImprovement();
	}

	@Test(expected=IllegalStateException.class)
	public void cannotImproveIfMortgaged() {
		setup();
		owner.receiveCash(MORTGAGE_VALUE);
		replay();

		property.mortgage();
		property.improve();
	}

	@Test(expected=IllegalStateException.class)
	public void cannotMortgageWhenMortgaged() {
		setup();
		owner.receiveCash(MORTGAGE_VALUE);
		replay();

		property.mortgage();
		property.mortgage();
	}

	@Test(expected=IllegalStateException.class)
	public void cannotUnmortgageWhenNotMortgaged() {
		setup();
		replay();

		property.unmortgage();
	}

	@Test
	public void mortgagingGivesCashToPlayer() {
		setup();
		owner.receiveCash(MORTGAGE_VALUE);
		replay();
		property.mortgage();

		verify(owner);
	}

	@Test
	public void unmortgagingRequiresCash() {
		setup();
		owner.receiveCash(MORTGAGE_VALUE);
		owner.payMoney(UNMORTGAGE_VALUE);
		replay();

		property.mortgage();
		property.unmortgage();

		verify(owner);
	}

	@Test
	public void mortgagedPropertiesRequireNoRent() {
		setup();
		owner.receiveCash(MORTGAGE_VALUE);
		replay();

		property.mortgage();

		assertThat(property.getRent(ANY_DICE_VALUE),is(0));
		assertThat(property.getRent(ANY_DICE_VALUE, null),is(0));
		assertThat(property.getRent(ANY_DICE_VALUE, RentOverride.DOUBLE),is(0));
		assertThat(property.getRent(ANY_DICE_VALUE, RentOverride.TEN_TIMES),is(0));
	}

	@Test
	public void utilitiesPayRentByDice() {
		setup();
		expect(theProperty.getMonopolySet()).andReturn(MonopolySet.Utility).anyTimes();
		replay();


		assertThat(property.getRent(DICE_ROLL),is(BASE_RENT*DICE_ROLL));
	}

	@Test
	public void nonUtilitiesPayRentWithoutDice() {
		setup();
		expect(theProperty.getMonopolySet()).andReturn(MonopolySet.Railroad).anyTimes();
		replay();


		assertThat(property.getRent(DICE_ROLL),is(BASE_RENT));
	}

	@Test
	public void tenTimesRentOverrideForcesThatValue() {
		setup();
		expect(theProperty.getMonopolySet()).andReturn(MonopolySet.Utility).anyTimes();
		replay();

		assertThat(property.getRent(DICE_ROLL, RentOverride.TEN_TIMES),is(DICE_ROLL*10));
	}

	@Test
	public void doubleRentOverrideForcesThatValue() {
		setup();
		expect(theProperty.getMonopolySet()).andReturn(MonopolySet.Railroad).anyTimes();
		replay();

		assertThat(property.getRent(DICE_ROLL, RentOverride.DOUBLE),is(BASE_RENT*2));
	}

	@Test
	public void noRentOverrideIsIgnored() {
		setup();
		expect(theProperty.getMonopolySet()).andReturn(MonopolySet.Railroad).anyTimes();
		replay();

		assertThat(property.getRent(DICE_ROLL, null),is(BASE_RENT));
	}

	@Before
	public void setup() {
		theProperty = createMock(Property.class);
		owner = createMock(Player.class);
		expect(theProperty.getRentCosts()).andReturn(new int[]{BASE_RENT,NEXT_RENT}).anyTimes();
		expect(theProperty.getMortgageValue()).andReturn(MORTGAGE_VALUE).anyTimes();
		expect(theProperty.getUnMortgageValue()).andReturn(UNMORTGAGE_VALUE).anyTimes();
		property = new OwnedProperty(theProperty, owner);
	}

	private void replay() {
		EasyMock.replay(theProperty);
		EasyMock.replay(owner);
	}

}
