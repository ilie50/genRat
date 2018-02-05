package org.genrat.fm;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreeMarkerGenrateXMLFromObject {

	private static Configuration cfg = null;

	static {
		cfg = new Configuration(Configuration.VERSION_2_3_20);
	}

	public String process(String content) {

		try (StringReader reader = new StringReader(content);
				StringWriter out = new StringWriter()) {

			Template template = new Template("templateName", reader, cfg);

			// Load template
			//Template template = cfg.getTemplate("BuildXMLTemplate.ftl");

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

			// Write output on console example 1
			template.process(templateData, out);
			String result = out.getBuffer().toString();
//			System.out.println(result);
			out.flush();
			return result;
		} catch (IOException | TemplateException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		//String fileInputPath = "C:\\tmp\\gr\\RemoteDebugginSetup.docx";

		try {

			//Template template = new Template("templateName", new StringReader(new Main().readDoc(fileInputPath)), cfg);

			// Load template
			Template template = cfg.getTemplate("BuildXMLTemplate.ftl");

			// Create data for template
			Map<String, Object> templateData = new HashMap<String, Object>();
			templateData.put("name", "Java Honk");

			List<Person> personDetails = Arrays.asList(new Person("Java Honk", "USA", 23),
					new Person("Java Monk", "France", 15));

			templateData.put("personDetails", personDetails);

			// Write output on console example 1
			StringWriter out = new StringWriter();
			template.process(templateData, out);
			System.out.println(out.getBuffer().toString());
			out.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}