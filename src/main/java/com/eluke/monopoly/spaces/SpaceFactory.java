/**
 *
 */
package com.eluke.monopoly.spaces;

/**
 * @author luke
 *
 */
public class SpaceFactory {
	private final PropertyLocator propertyLocator;

	public SpaceFactory(final PropertyLocator propertyLocator) {
		this.propertyLocator = propertyLocator;
	}

	public Space makeSpace(final String spaceName) {
		if (spaceName.equals("go")) {
			return new GoSpace();
		}
		else if (spaceName.equals("jail")) {
			return new JailSpace();
		}
		else if (spaceName.equals("Go to Jail")) {
			return new GoToJailSpace();
		}
		else if (spaceName.equals("Free Parking")) {
			return new FreeParkingSpace();
		}
		else if (spaceName.equals("Community Chest")) {
			return new CommunityChestSpace();
		}
		else if (spaceName.equals("Chance")) {
			return new ChanceSpace();
		}
		else if (spaceName.equals("Income Tax")) {
			return new PayTheBankSpace(spaceName, 200);
		}
		else if (spaceName.equals("Luxury Tax")) {
			return new PayTheBankSpace(spaceName, 100);
		}
		else {
			return new PropertySpace(propertyLocator.getPropertyByName(spaceName));
		}
	}
}
