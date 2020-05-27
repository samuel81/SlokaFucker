package me.samuel81.jin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

public class PDFHandler {
	
	private static int totalProcessed = 0;

	public static void processImage(File folder) throws DocumentException, MalformedURLException, IOException {
		ByteArrayOutputStream target = null;
		File[] imgs = folder.listFiles(new ImageFileFilter());
		if (imgs.length == 0)
			return;
		Document document = new Document();
		PdfWriter.getInstance(document, (target = new ByteArrayOutputStream()));
		document.open();
		for (File img : imgs) {
			Image image1 = Image.getInstance(img.getPath());
			document.setPageSize(image1);
			document.newPage();
			image1.setAbsolutePosition(0, 0);
			document.add(image1);
		}
		document.close();
		String name = folder.getName();
		String type, nib, no, thn;
		String[] t1 = name.split("-");
		if(t1.length < 3) {
			System.out.println(name);
			return;
		}
		nib = t1[0];
		String[] t2 = t1[2].split("_");
		if(t2.length < 3) {
			System.out.println(name);
			return;
		}
		type = t2[0];
		no = t2[1];
		thn = t2[2];
		File movedFolder = new File(folder.getParent() + "/" + nib);
		if(!movedFolder.exists())
			movedFolder.mkdirs();
		Util.manipulatePdf(target.toByteArray(),
				new File(movedFolder, type + "_" + nib + "_" + no + "_" + thn + ".pdf")
						.toPath().toString());
		totalProcessed++;
		if(totalProcessed % 100 == 0) {
			System.out.println("Processed : "+totalProcessed);
		}
		// Files.deleteIfExists(target.toPath());
	}
	
	public static int totalProcessed() {
		return totalProcessed;
	}

	/**
	 * Parses a PDF to a plain text file.
	 * 
	 * @param pdf the original PDF
	 * @param txt the resulting text
	 * @throws IOException
	 */
	public static void parsePdf(File pdf) throws IOException {
		PdfReader reader = new PdfReader(new FileInputStream(pdf));
		PdfReaderContentParser parser = new PdfReaderContentParser(reader);

		TextExtractionStrategy strategy;
		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
			System.out.println("Page " + i + ": \n" + strategy.getResultantText());
		}
		reader.close();
	}

	static class ImageFileFilter implements FileFilter {
		private final String[] okFileExtensions = new String[] { "jpg", "jpeg", "png", "bmp" };

		public boolean accept(File file) {
			for (String extension : okFileExtensions) {
				if (file.getName().toLowerCase().endsWith(extension)) {
					return true;
				}
			}
			return false;
		}
	}

}
