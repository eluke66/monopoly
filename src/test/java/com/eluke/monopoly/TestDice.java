package com.eluke.monopoly;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.hamcrest.junit.MatcherAssert;
import org.hamcrest.number.IsCloseTo;
import org.junit.Test;

import com.eluke.monopoly.Dice.DiceOutput;

public class TestDice {

	private static final int RUNS = 60000;
	private static final int EXPECTED = 60000/6;
	private static final int DELTA = (int)(EXPECTED*.97);

	@Test
	public void ensureAllDicePopUpWithEqualProbability() {
		Dice dice = new Dice();
		
		int[] countsPerRoll = {0,0,0,0,0,0};
		for (int i = 0; i < RUNS; i++) {
			DiceOutput roll = dice.roll();
			countsPerRoll[roll.firstDie]++;
		}
		
		for (int i = 0; i < countsPerRoll.length; i++) {
			MatcherAssert.assertThat((double)countsPerRoll[i], IsCloseTo.closeTo(EXPECTED, DELTA));
		}
	}

	@Test
	public void ensureDoublesAreReportedCorrectly() {
		assertTrue(new DiceOutput(3,3).isDouble());
		assertFalse(new DiceOutput(3,4).isDouble());
	}
}
