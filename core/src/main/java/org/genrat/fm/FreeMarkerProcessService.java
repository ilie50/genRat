package org.genrat.fm;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreeMarkerProcessService {

	protected static Configuration CFG = null;

	static {
		CFG = new Configuration(Configuration.VERSION_2_3_20);
	}

	public String process(String templateString, Serializable data) {

		try (StringReader reader = new StringReader(templateString);
				StringWriter out = new StringWriter()) {

			Template template = new Template("templateName", reader, CFG);

			// Load template
			//Template template = cfg.getTemplate("BuildXMLTemplate.ftl");


			// Write output on console example 1
			template.process(data, out);
			String result = out.getBuffer().toString();
//			System.out.println(result);
			out.flush();
			return result;
		} catch (IOException | TemplateException e) {
			throw new RuntimeException(e);
		}
	}
}