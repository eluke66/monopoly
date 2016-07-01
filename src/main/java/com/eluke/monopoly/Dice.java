/**
 *
 */
package com.eluke.monopoly;

/**
 * @author luke
 *
 */
public class Dice {
	public static class DiceOutput {
		final int firstDie;
		final int secondDie;
		public DiceOutput(final int firstDie, final int secondDie) {
			this.firstDie = firstDie;
			this.secondDie = secondDie;
		}
		public boolean isDouble() {
			return firstDie == secondDie;
		}

		@Override
		public String toString() {
			return "" + firstDie + "," + secondDie;
		}
	}

	public DiceOutput roll() {
		int firstDie = (int)(Math.random()*6);
		int secondDie = (int)(Math.random()*6);

		return new DiceOutput(firstDie, secondDie);
	}
}
