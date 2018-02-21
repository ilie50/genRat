package org.genrat.word.model;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.genrat.tags.model.TagType;

public class Element implements Serializable {

	private static final long serialVersionUID = -3312870307384700467L;
	
	private UUID groupId;
	private Object element;
	private TagType tagType;
	private Set<UUID> groups;
	
	public Element(Object element, UUID groupId, TagType tagType, Set<UUID> groups) {
		this.element = element;
		this.groupId = groupId;
		this.tagType = tagType;
		this.groups = groups;
	}

	public UUID getGroupId() {
		return groupId;
	}

	public void setGroupId(UUID groupId) {
		this.groupId = groupId;
	}

	public Object getElement() {
		return element;
	}

	public void setElement(Object element) {
		this.element = element;
	}
	
	public Set<UUID> getGroups() {
		return groups;
	}

	public void setGroups(Set<UUID> groups) {
		this.groups = groups;
	}

	public XWPFParagraph getParagraph() {
		if (element instanceof XWPFParagraph) {
			return (XWPFParagraph) element;
			
		}
		return null;
	}
	
	public XWPFTableRow getTableRow() {
		if (element instanceof XWPFTableRow) {
			return (XWPFTableRow) element;
			
		}
		return null;
	}

	public boolean isDirectiveStartTag() {
		return TagType.DIRECTIVE_START.equals(tagType);
	}
	
	public boolean isDirectiveEndTag() {
		return TagType.DIRECTIVE_END.equals(tagType);
	}
	
	public boolean isExpressionTag() {
		return TagType.EXPRESSION.equals(tagType);
	}

	public boolean isDirectiveClauseTag() {
		return TagType.DIRECTIVE_CLAUSE.equals(tagType);
	}
	
	public boolean isParagraph() {
		return getParagraph() != null;
	}

	public boolean isTableRow() {
		return getTableRow() != null;
	}

	@Override
	public String toString() {
		return "Element [groupId=" + groupId + ", element=" + element + ", tagType=" + tagType + ", groups="
				+ groups + "]";
	}
}
