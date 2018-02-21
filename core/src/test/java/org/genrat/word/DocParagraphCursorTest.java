package org.genrat.word;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;

public class DocParagraphCursorTest {

	public static final String DOCX_INPUT_PATH = "C:\\tmp\\gr\\a.docx";
	public static final String DOCX_OUTPUT_PATH = "C:\\tmp\\gr\\b.docx";

	public static void main(String[] args) throws Exception {

		Files.copy(new File(DOCX_INPUT_PATH).toPath(), new File(DOCX_INPUT_PATH + "_temp").toPath(),
				StandardCopyOption.REPLACE_EXISTING);

		try (XWPFDocument document = new DocParagraphTest().loadDocxIntoXWPFDocument(DOCX_INPUT_PATH + "_temp")) {
			// Write the Document in file system
			FileOutputStream out = new FileOutputStream(new File(DOCX_OUTPUT_PATH));

			XWPFParagraph source = document.getParagraphArray(0);
			XWPFParagraph clone = document.insertNewParagraph(source.getCTP().newCursor());
			CTPPr pPr = clone.getCTP().isSetPPr() ? clone.getCTP().getPPr() : clone.getCTP().addNewPPr();
			pPr.set(source.getCTP().getPPr());
			for (XWPFRun r : source.getRuns()) {
				XWPFRun nr = clone.createRun();
				cloneRun(nr, r);
			}
			clone.getRuns().get(0).setText("Before 1", 0);
			

			XWPFParagraph clone2 = document.insertNewParagraph(source.getCTP().newCursor());
			CTPPr pPr2 = clone2.getCTP().isSetPPr() ? clone2.getCTP().getPPr() : clone2.getCTP().addNewPPr();
			pPr2.set(source.getCTP().getPPr());
			for (XWPFRun r2 : source.getRuns()) {
				XWPFRun nr2 = clone2.createRun();
				cloneRun(nr2, r2);
			}
			clone2.getRuns().get(0).setText("Before 2", 0);

			
			document.write(out);
			out.close();
			System.out.println(DOCX_OUTPUT_PATH + " written successfully");

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Files.delete(new File(DOCX_INPUT_PATH + "_temp").toPath());
	}

	private static void cloneRun(XWPFRun clone, XWPFRun source) {
		cloneRun(clone, source, source.getText(0));
	}

	private static void cloneRun(XWPFRun clone, XWPFRun source, String text) {
		CTRPr rPr = clone.getCTR().isSetRPr() ? clone.getCTR().getRPr() : clone.getCTR().addNewRPr();
		rPr.set(source.getCTR().getRPr());
		clone.setText(text);
	}

}
