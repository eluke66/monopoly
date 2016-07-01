/**
 *
 */
package com.eluke.monopoly.spaces;

import com.eluke.monopoly.Property;
import com.google.common.base.Preconditions;

/**
 * @author luke
 *
 */
public class PropertySpace implements Space {
	private final Property property;

	public PropertySpace(final Property property) {
		Preconditions.checkNotNull(property);
		this.property = property;
	}

	public Property getProperty() {
		return property;
	}

	@Override
	public String toString() { return property.getName(); }
}
