package org.genrat.word.model;

import java.io.Serializable;

import org.apache.poi.xwpf.usermodel.XWPFRun;

public class Run implements Serializable {

	private static final long serialVersionUID = -6257847490492368442L;

	private int start;
	private int end;
	private XWPFRun run;
	private int position;
	
	public Run(XWPFRun run, int start, int end, int position) {
		this.run = run;
		this.start = start;
		this.end = end;
		this.position = position;
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
	public XWPFRun getRun() {
		return run;
	}
	public void setRun(XWPFRun run) {
		this.run = run;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

}
