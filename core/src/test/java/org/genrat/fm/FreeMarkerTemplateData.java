package org.genrat.fm;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FreeMarkerTemplateData {

	public Serializable getTemplateData() {
		// Create data for template
		Map<String, Object> templateData = new HashMap<String, Object>();
		templateData.put("name", "Java Honk NM");
		templateData.put("newName", "Java Tonk NW");
		templateData.put("age", 22);
		
		List<Person> personDetails = Arrays.asList(new Person("Java Honk US", "USA", 23),
				new Person("Java Monk FR", "France", 15));

		templateData.put("personDetails", personDetails);
		List<Section> sections = Arrays.asList(new Section("Section 1", 17), new Section("Section 2", 19));
		templateData.put("sections", sections);
		return (Serializable) templateData;
	}
}
