package org.genrat.word;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
import org.apache.xmlbeans.XmlException;
import org.genrat.fm.FreeMarkerProcessService;
import org.genrat.tags.TagsFinder;
import org.genrat.tags.model.Tag;
import org.genrat.tags.model.TagType;
import org.genrat.word.model.Element;
import org.genrat.word.model.Run;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;

public class DocProcessor {

	private static final String DELIMITER = UUID.randomUUID().toString();

	private XWPFDocument document;
	private StringBuilder template = new StringBuilder();
	
	private Map<UUID, Map<UUID, Tag>> allMap = new LinkedHashMap<>();
	private Map<UUID, Element> elementMap = new LinkedHashMap<>();
	private Stack<UUID> stack = new Stack<>();
	
	private int inTagCounter = 0;
	

	private TagsFinder tagFinder = new TagsFinder();
	private FreeMarkerProcessService freeMarker = new FreeMarkerProcessService();


	public DocProcessor(XWPFDocument document) {
		this.document = document;
	}

	public XWPFDocument getDocument() {
		return document;
	}

	public void wordDocProcessor() {
		List<XWPFHeader> headers = document.getHeaderList();
		for (XWPFHeader header : headers) {
			processElements(header.getBodyElements());
		}
		List<XWPFFooter> footers = document.getFooterList();
		for (XWPFFooter footer : footers) {
			processElements(footer.getBodyElements());
		}
		processElements(document.getBodyElements());
	}


	private Map<XWPFTable, XWPFTable> tableMap = new LinkedHashMap<>();
	
	public void applyData(Serializable data) throws XmlException, IOException {
		List<String> list = processTemplate(data);
		if (elementMap.isEmpty()) {
			return;
		}
		for (String item : list) {
			UUID elementUuid = getUUID(item.substring(0, DELIMITER.length()));
			String elementText = item.substring(DELIMITER.length());
			Map<UUID, Tag> map = allMap.get(elementUuid);
			if (map == null) {
				continue;
			}
			Element element = elementMap.get(elementUuid);
			Object newElement = null;
			if (element.isParagraph()) {
				XmlCursor cursor = findCursor(element);
				XWPFParagraph xwpfParagraph = element.getParagraph();
				
				XWPFParagraph newParagraph = document.insertNewParagraph(cursor);
				cloneParagraph(newParagraph, xwpfParagraph);
				newElement = newParagraph;
				tableMap.clear();
			} else if (element.isTableRow()) {
				XmlCursor cursor = findCursor(element);
				XWPFTableRow tableRow = element.getTableRow();

				XWPFTable newTable = tableMap.get(tableRow.getTable());
				XWPFTableRow newTableRow = null;
				if (newTable == null) {
					newTable = document.insertNewTbl(cursor);
					newTable.getCTTbl().setTblPr(tableRow.getTable().getCTTbl().getTblPr());
					newTable.getCTTbl().setTblGrid(tableRow.getTable().getCTTbl().getTblGrid());

					tableMap.put(tableRow.getTable(), newTable);
					newTableRow = newTable.getRow(0);
				} else {
					newTableRow = newTable.createRow();
				}
				cloneTableRow(newTableRow, tableRow);
				newElement = newTableRow;
			}
			List<XWPFRun> newRuns = getRuns(newElement);
			for (Entry<UUID, Tag> entry : map.entrySet()) {
				String convertedText = tagFinder.getContent(entry.getKey().toString(), elementText);
				String originalValue = entry.getValue().getContent();
				List<Run> runs = findCurrentRuns(entry.getValue(), elementUuid);
				if (element.isTableRow()) {
					List<XWPFTableCell> cells = element.getTableRow().getTableCells();
					for (XWPFTableCell cell : cells) {
						List<XWPFParagraph> paragraphs = cell.getParagraphs();
						for (XWPFParagraph paragraph : paragraphs) {
							List<XWPFRun> cellRuns = paragraph.getRuns();
							List<Run> currentRuns = filterCurrentRuns(runs, cellRuns);
							replaceTextRun(currentRuns, newRuns, originalValue, convertedText);
						}
					}
				} else if (element.isParagraph()) {
					replaceTextRun(runs, newRuns, originalValue, convertedText);
				}
			}
		}
		List<XWPFTable> removedTables = new ArrayList<>();
		for (Element element : elementMap.values()) {
			if (element.isParagraph()) {
				document.removeBodyElement(document.getPosOfParagraph(element.getParagraph()));
			} else if (element.isTableRow() && !removedTables.contains(element.getTableRow().getTable())) {
				document.removeBodyElement(document.getPosOfTable(element.getTableRow().getTable()));
				removedTables.add(element.getTableRow().getTable());
			}
		}
	}

