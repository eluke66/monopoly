/**
 * 
 */
package com.eluke.monopoly;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.eluke.monopoly.spaces.Space;
import com.eluke.monopoly.spaces.SpaceFactory;
import com.google.gson.Gson;

/**
 * @author luke
 *
 */
public class ConfigLoader {
	private static final String SPACE_FILE = "/spaces.json";
	private static final String PROPERTIES_FILE = "/properties.json";
	private SpaceFactory spaceFactory;

	public Set<Property> getProperties() {
		Gson gson = new Gson();
		try (InputStream stream = getClass().getResourceAsStream(PROPERTIES_FILE)) {
			String fileContents = new Scanner(stream, "UTF-8").useDelimiter("\\A").next();
			List<Map<String,Object>> contents = gson.fromJson(fileContents, List.class);
			return contents.stream().map(new Function<Map<String,Object>,Property>() {
				@Override
				public Property apply(final Map<String, Object> t) {
					return PropertyFactory.makeProperty(t);
				}
			}).collect(Collectors.<Property>toSet());

		} catch (IOException e) {
			throw new RuntimeException("Problem loading properties", e);
		}
	}
	public List<Space> getSpaces() {
		init();
		List<Space> spaces = new LinkedList<Space>();
		Gson gson = new Gson();
		try (InputStream stream = getClass().getResourceAsStream(SPACE_FILE)) {
			String fileContents = new Scanner(stream, "UTF-8").useDelimiter("\\A").next();
			List<?> contents = gson.fromJson(fileContents, List.class);
			for (Object o : contents) {
				Space space = spaceFactory.makeSpace(o.toString());
				spaces.add(space);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return spaces;
	}

	private void init() {
		// Load properties!
		// Initialize space factory
	}
	public static void main(final String[] args) {
		new ConfigLoader().getProperties();
	}
}
