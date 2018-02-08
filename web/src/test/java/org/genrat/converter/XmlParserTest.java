package org.genrat.converter;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class XmlParserTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String xmlString = "YOUR XML STRING HERE IF YOU WANT PARSE DATA FROM STRING";
        File file = new File("xml1.xml");
        XmlParser xmlParser = new XmlParser(xmlString);
        xmlParser = new XmlParser(file);
        
        Map<String, Object> xmlMap = xmlParser.parseXML();
        print(xmlMap, 0);
    }

    @SuppressWarnings("unchecked")
	private static void print(Map<String, Object> map, Integer tab) {
        Iterator<Entry<String, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Object> pairs = it.next();
            String key = pairs.getKey().toString();
            Object value = pairs.getValue();
            if (value instanceof Map) {
                System.out.println(getTab(tab) + key + " ==> [");
                print((Map<String, Object>) value, tab + 1);
                System.out.println(getTab(tab) + "]");
            }
            else if (value instanceof List) {
                System.out.println(getTab(tab) + key + " ==> [");
                print((List<Object>) value, tab + 1);
                System.out.println(getTab(tab) + "]");
            }
            else {
                System.out.println(getTab(tab) + key + " ==> " + value);
            }
        }
    }

    @SuppressWarnings("unchecked")
	private static void print(List<Object> list, Integer tab) {
        for (Integer index = 0; index < list.size(); index++) {
            Object value = list.get(index);
            if (value instanceof Map) {
                System.out.println(getTab(tab) + index.toString() + ": {");
                print((Map<String, Object>) value, tab + 1);
                System.out.println(getTab(tab) + "}");
            }
            else if (value instanceof List) {
                print((List<Object>) value, tab + 1);
            }
            else {
                System.out.println(getTab(tab) + index.toString() + ": " + value);
            }
        }
    }

    public static String getTab(Integer tab) {
        String string = "";
        for (Integer index = 0; index < tab; index++) {
            string += "    ";
        }
        return string;
    }

}
