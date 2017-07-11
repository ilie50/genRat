package core;

import java.io.StringWriter;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;

public class VelocityEx {

	private static final String DEFAULT_ENCODING = "UTF-8";

	public static void main(String[] args) {

		
		SAXBuilder builder;
		Document root = null;

		try {
			builder = new SAXBuilder();
			root = builder.build("C:\\tmp\\gr\\xml\\employees.xml");
		} catch (Exception ee) {
		}

		VelocityContext context = new VelocityContext();
		context.put("root", root.getRootElement());

		String doc = Main.readDoc();
		
		VelocityEngine engine = new VelocityEngine(getProperties()); 
		engine.init(); 
		
		
		StringWriter sw = new StringWriter();
		Template veTemplate = engine.getTemplate(doc); 
		veTemplate.merge(context, sw); 
		
		System.out.println(sw.toString());
	}

	private static Properties getProperties() {
		Properties p = new Properties(); 

		p.setProperty( RuntimeConstants.INPUT_ENCODING, DEFAULT_ENCODING ); 
		p.setProperty( RuntimeConstants.OUTPUT_ENCODING, DEFAULT_ENCODING ); 

		p.setProperty( RuntimeConstants.RESOURCE_MANAGER_CLASS, 
		       "org.apache.velocity.runtime.resource.ResourceManagerImpl" ); 
		p.setProperty( RuntimeConstants.RESOURCE_MANAGER_CACHE_CLASS, 
		       "org.apache.velocity.runtime.resource.ResourceCacheImpl" ); 
		p.setProperty( RuntimeConstants.RESOURCE_MANAGER_DEFAULTCACHE_SIZE, "100" ); 

		p.setProperty( RuntimeConstants.RESOURCE_LOADER, "string" ); 
		p.setProperty( "string.resource.loader.class", 
		       "loader.SimpleStringResourceLoader" ); 
		p.setProperty( "string.resource.loader.encoding", DEFAULT_ENCODING ); 
		p.setProperty( "string.resource.loader.cache", Boolean.TRUE.toString() ); 
		p.setProperty( "string.resource.loader.modificationCheckInterval", "-1" ); 
		return p;
	}
}
