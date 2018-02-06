package org.genrat.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.UUID;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.converter.core.XWPFConverterException;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.genrat.word.DocProcessor;
import org.springframework.stereotype.Service;

@Service
public class GeneratorService {

	
	public ByteArrayOutputStream createPdf(InputStream templateInput) {
		// 1) Load DOCX into XWPFDocument
		try (XWPFDocument document = loadDocxIntoXWPFDocument(templateInput);
				ByteArrayOutputStream out = new ByteArrayOutputStream()) {

			// 2) Bind XML data into XWPFDocument
			DocProcessor docProcessor = bindXMLDataIntoXWPFDocument(document);
			processXWPFDocument(docProcessor);
			// 3) Prepare Pdf options
			PdfOptions options = preparePdfOptions();
			
			// 3) Convert XWPFDocument to Pdf
			
			convertXWPFDocumentToPdf(docProcessor.getDocument(), options, out);
			return out;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public void createPdf(String templateInputPath, String pdfOutputPath) {
		String xml = "<name>Name</name>";
		createPDF(templateInputPath, pdfOutputPath, xml);

	}
	
	public void createPDF(String templateInputPath, String outputFolderPath, String xmlData) {

		
		String workingFilePath = createDocWorkingCopy(templateInputPath, outputFolderPath);

		// 1) Load DOCX into XWPFDocument
		try (XWPFDocument document = loadDocxIntoXWPFDocument(workingFilePath)) {

			// 2) Bind XML data into XWPFDocument
			DocProcessor docProcessor = bindXMLDataIntoXWPFDocument(document);
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

	private DocProcessor bindXMLDataIntoXWPFDocument(XWPFDocument document) {
		DocProcessor docProcessor = new DocProcessor(document);
		docProcessor.wordDocProcessor();
		return docProcessor;
	}

	private void processXWPFDocument(DocProcessor docProcessor) {
		docProcessor.applyData();
	}

	private PdfOptions preparePdfOptions() {
		return PdfOptions.create();
	}

	private void convertXWPFDocumentToPdf(XWPFDocument document, PdfOptions options, OutputStream out) {
		long start = System.currentTimeMillis();
		try {
			PdfConverter.getInstance().convert(document, out, options);
		} catch (XWPFConverterException | IOException e) {
			throw new RuntimeException(e);
		}

		System.err.println(
				"Generate pdf with " + (System.currentTimeMillis() - start) + "ms");
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

	public XWPFDocument loadDocxIntoXWPFDocument(InputStream input) {
		try {
			return new XWPFDocument(OPCPackage.open(input));
		} catch (InvalidFormatException | IOException e) {
			throw new RuntimeException(e);
		}
	}


}
