package org.genrat.word;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlCursor;
import org.genrat.core.TagsFinder;
import org.genrat.fm.FreeMarkerGenrateXMLFromObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;

public class DocProcessor {

	private static final String DELIMITER = UUID.randomUUID().toString();

	private XWPFDocument document;
	private StringBuilder template = new StringBuilder();
	
	private Map<UUID, Map<UUID, String>> allMap = new LinkedHashMap<>();
	private Map<UUID, Paragraph> paragraphMap = new LinkedHashMap<>();
	private Stack<UUID> stack = new Stack<>();
	
	private int inTagCounter = 0;
	

	private TagsFinder tagFinder = new TagsFinder();
	private FreeMarkerGenrateXMLFromObject freeMarker = new FreeMarkerGenrateXMLFromObject();


	public DocProcessor(XWPFDocument document) {
		this.document = document;
	}

	public XWPFDocument getDocument() {
		return document;
	}

	public void wordDocProcessor() {
		List<XWPFHeader> headers = document.getHeaderList();
		for (XWPFHeader header : headers) {
			processBodyElements(header.getBodyElements());
		}
		List<XWPFFooter> footers = document.getFooterList();
		for (XWPFFooter footer : footers) {
			processBodyElements(footer.getBodyElements());
		}
		processBodyElements(document.getBodyElements());
	}


	public void applyData() {
		List<String> list = processTemplate();
		int uuidLength = UUID.randomUUID().toString().length();
		if (paragraphMap.isEmpty()) {
			return;
		}
		int posOfParagraph = 0;
		for (String item : list) {
			UUID paragraphUuid = getUUID(item.substring(0, uuidLength));
			String paragraphText = item.substring(uuidLength);
			Map<UUID, String> map = allMap.get(paragraphUuid);
			if (map == null) {
				continue;
			}
			Paragraph paragraph = paragraphMap.get(paragraphUuid);
			
			XmlCursor cursor = findCursor(paragraphUuid, paragraph.getGroupId(), list, item, uuidLength, posOfParagraph);
			XWPFParagraph newParagraph = new DocParagraph().createParagraph(document, cursor);
			posOfParagraph = document.getPosOfParagraph(newParagraph);
			XWPFParagraph xwpfParagraph = paragraph.getParagraph();
			cloneParagraph(newParagraph, xwpfParagraph);
			
			int indexDiff = 0;
			for (Entry<UUID, String> entry : map.entrySet()) {
				String convertedText = tagFinder.getContent(entry.getKey().toString(), paragraphText);
				String originalValue = entry.getValue();
				Map<UUID, XWPFRun> runMap = paragraphRunMap.get(paragraphUuid);
				List<XWPFRun> runs = new ArrayList<>();
				for (Entry<UUID, XWPFRun> runEntry : runMap.entrySet()) {
					if (originalValue.contains(runEntry.getKey().toString())) {
						runs.add(runEntry.getValue());
					}
				}
				if (!runs.isEmpty()) {
					List<XWPFRun> paragraphRuns = xwpfParagraph.getRuns();
					boolean alreadyInserted = false;
					int index = 0; 
					int i = 0;
					for (i = 0; i < paragraphRuns.size(); i++) {
						XWPFRun run = paragraphRuns.get(i);
						if (runs.contains(run)) {
							if (!alreadyInserted) {
								index -= indexDiff;
							}
							newParagraph.removeRun(index);
							index--;
							if (!alreadyInserted) {
								index++;
								XWPFRun nr = null;
								if (index >= newParagraph.getRuns().size()) {
									nr = newParagraph.createRun();
								} else {
									nr = newParagraph.insertNewRun(index);
									
								}
								cloneRun(nr, runs.get(0), convertedText);
								alreadyInserted = true;
							}
						}
						
						index++;
					}
					indexDiff = i - index;
				}
			}
		}
		for (Paragraph paragraph : paragraphMap.values()) {
			document.removeBodyElement(document.getPosOfParagraph(paragraph.getParagraph()));
		}
	}

	private XmlCursor findCursor(UUID paragraphUuid, UUID groupId, List<String> list, String item, int uuidLength, int posOfParagraph) {
		if (groupId == null) {
			return paragraphMap.get(paragraphUuid).getParagraph().getCTP().newCursor();			
		}
		boolean found = false;
		Paragraph nextParagraph = null;
		for (Paragraph paragraph : paragraphMap.values()) {
			if (groupId.equals(paragraph.getGroupId())) {
				found = true;
			} else if (found) {
				nextParagraph = paragraph;
			}
		}
		return nextParagraph.getParagraph().getCTP().newCursor();
	}