	private void replaceTextRun(List<Run> runs, List<XWPFRun> newRuns, String originalValue, String convertedText) {
		if (newRuns.isEmpty()) {
			return;
		}
		String runText = "";
		XWPFRun firstXwpfRun = null;
		for (Run run : runs) {
			XWPFRun newXwpfRun = newRuns.get(run.getPosition());
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
	
	private List<Run> filterCurrentRuns(List<Run> runs, List<XWPFRun> cellRuns) {
		List<Run> currentRuns = new ArrayList<>();
		for (Run run : runs) {
			if (cellRuns.contains(run.getRun())) {
				currentRuns.add(run);
			}
		}
		return currentRuns;
	}

	private List<Run> findCurrentRuns(Tag tag, UUID elementUuid) {
		List<Run> list = elementRunList.get(elementUuid);
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

	private XmlCursor findCursor(Element currentElement) {
		UUID groupId = currentElement.getGroupId();
		XmlCursor cursor = null;
		if (groupId == null) {
			if (currentElement.isParagraph()) {
				cursor = currentElement.getParagraph().getCTP().newCursor();			
			} else if (currentElement.isTableRow()) {
				cursor = currentElement.getTableRow().getTable().getCTTbl().newCursor();
			}
			return cursor;
		}
		boolean found = false;
		List<Element> subElements = allElements.subList(allElements.indexOf(currentElement), allElements.size());
		Element lastElement = null;
		for (Element element : subElements) {
			if (currentElement.getGroups().contains(element.getGroupId())) {
				found = true;
			} else if (found) {
				lastElement = element;
				found = false;
			}
		}
		if (lastElement != null) {
			if (lastElement.isParagraph()) {
				cursor = lastElement.getParagraph().getCTP().newCursor();			
			} else if (lastElement.isTableRow()) {
				cursor = lastElement.getTableRow().getTable().getCTTbl().newCursor();
			}
		} else {
			if (currentElement.isParagraph()) {
				cursor = currentElement.getParagraph().getCTP().newCursor();			
			} else if (currentElement.isTableRow()) {
				cursor = currentElement.getTableRow().getTable().getCTTbl().newCursor();
			}
		}
		return cursor;
	}

	private List<String> processTemplate(Serializable data) {
		String result = freeMarker.process(template.toString(), data);
		List<String> list = Arrays.asList(result.split(DELIMITER));
		List<String> newList = new ArrayList<>(); 
		for (int i = 0; i < list.size(); i++) {
			String item = list.get(i);
			newList.add(item);
			UUID elementUuid = getUUID(item.substring(0, DELIMITER.length()));
			Element element = elementMap.get(elementUuid);
			UUID startElementUuid = null;
			if (element.isDirectiveEndTag()) {
				int j = i + 1;
				boolean isLastTag = true;
				while (j < list.size()) {
					String jitem = list.get(j);
					j++;
					UUID jelementUuid = getUUID(jitem.substring(0, DELIMITER.length()));
					if (jelementUuid.equals(elementUuid)) {
						isLastTag = false;
						break;
					}
				}
				if (!isLastTag) {
					int k = j - 1;
					while (k >= 0) {
						String kitem = list.get(k);
						k--;
						UUID kelementUuid = getUUID(kitem.substring(0, DELIMITER.length()));
						Element kelement = elementMap.get(kelementUuid);
						if (kelement.isDirectiveStartTag()) {
							startElementUuid = kelementUuid;
							break;
						}
					}
				}
				if (startElementUuid != null) {
					newList.set(i, item.replace(elementUuid.toString(), startElementUuid.toString()));
				}
			}
		}
		return newList;

	}

	private void processElements(List<IBodyElement> elements) {
		for (IBodyElement element : elements) {
			if (element instanceof XWPFParagraph) {
				processElement(element);
			} else if (element instanceof XWPFTable) {
				XWPFTable table = (XWPFTable) element;
				List<XWPFTableRow> rows = table.getRows();
				for (XWPFTableRow row : rows) {
					processElement(row);
				}
			}
		}
	}
	
	private List<Element> allElements = new ArrayList<>();
	
	private void processElement(Object bodyElement) {
		Map<UUID, Tag> map = new LinkedHashMap<>();
		List<XWPFRun> runs = getRuns(bodyElement);
		List<Run> runList = new ArrayList<>();
		String builtText = "";
		int position = 0;
		for (XWPFRun run : runs) {
			String runText = run.getText(0);
			int start = builtText.length();
			runList.add(new Run(run, start, start + runText.length(), position++));
			builtText += runText;
		}
		UUID elementUuid = UUID.randomUUID();
		elementRunList.put(elementUuid, runList);
		String text = getText(bodyElement);
		String originalText = text;
		String delimiter = "";
		TagType tagType = null;
		if (!template.toString().isEmpty()) {
			delimiter = DELIMITER;
		}
		if (tagFinder.isDirectiveStartTag(text) && tagFinder.isDirectiveEndTag(text)) {
			stack.push(elementUuid);
		} else if (tagFinder.isDirectiveStartTag(text)) {
			inTagCounter++;
			stack.push(elementUuid);
			tagType = TagType.DIRECTIVE_START;
		} else if (tagFinder.isDirectiveEndTag(text)) {
			stack.pop();
			inTagCounter--;
			tagType = TagType.DIRECTIVE_END;
		}
		Element element = new Element(bodyElement, stack.isEmpty() ? null : stack.peek(), tagType, new HashSet<>(stack));
		allElements.add(element);
		if (tagFinder.isTag(text) || inTagCounter > 0) {
			List<Tag> tags = tagFinder.getTagValues(text);
			template.append(delimiter);
			template.append(elementUuid);
			for (int i = 0; i < tags.size(); i++) {
				Tag tag = tags.get(i);
				UUID tagUuid = UUID.randomUUID();
				int startIndex = text.indexOf(tag.getValue());
				int endIndex = text.length();
				if (i < tags.size() - 1) {
					endIndex = text.indexOf(tags.get(i + 1).getValue(), startIndex);
				}
				String tagContent = text.substring(startIndex, endIndex);
				if (tagFinder.isExpressionTag(tagContent) || tagFinder.isDirectiveEndTag(tagContent)) {
					tagContent = tagContent.substring(0,  tag.getValue().length());
				}
				tag.setContent(tagContent);
				map.put(tagUuid, tag);
				template.append(tagUuid + tagContent + tagUuid);			
				text = text.substring(startIndex + tagContent.length());
			}
			allMap.put(elementUuid, map);
			elementMap.put(elementUuid, element);
		}
		if (tagFinder.isDirectiveStartTag(originalText) && tagFinder.isDirectiveEndTag(originalText)) {
			stack.pop();
		}
	}

	private List<XWPFRun> getRuns(Object element) {
		if (element instanceof XWPFParagraph) {
			XWPFParagraph paragraph = (XWPFParagraph) element;
			return paragraph.getRuns();
		}
		if (element instanceof XWPFTableRow) {
			List<XWPFRun> runs = new ArrayList<>();
			XWPFTableRow row = (XWPFTableRow) element;
			List<XWPFTableCell> cells = row.getTableCells();
			for (XWPFTableCell cell : cells) {
				List<IBodyElement> elements = cell.getBodyElements();
				for (IBodyElement subElement : elements) {
					List<XWPFRun> subRuns = getRuns(subElement);
					if (tagFinder.isTag(getText(subElement))) {
						runs.addAll(subRuns);
						
					}
				}
			}
			return runs;
		}
		return null;
	}

	private String getText(Object element) {
		if (element instanceof XWPFParagraph) {
			XWPFParagraph paragraph = (XWPFParagraph) element;
			return paragraph.getText();
		}
		if (element instanceof XWPFTableRow) {
			String text = "";
			XWPFTableRow row = (XWPFTableRow) element;
			List<XWPFTableCell> cells = row.getTableCells();
			for (XWPFTableCell cell : cells) {
				List<IBodyElement> elements = cell.getBodyElements();
				for (IBodyElement subElement : elements) {
					String subText = getText(subElement);
					if (tagFinder.isTag(subText)) {
						text += subText;
					}
				}
			}
			return text;
		}
		return null;
	}
	
	private Map<UUID, List<Run>> elementRunList = new LinkedHashMap<>();
	

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
	
	private void cloneTable(XWPFTable clone, XWPFTable source) {
	    clone.getCTTbl().setTblPr(source.getCTTbl().getTblPr());
	    clone.getCTTbl().setTblGrid(source.getCTTbl().getTblGrid());
	    for (int r = 0; r<source.getRows().size(); r++) {
	        XWPFTableRow targetRow = clone.createRow();
	        XWPFTableRow row = source.getRows().get(r);
	        cloneTableRow(targetRow, row);
	    }
	    //newly created table has one row by default. we need to remove the default row.
	    clone.removeRow(0);
	}

	private void cloneTableRow(XWPFTableRow cloneRow, XWPFTableRow row) {
		cloneRow.getCtRow().setTrPr(row.getCtRow().getTrPr());
		for (int c=0; c<row.getTableCells().size(); c++) {
		    //newly created row has 1 cell
			XWPFTableCell targetCell = null;
			if (cloneRow.getTableCells().size() > c) {
				targetCell = cloneRow.getTableCells().get(c);
			} else {
				targetCell = c==0 ? cloneRow.getTableCells().get(0) : cloneRow.createCell();
			}
		    XWPFTableCell cell = row.getTableCells().get(c);
		    targetCell.getCTTc().setTcPr(cell.getCTTc().getTcPr());
		    XmlCursor cursor = targetCell.getParagraphArray(targetCell.getParagraphs().size() - 1).getCTP().newCursor();
		    for (int p = 0; p < cell.getBodyElements().size(); p++) {
		        IBodyElement elem = cell.getBodyElements().get(p);
		        if (elem instanceof XWPFParagraph) {
		        	XWPFParagraph targetPar = null;
		        	if (p == cell.getBodyElements().size() - 1) {
		        		targetPar = targetCell.getParagraphArray(p);
		        	} else {
						targetPar = targetCell.insertNewParagraph(cursor);
						cursor.toNextToken();
					}
		            XWPFParagraph par = (XWPFParagraph) elem;
		            cloneParagraph(targetPar, par);
		        } else if (elem instanceof XWPFTable) {
		        	//newly created cell has one default paragraph we need to remove
		        	targetCell.removeParagraph(0);
		            XWPFTable targetTable = targetCell.insertNewTbl(cursor);
		            XWPFTable table = (XWPFTable) elem;
		            cloneTable(targetTable, table);
		            cursor.toNextToken();
		        }
		    }
		    //newly created cell has one default paragraph we need to remove
		    //targetCell.removeParagraph(targetCell.getParagraphs().size()-1);
		}
	}
}
