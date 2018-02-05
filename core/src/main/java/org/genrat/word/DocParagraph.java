package org.genrat.word;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.xmlbeans.XmlCursor;
import org.genrat.core.Main;

public class DocParagraph {

	public static final String DOCX_OUTPUT_PATH = "C:\\tmp\\gr\\RemoteDebugginSetup_copy.docx";

	public static void main(String[] args) throws Exception {

		Files.copy(new File(Main.DOCX_INPUT_PATH).toPath(), new File(Main.DOCX_INPUT_PATH + "_temp").toPath(),
				StandardCopyOption.REPLACE_EXISTING);
		// Blank Document
		try (XWPFDocument document = new Main().loadDocxIntoXWPFDocument(Main.DOCX_INPUT_PATH + "_temp")) {
			// Write the Document in file system
			FileOutputStream out = new FileOutputStream(new File(DOCX_OUTPUT_PATH));
			// FileOutputStream out = new FileOutputStream(new File(Main.DOCX_INPUT_PATH +
			// "_temp"));

			String[] paragraphs = new String[] { "ABCD", "EFGH", "IJKL" };
			new DocParagraph().createParagraphs(document.getParagraphs().iterator().next(), paragraphs);
			// create Paragraph
			/*
			 * XWPFParagraph paragraph = document.createParagraph(); XWPFRun run =
			 * paragraph.createRun();
			 * run.setText("At tutorialspoint.com, we strive hard to " +
			 * "provide quality tutorials for self-learning " +
			 * "purpose in the domains of Academics, Information " +
			 * "Technology, Management and Computer Programming Languages.");
			 */

			document.write(out);
			out.close();
			System.out.println("createparagraph.docx written successfully");

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Files.delete(new File(Main.DOCX_INPUT_PATH + "_temp").toPath());
	}

	public void createParagraph(XWPFDocument document, String uuid, String newText) {
		List<XWPFParagraph> ps = document.getParagraphs().stream().filter(p -> p.getText().contains(uuid))
				.collect(Collectors.toList());
		new DocParagraph().createParagraphs(ps.iterator().next(), newText);
	}

	public void createParagraph(String workingFilePath, String uuid, String newText) {
		try {
			Files.copy(new File(workingFilePath).toPath(), new File(Main.DOCX_INPUT_PATH + "_temp").toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			// Blank Document
			try (XWPFDocument document = new Main().loadDocxIntoXWPFDocument(Main.DOCX_INPUT_PATH + "_temp")) {
				// Write the Document in file system
				FileOutputStream out = new FileOutputStream(new File(DOCX_OUTPUT_PATH));

				List<XWPFParagraph> ps = document.getParagraphs().stream().filter(p -> p.getText().contains(uuid))
						.collect(Collectors.toList());
				new DocParagraph().createParagraphs(ps.iterator().next(), newText);
				// create Paragraph
				/*
				 * XWPFParagraph paragraph = document.createParagraph(); XWPFRun run =
				 * paragraph.createRun();
				 * run.setText("At tutorialspoint.com, we strive hard to " +
				 * "provide quality tutorials for self-learning " +
				 * "purpose in the domains of Academics, Information " +
				 * "Technology, Management and Computer Programming Languages.");
				 */

				document.write(out);
				out.close();
				System.out.println("createparagraph.docx written successfully");

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			Files.delete(new File(Main.DOCX_INPUT_PATH + "_temp").toPath());
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	public XWPFParagraph createParagraph(XWPFDocument document/*XWPFParagraph xwpfParagraph*/, XmlCursor cursor) {
		/*if (xwpfParagraph == null) {
			throw new IllegalArgumentException(XWPFParagraph.class + " can't be null");
		}*/
		//XWPFDocument document = xwpfParagraph.getDocument();
		//XmlCursor cursor = xwpfParagraph.getCTP().newCursor();
		XWPFParagraph newParagraph = document.insertNewParagraph(cursor);
		//newParagraph.setAlignment(xwpfParagraph.getAlignment());
		//newParagraph.getCTP().insertNewR(0).insertNewT(0).setStringValue("");
		//BigInteger numID = xwpfParagraph.getNumID();
		//newParagraph.setNumID(numID);
		// document.removeBodyElement(document.getPosOfParagraph(xwpfParagraph));
		return newParagraph;
	}


	public void createParagraphs(XWPFParagraph xwpfParagraph, String... paragraphs) {
		if (xwpfParagraph != null) {
			XWPFDocument document = xwpfParagraph.getDocument();
			for (int i = 0; i < paragraphs.length; i++) {
				XmlCursor cursor = xwpfParagraph.getCTP().newCursor();
				XWPFParagraph newParagraph = document.insertNewParagraph(cursor);
				//newParagraph.setAlignment(xwpfParagraph.getAlignment());
				//newParagraph.getCTP().insertNewR(0).insertNewT(0).setStringValue(paragraphs[i]);
				BigInteger numID = xwpfParagraph.getNumID();
				newParagraph.setNumID(numID);
			}
			// document.removeBodyElement(document.getPosOfParagraph(xwpfParagraph));
		}
	}

}
