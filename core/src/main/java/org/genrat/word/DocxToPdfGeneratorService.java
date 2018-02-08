package org.genrat.word;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.converter.core.XWPFConverterException;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.genrat.core.IGeneratorService;
import org.springframework.stereotype.Service;

@Service
public class DocxToPdfGeneratorService implements IGeneratorService {

	
	public ByteArrayOutputStream createPdf(InputStream templateInput, Serializable data) {
		// 1) Load DOCX into XWPFDocument
		try (XWPFDocument document = loadDocxIntoXWPFDocument(templateInput);
				ByteArrayOutputStream out = new ByteArrayOutputStream()) {

			// 2) Bind XML data into XWPFDocument
			DocProcessor docProcessor = bindXMLDataIntoXWPFDocument(document);
			processXWPFDocument(docProcessor, data);
			// 3) Prepare Pdf options
			PdfOptions options = preparePdfOptions();
			
			// 3) Convert XWPFDocument to Pdf
			
			convertXWPFDocumentToPdf(docProcessor.getDocument(), options, out);
			return out;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private DocProcessor bindXMLDataIntoXWPFDocument(XWPFDocument document) {
		long start = System.currentTimeMillis();		
		DocProcessor docProcessor = new DocProcessor(document);
		docProcessor.wordDocProcessor();
		System.err.println(
				"Bind data to doc in " + (System.currentTimeMillis() - start) + "ms");
		return docProcessor;
	}

	private void processXWPFDocument(DocProcessor docProcessor, Serializable data) {
		docProcessor.applyData(data);
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
				"Generate pdf in " + (System.currentTimeMillis() - start) + "ms");
	}


	private XWPFDocument loadDocxIntoXWPFDocument(InputStream input) {
		long start = System.currentTimeMillis();
		try {
			return new XWPFDocument(OPCPackage.open(input));
		} catch (InvalidFormatException | IOException e) {
			throw new RuntimeException(e);
		} finally {
			System.err.println(
					"Load docx by poi in " + (System.currentTimeMillis() - start) + "ms");

		}
	}

}
