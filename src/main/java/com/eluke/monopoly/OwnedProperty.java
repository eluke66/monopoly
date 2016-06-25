/**
 * 
 */
package com.eluke.monopoly;

/**
 * @author luke
 *
 */
public class OwnedProperty {
	private final Property property;
	private Player owner;
	private int numImprovements = 0;

	public OwnedProperty(final Property property) {
		this.property = property;
	}

	public Property getProperty() {
		return property;
	}

	public Player getOwner() {
		return owner;
	}

	public int getNumImprovements() {
		return numImprovements;
	}

	public void setOwner(final Player player) {
		this.owner = player;
	}

	public void improve() {
		numImprovements++;
	}

	public void removeImprovement() {
		numImprovements--;
		if (numImprovements < 0) {
			throw new IllegalStateException("Cannot remove improvements on " + this + " as it has no more improvements");
		}
	}
}
