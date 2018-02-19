package org.genrat.word.model;

import java.io.Serializable;
import java.util.UUID;

import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;

public class Element implements Serializable {

	private static final long serialVersionUID = -3312870307384700467L;
	
	private UUID groupId;
	private IBodyElement element;
	
	public Element(IBodyElement element, UUID groupId) {
		this.element = element;
		this.groupId = groupId;
	}

	public UUID getGroupId() {
		return groupId;
	}

	public void setGroupId(UUID groupId) {
		this.groupId = groupId;
	}

	public IBodyElement getElement() {
		return element;
	}

	public void setElement(IBodyElement element) {
		this.element = element;
	}
	public XWPFParagraph getParagraph() {
		if (element instanceof XWPFParagraph) {
			return (XWPFParagraph) element;
			
		}
		return null;
	}
	
	public XWPFTable getTable() {
		if (element instanceof XWPFTable) {
			return (XWPFTable) element;
			
		}
		return null;
	}

	public boolean isParagraph() {
		return getParagraph() != null;
	}

	public boolean isTable() {
		return getTable() != null;
	}

	@Override
	public String toString() {
		return "Element [groupId=" + groupId + ", element=" + element + "]";
	}
}
