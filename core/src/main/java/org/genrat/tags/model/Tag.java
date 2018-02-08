package org.genrat.tags.model;

import java.io.Serializable;

public class Tag implements Serializable {

	private static final long serialVersionUID = 3785847182600935401L;

	private int start;
	private int end;
	private String value;
	private String content;
	
	public Tag(String value, int start, int end) {
		this.value = value;
		this.start = start;
		this.end = end;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
