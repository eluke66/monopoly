/**
 *
 */
package com.eluke.monopoly.spaces;

/**
 * @author luke
 *
 */
public class PayTheBankSpace implements Space {
	private final String description;
	private final int amount;

	public PayTheBankSpace(final String description, final int amount) {
		this.description = description;
		this.amount = amount;
	}

	@Override
	public String toString() { return description; }

	public int getAmount() {
		return amount;
	}
}
