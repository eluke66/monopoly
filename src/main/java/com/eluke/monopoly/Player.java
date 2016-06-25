/**
 * 
 */
package com.eluke.monopoly;

import java.util.HashSet;
import java.util.Set;

/**
 * @author luke
 *
 */
public class Player {
	private final Set<OwnedProperty> properties;
	private int cash;

	public Player(final int cash) {
		this.cash = cash;
		properties = new HashSet<OwnedProperty>();
	}
}
