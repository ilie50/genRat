package org.genrat.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.genrat.word.DocProcessor;

public class Main {

	public static final String DOCX_INPUT_PATH = "C:\\tmp\\gr\\RemoteDebugginSetup.docx";
	public static final String PDF_OUTPUT_PATH = "C:\\tmp\\gr\\pdf\\";

	public static void main(String[] args) {
		Main main = new Main();
		String xml = "<name>Name</name>";
		main.createPDF(DOCX_INPUT_PATH, PDF_OUTPUT_PATH, xml);
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

	public void updateWorkingDoc(String templateInputPath, String outputFolderPath, String xmlData) {

		DocProcessor docProcessor = null;
		
		String workingFilePath = createDocWorkingCopy(templateInputPath, outputFolderPath);

		// 1) Load DOCX into XWPFDocument
		try (XWPFDocument document = loadDocxIntoXWPFDocument(workingFilePath)) {

			// 2) Bind XML data into XWPFDocument
			docProcessor = bindXMLDataIntoXWPFDocument(workingFilePath, document);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		//deleteWorkingFile(workingFilePath);

	}

	public void createPDF(String templateInputPath, String outputFolderPath, String xmlData) {

		
		String workingFilePath = createDocWorkingCopy(templateInputPath, outputFolderPath);

		// 1) Load DOCX into XWPFDocument
		try (XWPFDocument document = loadDocxIntoXWPFDocument(workingFilePath)) {

			// 2) Bind XML data into XWPFDocument
			DocProcessor docProcessor = bindXMLDataIntoXWPFDocument(workingFilePath, document);
			processXWPFDocument(docProcessor);
			// 3) Prepare Pdf options
			PdfOptions options = preparePdfOptions();
			
			// 3) Convert XWPFDocument to Pdf
			convertXWPFDocumentToPdf(docProcessor.getDocument(), options, workingFilePath + ".pdf");

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		deleteWorkingFile(workingFilePath);

	}

	private void deleteWorkingFile(String workingFilePath) {
		try {
			Files.delete(new File(workingFilePath).toPath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}		
	}

	private String createDocWorkingCopy(String templateInputPath, String outputFolderPath) {
		String fileOutputPath = outputFolderPath + File.separator + UUID.randomUUID().toString();
		try {
			Files.copy(new File(templateInputPath).toPath(), new File(fileOutputPath).toPath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return fileOutputPath;
	}

	private DocProcessor bindXMLDataIntoXWPFDocument(String workingFilePath, XWPFDocument document) {
		DocProcessor docProcessor = new DocProcessor(document);
		docProcessor.wordDocProcessor(/*new DocVisitor()*/);
		return docProcessor;
	}

	private void processXWPFDocument(DocProcessor docProcessor) {
		List<String> list = docProcessor.processTemplate();
		docProcessor.applyData(list);
	}

	private PdfOptions preparePdfOptions() {
		return PdfOptions.create();
	}

	private void convertXWPFDocumentToPdf(XWPFDocument document, PdfOptions options, String fileOutputPath) {
		long start = System.currentTimeMillis();

		File fileOutput = new File(fileOutputPath);
		try (OutputStream out = new FileOutputStream(fileOutput)) {
			PdfConverter.getInstance().convert(document, out, options);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		System.err.println(
				"Generate " + fileOutput.getAbsolutePath() + " with " + (System.currentTimeMillis() - start) + "ms");
	}

	public XWPFDocument loadDocxIntoXWPFDocument(String fileInputPath) {
		try {
			return new XWPFDocument(OPCPackage.open(fileInputPath));
		} catch (InvalidFormatException | IOException e) {
			throw new RuntimeException(e);
		}
	}

}
