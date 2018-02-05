package org.genrat.word;

import java.util.UUID;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public class Paragraph {

	private XWPFParagraph paragraph;
	private UUID groupId;
	
	public Paragraph(XWPFParagraph paragraph, UUID groupId) {
		this.paragraph = paragraph;
		this.groupId = groupId;
	}

	public XWPFParagraph getParagraph() {
		return paragraph;
	}

	public void setParagraph(XWPFParagraph paragraph) {
		this.paragraph = paragraph;
	}

	public UUID getGroupId() {
		return groupId;
	}

	public void setGroupId(UUID groupId) {
		this.groupId = groupId;
	}
	
	
}
