package me.samuel81.indexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;

import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

public class PDFHandler {

	/**
	 * Processing PDF files
	 * 
	 * @param f          PDF files that need to be processed
	 * @param autoRotate check if automatically rotate page
	 * @param autoRBP    check if automatically remove blank page
	 * @param split      check if automatically split page
	 * @param convert    check if automatically convert PDF to TIFF
	 */
	public static void prosesPDF(File f, boolean autoRotate, boolean autoRBP, boolean split, boolean convert) {

		try (InputStream resource = new FileInputStream(f)) {
			PDDocument document = PDDocument.load(resource);
			Iterator<PDPage> iter = document.getPages().iterator();

			/**
			 * Count page and warkah and use it later to automatically split the pdf
			 */
			int pages = 0;
			int warkah = 0;

			while (iter.hasNext()) {
				PDPage page = iter.next();
				float w = page.getArtBox().getWidth();
				float h = page.getArtBox().getHeight();

				boolean landscape = w > h;
				if (autoRotate) {
					if (Main.getMyFrame().getRotateIfLandscape().isSelected() && landscape) {
						page.setRotation((int) Main.getMyFrame().getRotateLandscapeAmount().getSelectedItem());
					} else {
						page.setRotation((int) Main.getMyFrame().getRotateAmount().getSelectedItem());
					}
				}

				/*
				 * System.out.println("========="); System.out.println(w);
				 * System.out.println(h); System.out.println("=========");
				 */

				/**
				 * Remove page which has lower page size then the other
				 */
				if (autoRBP) {
					if ((w < 700f && h < 600f)) {
						document.removePage(page);
						continue;
					}
				}

				pages++;

				if (pages % 4 == 0) {
					warkah++;
				}

			}
			
			//parsePdf(f);

			if (split) {
				if (warkah == 2) {
					if (pages == 8) {

						PDFTextStripper pdfStripper = new PDFTextStripper();
						pdfStripper.setStartPage(1);
						pdfStripper.setEndPage(1);
						String page1 = pdfStripper.getText(document).toUpperCase();

						pdfStripper.setStartPage(5);
						pdfStripper.setEndPage(5);
						String page2 = pdfStripper.getText(document).toUpperCase();

						if (page1.contains("BUKU") || page1.contains("TANAH") || page1.contains("HAK")
								|| page1.contains("205")) {

							if (page2.contains("SURAT") || page2.contains("UKUR") || page2.contains("SEBIDANG")
									|| page2.contains("207") || page2.contains("GAMBAR")) {

								Splitter splitter = new Splitter();
								splitter.setSplitAtPage(4);
								List<PDDocument> splittedDocuments = splitter.split(document);
								int i = 0;
								for (PDDocument doc : splittedDocuments) {
									File newF = new File(f.getAbsolutePath().replace(".pdf", "-" + i + ".pdf"));
									doc.save(newF);
									doc.close();
									i++;

									if (convert)
										convertToTiff(newF);
								}
							}
						}
					}
				}
				document.save(f);
				document.close();
				//FileUtils.moveFile(f, new File(f.getParent() + "/BEKASAN", f.getName()));
			} else {

				document.save(f);
				document.close();

				if (convert)
					convertToTiff(f);
			}
			resource.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Converting PDF to TIFF with GhostScript
	 * 
	 * @param f PDF File to convert
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private static void convertToTiff(File f) throws IOException, InterruptedException {
		Process process = Runtime.getRuntime()
				.exec(String.format(
						Main.config.getGsFolder()
								+ "gswin64c -dBATCH -dNOPAUSE -r150 -sDEVICE=tiff24nc -sOutputFile=\"%s\" \"%s\"",
						f.getAbsolutePath().replace("pdf", "tif"), f.getAbsolutePath()).replace("/", "\\"));
		process.waitFor();

		File folder = new File(f.getParent() + "/READY TO INDEX");
		if (!folder.exists())
			folder.mkdirs();
		FileUtils.moveFile(new File(f.getAbsolutePath().replace("pdf", "tif")),
				new File(folder, f.getName().replace("pdf", "tif")));
	}

	/**
	 * Fixing broken PDF page By merging 1-2, 3-4, 5-6, and etc page into 1 page
	 * 
	 * @param pdf that want to be fixed
	 */
	public static void mergeBrokenPDF(File pdf) {
		File pdfFile = new File(pdf.getAbsolutePath().replace(".pdf", "-fixed.pdf"));
		try {
			Document TifftoPDF = new Document();
			PdfWriter pdfWriter = PdfWriter.getInstance(TifftoPDF, new FileOutputStream(pdfFile));

			pdfWriter.setStrictImageSequence(true);
			pdfWriter.setPdfVersion(PdfWriter.VERSION_1_4);

			pdfWriter.open();
			TifftoPDF.open();

			PdfContentByte cb = pdfWriter.getDirectContent();

			PdfReader reader = new PdfReader(new FileInputStream(pdf));

			boolean odd = true;

			PdfImportedPage last = null;

			for (int i = 0; i < reader.getNumberOfPages(); i++) {
				PdfImportedPage now = pdfWriter.getImportedPage(reader, i + 1);
				if (now.getWidth() > 400f)
					continue;
				if (i == 0 || i % 2 == 0) {
					last = now;
				} else {
					if (last == null)
						continue;
					if (last.getWidth() == now.getWidth() && last.getHeight() == now.getHeight()) {
						TifftoPDF.newPage();
						Rectangle pageSize = new Rectangle(now.getWidth() * 2, now.getHeight());
						TifftoPDF.setPageSize(pageSize);

						if (odd) {
							cb.addTemplate(now, 0, 0);
							cb.addTemplate(last, now.getWidth(), 0);
						} else {
							cb.addTemplate(last, 0, 0);
							cb.addTemplate(now, now.getWidth(), 0);
						}
					}
					last = null;
					odd = !odd;
				}
			}
			TifftoPDF.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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
            System.out.println("Page "+i+": \n"+strategy.getResultantText());
        }
        reader.close();
    }

}
