package com.eluke.monopoly;

import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;
import org.junit.Test;

import com.eluke.monopoly.cards.DrawCard;
import com.eluke.monopoly.spaces.Space;

public class TestConfigLoader {

	@Test
	public void testLoadingChanceCards() {
		ConfigLoader loader = new ConfigLoader();
		
		Set<DrawCard> cards = loader.getChanceCards();
		assertThat(cards.size(),is(16));
	}

	@Test
	public void testLoadingCommunityChestCards() {
		ConfigLoader loader = new ConfigLoader();
		
		Set<DrawCard> cards = loader.getCommunityChestCards();
		assertThat(cards.size(),is(17));
	}
	
	@Test
	public void testLoadingProperties() {
		ConfigLoader loader = new ConfigLoader();
		
		Set<Property> properties = loader.getProperties();
		assertThat(properties.size(),is(28));
	}
	
	@Test
	public void testLoadingSpaces() {
		ConfigLoader loader = new ConfigLoader();
		
		List<Space> spaces = loader.getSpaces();
		assertThat(spaces.size(),is(40));
	}
	
	@Test(expected=RuntimeException.class)
	public void testMissingFileShouldThrow() {
		ConfigLoader loader = new ConfigLoader("someotherprefix");
		
		loader.getSpaces();
	}
}
