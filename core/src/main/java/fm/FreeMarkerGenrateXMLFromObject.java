package fm;

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Main;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class FreeMarkerGenrateXMLFromObject {

	private static Configuration cfg = null;

	static {
		cfg = new Configuration(Configuration.VERSION_2_3_20);
	}

	public static void main(String[] args) {

		try {

			Template template = new Template("templateName", new StringReader(Main.readDoc()), cfg);

			// Load template
			//Template template = cfg.getTemplate("BuildXMLTemplate.ftl");

			// Create data for template
			Map<String, Object> templateData = new HashMap<String, Object>();
			templateData.put("name", "Java Honk");

			List<Person> personDetails = Arrays.asList(new Person("Java Honk", "USA"),
					new Person("Java Monk", "France"));

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