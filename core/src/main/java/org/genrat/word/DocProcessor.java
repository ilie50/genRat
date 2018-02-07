package org.genrat.word;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import org.genrat.tags.Tag;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;

public class DocProcessor {

	private static final String DELIMITER = UUID.randomUUID().toString();

	private XWPFDocument document;
	private StringBuilder template = new StringBuilder();
	
	private Map<UUID, Map<UUID, Tag>> allMap = new LinkedHashMap<>();
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
			Map<UUID, Tag> map = allMap.get(paragraphUuid);
			if (map == null) {
				continue;
			}
			Paragraph paragraph = paragraphMap.get(paragraphUuid);
			
			XmlCursor cursor = findCursor(paragraphUuid, paragraph.getGroupId(), list, item, uuidLength, posOfParagraph);
			XWPFParagraph newParagraph = new DocParagraph().createParagraph(document, cursor);
			posOfParagraph = document.getPosOfParagraph(newParagraph);
			XWPFParagraph xwpfParagraph = paragraph.getParagraph();
			cloneParagraph(newParagraph, xwpfParagraph);
			for (Entry<UUID, Tag> entry : map.entrySet()) {
				String convertedText = tagFinder.getContent(entry.getKey().toString(), paragraphText);
				String originalValue = entry.getValue().getContent();
				List<Run> runs = findCurrentRuns(entry.getValue(), paragraphUuid);
				String runText = "";
				XWPFRun firstXwpfRun = null;
				for (Run run : runs) {
					XWPFRun newXwpfRun = newParagraph.getRuns().get(run.getPosition());
					runText += newXwpfRun.getText(0);
					if (firstXwpfRun == null) {
						firstXwpfRun = newXwpfRun;
					}
					newXwpfRun.setText("", 0);
				}
				if (firstXwpfRun != null) {
					firstXwpfRun.setText(runText.replace(originalValue, convertedText), 0);
				}
			}
		}
		for (Paragraph paragraph : paragraphMap.values()) {
			document.removeBodyElement(document.getPosOfParagraph(paragraph.getParagraph()));
		}
	}

	private List<Run> findCurrentRuns(Tag tag, UUID paragraphUuid) {
		List<Run> list = paragraphRunList.get(paragraphUuid);
		Set<Run> result = new LinkedHashSet<>();
		if (list == null) {
			return new ArrayList<>(result);
		}
		int tagStart = tag.getStart();
		int tagEnd = tagStart + tag.getContent().length();
		for (Run run : list) {
			int runStart = run.getStart();
			int runEnd = run.getEnd();
			if (runStart >= tagStart && runEnd <= tagEnd) {
				result.add(run);
			} else if (runStart <= tagStart && runEnd <= tagEnd && runEnd > tagStart) {
				result.add(run);
			} else if (tagStart <= runStart && tagEnd <= runEnd && tagEnd > runStart) {
				result.add(run);
			} else if (runStart <= tagStart && tagEnd <= runEnd) {
				result.add(run);
			}
		}
		return new ArrayList<>(result);
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
	private Map<UUID, List<Run>> paragraphRunList = new LinkedHashMap<>();
	
	private void processParagraph(XWPFParagraph paragraph) {
		Map<UUID, Tag> map = new LinkedHashMap<>();
		List<XWPFRun> runs = paragraph.getRuns();
		List<Run> runList = new ArrayList<>();
		String builtText = "";
		int position = 0;
		for (XWPFRun run : runs) {
			
			String runText = run.getText(0);
			int start = builtText.length();
			runList.add(new Run(run, start, start + runText.length(), position++));
			builtText += runText;
		}
		UUID paragraphUuid = UUID.randomUUID();
		paragraphRunList.put(paragraphUuid, runList);
		String text = /*sbRuns.toString();*/paragraph.getText();
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
			List<Tag> tags = tagFinder.getTagValues(text);
			template.append(delimiter);
			template.append(paragraphUuid);
			for (int i = 0; i < tags.size(); i++) {
				Tag tag = tags.get(i);
				UUID tagUuid = UUID.randomUUID();
				int startIndex = text.indexOf(tag.getValue());
				int endIndex = text.length();
				if (i < tags.size() - 1) {
					endIndex = text.indexOf(tags.get(i + 1).getValue(), startIndex);
				}
				String tagContent = text.substring(startIndex, endIndex);
				tag.setContent(tagContent);
				map.put(tagUuid, tag);
				template.append(tagUuid + tagContent + tagUuid);			
				text = text.substring(startIndex + tagContent.length());
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
		cloneRun(clone, source, source.getText(0));
	}

	private void cloneRun(XWPFRun clone, XWPFRun source, String text) {
		CTRPr rPr = clone.getCTR().isSetRPr() ? clone.getCTR().getRPr() : clone.getCTR().addNewRPr();
		rPr.set(source.getCTR().getRPr());
		clone.setText(text);
	}
}
