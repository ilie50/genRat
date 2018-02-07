package org.genrat.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

	public void createPdf(String templateInputPath, String outputFolderPath) {

		ByteArrayOutputStream byteArrayOutputStream;
		try (FileInputStream fis = new FileInputStream(new File(templateInputPath))) {
			byteArrayOutputStream = createPdf(fis);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		try(OutputStream outputStream = new FileOutputStream(outputFolderPath + UUID.randomUUID().toString() + ".pdf")) {
		    byteArrayOutputStream.writeTo(outputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 
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
