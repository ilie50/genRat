package org.genrat.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class Main {

//	public static final String DOCX_INPUT_PATH = "C:\\tmp\\gr\\RemoteDebugginSetup.docx";
	public static final String DOCX_INPUT_PATH = "C:\\Users\\ibratilescu\\Documents\\TestDocument.docx";
	public static final String PDF_OUTPUT_PATH = "C:\\tmp\\gr\\pdf\\";

	public static void main(String[] args) {
		GeneratorService gs = new GeneratorService();
		gs.createPdf(DOCX_INPUT_PATH, PDF_OUTPUT_PATH);
		//main.updateWorkingDoc(DOCX_INPUT_PATH, PDF_OUTPUT_PATH, xml);
		//main.readDoc(fileInputPath);
	}

	private boolean isDocxFile(String filePath) {
		return filePath.substring(filePath.length() - 1).equals("x");
	}

	public String readDoc(String fileInputPath) {
		String text = "";
		try (FileInputStream fis = new FileInputStream(new File(fileInputPath))) {
			if (isDocxFile(fileInputPath)) {
				text = getDocxContent(fis);
			} else {
				text = getDocContent(fis);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return text;
	}

	private String getDocContent(FileInputStream fis) {
		String text = "";

		try (HWPFDocument doc = new HWPFDocument(fis); WordExtractor extractor = new WordExtractor(doc)) {
			text = extractor.getText();
			System.out.println(text);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return text;
	}

	private String getDocxContent(FileInputStream fis) {
		String text = "";
		try (XWPFDocument doc = new XWPFDocument(fis); XWPFWordExtractor extract = new XWPFWordExtractor(doc)) {
			text = extract.getText();
			System.out.println(text);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return text;
	}

}