	private List<String> processTemplate() {
		String result = freeMarker.process(template.toString());
		List<String> list = Arrays.asList(result.split(DELIMITER));
		return list;

	}

	private void processBodyElements(List<IBodyElement> bodyElements) {
		for (IBodyElement element : bodyElements) {
			if (element instanceof XWPFParagraph) {
				XWPFParagraph paragraph = (XWPFParagraph) element;
				processParagraph(paragraph);
			}
			if (element instanceof XWPFTable) {
				XWPFTable table = (XWPFTable) element;
				List<XWPFTableRow> rows = table.getRows();
				for (XWPFTableRow row : rows) {
					List<XWPFTableCell> cells = row.getTableCells();
					for (XWPFTableCell cell : cells) {
						processBodyElements(cell.getBodyElements());
					}
				}
			}
		}
	}
	private Map<UUID, Map<UUID, XWPFRun>> paragraphRunMap = new LinkedHashMap<>();
	
	private void processParagraph(XWPFParagraph paragraph) {
		Map<UUID, String> map = new LinkedHashMap<>();
		List<XWPFRun> runs = paragraph.getRuns();
		StringBuilder sbRuns = new StringBuilder();
		Map<UUID, XWPFRun> runMap = new LinkedHashMap<>();
		for (XWPFRun run : runs) {
			UUID uuid = UUID.randomUUID();
			runMap.put(uuid, run);
			sbRuns.append(run.getText(0)).append(uuid.toString());
		}
		UUID paragraphUuid = UUID.randomUUID();
		paragraphRunMap.put(paragraphUuid, runMap);
		String text = sbRuns.toString();//paragraph.getText();
		String delimiter = "";
		if (!template.toString().isEmpty()) {
			delimiter = DELIMITER;
		}
		if (tagFinder.isDirectiveStartTag(text) && tagFinder.isDirectiveEndTag(text)) {
			stack.push(paragraphUuid);
		} else if (tagFinder.isDirectiveStartTag(text)/* && !tagFinder.isDirectiveClauseTag(text)*/) {
			inTagCounter++;
			stack.push(paragraphUuid);
		} else if (tagFinder.isDirectiveEndTag(text)) {
			stack.pop();
			inTagCounter--;
		}
		if (tagFinder.isTag(text) || inTagCounter > 0) {
			List<String> tags = tagFinder.getTagValues(text);
			template.append(delimiter);
			template.append(paragraphUuid);
			for (int i = 0; i < tags.size(); i++) {
				String tag = tags.get(i);
				UUID tagUuid = UUID.randomUUID();
				int startIndex = text.indexOf(tag);
				int endIndex = text.length();
				if (i < tags.size() - 1) {
					endIndex = text.indexOf(tags.get(i + 1), startIndex);
				}
				String tagContent = text.substring(startIndex, endIndex);
				map.put(tagUuid, tagContent);
				String cleanTagContent = removeRunUuidFromTagContent(tagContent, runMap.keySet());
				template.append(tagUuid + cleanTagContent + tagUuid);			
				text = text.substring(startIndex + tagContent.length());
			}
			allMap.put(paragraphUuid, map);
			paragraphMap.put(paragraphUuid, new Paragraph(paragraph, stack.isEmpty() ? null : stack.peek()));
		}
		if (tagFinder.isDirectiveStartTag(text) && tagFinder.isDirectiveEndTag(text)) {
			stack.pop();
		}
	}

	private String removeRunUuidFromTagContent(String tagContent, Set<UUID> uuids) {
		for (UUID uuid : uuids) {
			tagContent = tagContent.replace(uuid.toString(), "");
		}
		return tagContent;
	}

	private UUID getUUID(String sUuid) {
		try {
			return UUID.fromString(sUuid);
		} catch (IllegalArgumentException e) {
			return null;
		}

	}

	private void cloneParagraph(XWPFParagraph clone, XWPFParagraph source) {
		CTPPr pPr = clone.getCTP().isSetPPr() ? clone.getCTP().getPPr() : clone.getCTP().addNewPPr();
		pPr.set(source.getCTP().getPPr());
		for (XWPFRun r : source.getRuns()) {
			XWPFRun nr = clone.createRun();
			cloneRun(nr, r);
		}
	}

	private void cloneRun(XWPFRun clone, XWPFRun source) {
		cloneRun(clone, source, source.getText(0));
	}

	private void cloneRun(XWPFRun clone, XWPFRun source, String text) {
		CTRPr rPr = clone.getCTR().isSetRPr() ? clone.getCTR().getRPr() : clone.getCTR().addNewRPr();
		rPr.set(source.getCTR().getRPr());
		clone.setText(text);
	}
}
