package com.eluke.monopoly.actions;

import com.eluke.monopoly.OwnedProperty;

public interface PlayerPropertyAction extends PlayerAction {
	public OwnedProperty getProperty();
}
