/**
 * 
 */
package com.eluke.monopoly;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.eluke.monopoly.cards.DrawCard;
import com.eluke.monopoly.spaces.PropertyLocator;
import com.eluke.monopoly.spaces.Space;
import com.eluke.monopoly.spaces.SpaceFactory;
import com.google.gson.Gson;

/**
 * @author luke
 *
 */
public class ConfigLoader {
	private static final String SPACE_FILE = "spaces.json";
	private static final String PROPERTIES_FILE = "properties.json";
	private static final String CHANCE_FILE = "chance.json";
	private static final String COMMUNITY_CHEST_FILE = "community_chest.json";
	private SpaceFactory spaceFactory;
	private final String prefix;

	ConfigLoader(String prefix) {
		this.prefix = prefix;
	}
	public ConfigLoader() {
		this("/");
	}
	public Set<Property> getProperties() {
		Function<? super Map<String,Object>, Property> mapper = (t -> PropertyFactory.makeProperty(t));
		Collector<? super Property, ?, Set<Property>> collector = Collectors.<Property>toSet();
		return this.<Map<String,Object>,Property,Set<Property>>loadFromFile(PROPERTIES_FILE, mapper, collector);	
	}
	public List<Space> getSpaces() {
		Function<? super String, Space> mapper = (t -> spaceFactory.makeSpace(t));
		Collector<? super Space, ?, List<Space>> collector = Collectors.<Space>toList();
		init();
		return this.<String,Space,List<Space>>loadFromFile(SPACE_FILE, mapper, collector);
	}
	public Set<DrawCard> getChanceCards() {
		return loadCards(CHANCE_FILE);
	}
	public Set<DrawCard> getCommunityChestCards() {
		return loadCards(COMMUNITY_CHEST_FILE);
	}
	private <FileListType, OutputType,CollectionType> CollectionType loadFromFile(
			String filename, 
			Function<? super FileListType, OutputType> mapper,
			Collector<? super OutputType, ?, CollectionType> collector
			) {
		Gson gson = new Gson();
		Scanner scanner = null;
		try (InputStream stream = getClass().getResourceAsStream(prefix + filename)) {
			scanner = new Scanner(stream, "UTF-8");
			String fileContents = scanner.useDelimiter("\\A").next();
			@SuppressWarnings("unchecked")
			List<FileListType> contents = gson.fromJson(fileContents, List.class);
			return contents.stream().map(mapper).collect(collector);
		} catch (IOException e) {
			throw new RuntimeException("Problem loading from " + filename, e);
		}
		finally { 
			if (scanner != null) {
				scanner.close();
			}
		}
	}
	
	
	private Set<DrawCard> loadCards(String filename) {

		Function<? super Map<String,Object>, DrawCard> mapper = (t -> CardFactory.makeCard(t));
		Collector<? super DrawCard, ?, Set<DrawCard>> collector = Collectors.<DrawCard>toSet();
		return this.<Map<String,Object>,DrawCard,Set<DrawCard>>loadFromFile(filename, mapper, collector);	
	}
	
	private void init() {
		// Load properties!
		Set<Property> properties = getProperties();
		
		// Initialize space factory
		Map<String,Property> propertiesPerName = 
				properties.stream().collect(Collectors.toMap(Property::getName,
                        Function.identity()));
		spaceFactory = new SpaceFactory(new PropertyLocator(propertiesPerName));
	}
	
}
