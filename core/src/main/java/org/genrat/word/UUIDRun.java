package org.genrat.word;

import java.util.UUID;

import org.apache.poi.xwpf.usermodel.XWPFRun;

public class UUIDRun {

	private static final String LIST_START_TAG = "<#list";
	private static final String LIST_END_TAG = "</#list";
	
	private UUID uuid;
	private XWPFRun run;
	private String text;
	private boolean inTag;

	public UUIDRun(UUID uuid, XWPFRun run, String text, boolean inTag) {
		this.uuid = uuid;
		this.run = run;
		this.text = text;
		this.inTag = inTag;
	}

	public UUID getUuid() {
		return uuid;
	}

	public XWPFRun getRun() {
		return run;
	}

	public String getText() {
		return text;
	}

	public boolean isListStart() {
		return text.indexOf(LIST_START_TAG) >= 0;
	}

	public boolean isListEnd() {
		return text.indexOf(LIST_END_TAG) >= 0;
	}

	public boolean isInTag() {
		return inTag;
	}

}
