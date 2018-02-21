package org.genrat.core;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import freemarker.ext.dom.NodeModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

public class FMEx {

	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
		
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);

        // Where do we load the templates from:
        cfg.setClassForTemplateLoading(Main.class, "templates");

        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.UK);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        
        Template template = cfg.getTemplate("helloworld.ftl");
        template.getName();
		NodeModel.parse(new File("C:\\tmp\\gr\\xml\\employees.xml"));
	}
}
