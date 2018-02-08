package org.genrat.converter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.math.NumberUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Pritom K Mondal
 * @published 12th September 2013 08:04 PM
 */
public class XmlParser {
    private String xmlString = "";
    private File xmlFile = null;
    private Document doc = null;

    
    public XmlParser(String xmlString) {
        this.xmlString = xmlString;
    }
    
    public XmlParser(File xmlFile) {
        this.xmlFile = xmlFile;
    }

    public Map<String, Object> parseXML() {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            if (this.xmlFile != null) {
                doc = dBuilder.parse(this.xmlFile);
            }
            else {
                doc = dBuilder.parse( new ByteArrayInputStream(xmlString.getBytes()) );
            }

            doc.getDocumentElement().normalize();

            NodeList resultNode = doc.getChildNodes();

            Map<String, Object> result = new HashMap<>();
            XmlParser.MyNodeList tempNodeList = new XmlParser.MyNodeList();

            String emptyNodeName = null, emptyNodeValue = null;

            for(int index = 0; index < resultNode.getLength(); index ++) {
                Node tempNode = resultNode.item(index);
                if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                    tempNodeList.addNode(tempNode);
                }
                emptyNodeName = tempNode.getNodeName();
                emptyNodeValue = tempNode.getNodeValue();
            }

            if (tempNodeList.getLength() == 0 && emptyNodeName != null
                    && emptyNodeValue != null) {
                result.put(emptyNodeName, emptyNodeValue);
                return result;
            }

            this.parseXMLNode(tempNodeList, result);
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void parseXMLNode(NodeList nList, Map<String, Object> result) {
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE
                    && nNode.hasChildNodes()
                    && nNode.getFirstChild() != null
                    && (nNode.getFirstChild().getNextSibling() != null
                    || nNode.getFirstChild().hasChildNodes())) {
                NodeList childNodes = nNode.getChildNodes();
                XmlParser.MyNodeList tempNodeList = new XmlParser.MyNodeList();
                for(int index = 0; index < childNodes.getLength(); index ++) {
                    Node tempNode = childNodes.item(index);
                    if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                        tempNodeList.addNode(tempNode);
                    }
                }
                Map<String, Object> dataHashMap = new HashMap<>();
                if (result.containsKey(nNode.getNodeName()) && result.get(nNode.getNodeName()) instanceof List) {
                    @SuppressWarnings("unchecked")
					List<Object> mapExisting = (List<Object>) result.get(nNode.getNodeName());
                    mapExisting.add(dataHashMap);
                } else if(result.containsKey(nNode.getNodeName())) {
                    List<Object> counterList = new ArrayList<>();
                    counterList.add(result.get(nNode.getNodeName()));
                    counterList.add(dataHashMap);
                    result.put(nNode.getNodeName(), counterList);
                } else {
                    result.put(nNode.getNodeName(), dataHashMap);
                }
                if (nNode.getAttributes().getLength() > 0) {
                    Map<String, Object> attributeMap = new HashMap<>();
                    for(int attributeCounter = 0;
                        attributeCounter < nNode.getAttributes().getLength();
                        attributeCounter++) {
                        attributeMap.put(
                                nNode.getAttributes().item(attributeCounter).getNodeName(),
                                nNode.getAttributes().item(attributeCounter).getNodeValue()
                        );
                    }
                    dataHashMap.put("<<attributes>>", attributeMap);
                }
                this.parseXMLNode(tempNodeList, dataHashMap);
            } else if (nNode.getNodeType() == Node.ELEMENT_NODE
                    && nNode.hasChildNodes() && nNode.getFirstChild() != null
                    && nNode.getFirstChild().getNextSibling() == null) {
                this.putValue(result, nNode);
            } else if(nNode.getNodeType() == Node.ELEMENT_NODE) {
                this.putValue(result, nNode);
            }
        }
    }

    private void putValue(Map<String, Object> result, Node nNode) {
        Map<String, Object> attributeMap = new HashMap<>();
        Object nodeValue = null;
        if(nNode.getFirstChild() != null) {
            nodeValue = nNode.getFirstChild().getNodeValue();
            if(nodeValue != null) {
                nodeValue = nodeValue.toString().trim();
            	if (NumberUtils.isDigits((String) nodeValue)) {
					nodeValue = Integer.valueOf((String) nodeValue);
				}
            }
        }
        Map<String, Object> nodeMap = new HashMap<>();
        nodeMap.put("<<value>>", nodeValue);
        Object putNode = nodeValue;
        if (nNode.getAttributes().getLength() > 0) {
            for(int attributeCounter = 0;
                attributeCounter < nNode.getAttributes().getLength();
                attributeCounter++) {
                attributeMap.put(
                        nNode.getAttributes().item(attributeCounter).getNodeName(),
                        nNode.getAttributes().item(attributeCounter).getNodeValue()
                );
            }
            nodeMap.put("<<attributes>>", attributeMap);
            putNode = nodeMap;
        }
        if (result.containsKey(nNode.getNodeName()) && result.get(nNode.getNodeName()) instanceof List) {
            @SuppressWarnings("unchecked")
			List<Object> mapExisting = (List<Object>) result.get(nNode.getNodeName());
            mapExisting.add(putNode);
        } else if(result.containsKey(nNode.getNodeName())) {
            List<Object> counterList = new ArrayList<Object>();
            counterList.add(result.get(nNode.getNodeName()));
            counterList.add(putNode);
            result.put(nNode.getNodeName(), counterList);
        } else {
            result.put(nNode.getNodeName(), putNode);
        }
    }

    class MyNodeList implements NodeList {
        List<Node> nodes = new ArrayList<Node>();
        int length = 0;
        public MyNodeList() {}

        public void addNode(Node node) {
            nodes.add(node);
            length++;
        }

        @Override
        public Node item(int index) {
            try {
                return nodes.get(index);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        public int getLength() {
            return length;
        }
    }
    
}