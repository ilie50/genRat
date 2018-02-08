package org.genrat.fm;

import static org.genrat.fm.FreeMarkerProcessService.*;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import freemarker.template.Template;

public class FreeMarkerProcessServiceTest {

	public static void main(String[] args) {
		//String fileInputPath = "C:\\tmp\\gr\\RemoteDebugginSetup.docx";

		try {

			//Template template = new Template("templateName", new StringReader(new Main().readDoc(fileInputPath)), cfg);

			// Load template
			Template template = CFG.getTemplate("BuildXMLTemplate.ftl");

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
