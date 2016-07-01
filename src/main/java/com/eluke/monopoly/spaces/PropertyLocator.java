package com.eluke.monopoly.spaces;

import java.util.Map;

import com.eluke.monopoly.Property;

public class PropertyLocator {
	private final Map<String,Property> propertiesByName;

	public PropertyLocator(final Map<String, Property> propertiesByName) {
		this.propertiesByName = propertiesByName;
	}

	public Property getPropertyByName(final String spaceName) {
		Property property = propertiesByName.get(spaceName);
		if (property == null) {
			throw new IllegalArgumentException("No property named " + spaceName);
		}
		return property;
	}

}
