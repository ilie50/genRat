package org.genrat.word;

import java.util.Map;
import java.util.UUID;

import org.apache.poi.xwpf.usermodel.XWPFRun;

public class UUIDText {

	private UUID uuid;
	private String text;
	private XWPFRun run;
	private int index;
	private boolean listStart;
	private boolean listEnd;
	
	public UUIDText(String s, Map<UUID, UUIDRun> content) {
		int length = UUID.randomUUID().toString().length();
		uuid = UUID.fromString(s.substring(0, length));
		text = s.substring(length + 1);
		UUIDRun uuidRun = content.get(uuid);
		listStart = uuidRun.isListStart();
		listEnd = uuidRun.isListEnd();
		run = uuidRun.getRun();
	}

	public UUID getUuid() {
		return uuid;
	}

	public String getText() {
		return text;
	}

	public XWPFRun getRun() {
		return run;
	}
	
	public boolean isListStart() {
		return listStart;
	}

	public boolean isListEnd() {
		return listEnd;
	}

}
