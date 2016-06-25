/**
 * 
 */
package com.eluke.monopoly.spaces;

import com.eluke.monopoly.Property;

/**
 * @author luke
 *
 */
public class PropertySpace implements Space {
	private final Property property;

	public PropertySpace(final Property property) {
		this.property = property;
	}

	public Property getProperty() {
		return property;
	}
}
