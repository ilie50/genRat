package org.genrat.word;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

	private XWPFDocument document;
	
	private TagsFinder tagFinder = new TagsFinder();
	private FreeMarkerGenrateXMLFromObject freeMarker = new FreeMarkerGenrateXMLFromObject();

	private Map<UUID, Paragraph> paragraphMap = new LinkedHashMap<>();

	public DocProcessor(XWPFDocument document) {
		this.document = document;
	}

	public XWPFDocument getDocument() {
		return document;
	}

	public void applyData(List<String> list) {
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
			cloneParagraph(newParagraph, paragraph.getParagraph());
			for (Entry<UUID, String> entry : map.entrySet()) {
				String convertedText = tagFinder.getContent(entry.getKey().toString(), paragraphText);
				String originalValue = entry.getValue();
				List<XWPFRun> runs = newParagraph.getRuns();
				String runText = "";
				List<XWPFRun> runsToClear = new ArrayList<>();
				for (XWPFRun run : runs) {
					String text = run.getText(0);
					if (text != null) {
						if (originalValue.contains(text)) {
							runText += text;
							runsToClear.add(0, run);
						}
						if (text.contains(originalValue)) {
							//TODO: fix else tag
							text = text.replace(originalValue, convertedText);
							run.setText(text, 0);
							runsToClear.clear();
							break;
						} else if (runText.contains(originalValue)) {
							run.setText(convertedText, 0);							
						}
					}
				}
				if (!runsToClear.isEmpty()) {
					runsToClear.remove(0);
					for (XWPFRun run : runsToClear) {
						run.setText("", 0);
					}
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

	private String DELIMITER = UUID.randomUUID().toString();

	public List<String> processTemplate() {
		String result = freeMarker.process(template.toString());
		List<String> list = Arrays.asList(result.split(DELIMITER));
		return list;

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
	
	private StringBuilder template = new StringBuilder();
	private Map<UUID, Map<UUID, String>> allMap = new LinkedHashMap<>();
	int inTagCounter = 0;
	private Stack<UUID> stack = new Stack<>();
	private void processParagraph(XWPFParagraph paragraph) {
		Map<UUID, String> map = new LinkedHashMap<>();
		String text = paragraph.getText();
		String delimiter = "";
		if (!template.toString().isEmpty()) {
			delimiter = DELIMITER;
		}
		UUID paragraphUuid = UUID.randomUUID();
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
			for (String tag : tags) {
				UUID tagUuid = UUID.randomUUID();
				map.put(tagUuid, tag);
				template.append(tagUuid + tag + tagUuid);
			}
			allMap.put(paragraphUuid, map);
			paragraphMap.put(paragraphUuid, new Paragraph(paragraph, stack.isEmpty() ? null : stack.peek()));
		}
		if (tagFinder.isDirectiveStartTag(text) && tagFinder.isDirectiveEndTag(text)) {
			stack.pop();
		}
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
		CTRPr rPr = clone.getCTR().isSetRPr() ? clone.getCTR().getRPr() : clone.getCTR().addNewRPr();
		rPr.set(source.getCTR().getRPr());
		clone.setText(source.getText(0));
	}
}
