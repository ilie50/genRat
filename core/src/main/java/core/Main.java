package core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class Main {

	public static void main(String[] args) {
		readDoc();
		createPDF();
	}
	
	public static String readDoc() {
		// Alternate between the two to check what works.
		String filePath = "C:\\tmp\\gr\\RemoteDebugginSetup.docx";
		FileInputStream fis;
		String text = "";
		if (filePath.substring(filePath.length() - 1).equals("x")) { // is a
			XWPFWordExtractor extract = null;													// docx
			try {
				fis = new FileInputStream(new File(filePath));
				XWPFDocument doc = new XWPFDocument(fis);
				extract = new XWPFWordExtractor(doc);
				text = extract.getText();
				System.out.println(text);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (extract != null) {
					try {
						extract.close();
					} catch (IOException e) {
					}
				}
			}
		} else { // is not a docx
			WordExtractor extractor = null;
			try {
				fis = new FileInputStream(new File(filePath));
				HWPFDocument doc = new HWPFDocument(fis);
				extractor = new WordExtractor(doc);
				text = extractor.getText();
				System.out.println(text);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (extractor != null) {
					try {
						extractor.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return text;
	}
	
	private static void createPDF() {
        try {
            long start = System.currentTimeMillis();
 
            // 1) Load DOCX into XWPFDocument
            InputStream is = new FileInputStream(new File(
                    "C:\\tmp\\gr\\RemoteDebugginSetup.docx"));
            XWPFDocument document = new XWPFDocument(is);
 
            // 2) Prepare Pdf options
            PdfOptions options = PdfOptions.create();
 
            // 3) Convert XWPFDocument to Pdf
            OutputStream out = new FileOutputStream(new File(
                    "C:\\tmp\\gr\\pdf\\RemoteDebugginSetup.pdf"));
            PdfConverter.getInstance().convert(document, out, options);
             
            System.err.println("Generate pdf/RemoteDebugginSetup.pdf with "
                    + (System.currentTimeMillis() - start) + "ms");
             
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
	
}
