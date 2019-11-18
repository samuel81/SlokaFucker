package me.samuel81.indexer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;

import me.samuel81.indexer.gui.MyFrame;

public class PDFHandler {

	public static void fixPDF(File f) {
		try (InputStream resource = new FileInputStream(f)) {
			PDDocument pd = PDDocument.load(resource);
			PDFRenderer renderer = new PDFRenderer(pd);
			PDPage last = null;
			int lastPage = -1;
			pd.addPage(new PDPage());

			int previousPages = pd.getPages().getCount();
			boolean odd = true;
			for (int i = 0; i < previousPages; i++) {
				PDPage now = pd.getPage(i);
				if (now.getArtBox().getWidth() > 400f)
					continue;
				if (i == 0 || i % 2 == 0) {
					last = now;
					lastPage = i;
				} else {
					if (last == null)
						continue;
					if (last.getArtBox().getWidth() == now.getArtBox().getWidth()
							&& last.getArtBox().getHeight() == now.getArtBox().getHeight()) {
						BufferedImage lastI = renderer.renderImage(i);
						BufferedImage nowI = renderer.renderImage(lastPage);

						BufferedImage joined = null;
						if (odd) {
							joined = joinBufferedImage(lastI, nowI);
						} else {
							joined = joinBufferedImage(nowI, lastI);
						}

						PDPage newPage = new PDPage();
						pd.addPage(newPage);
						PDImageXObject pdImageXObject = LosslessFactory.createFromImage(pd, joined);
						@SuppressWarnings("deprecation")
						PDPageContentStream contentStream = new PDPageContentStream(pd, newPage, true, false);
						contentStream.drawImage(pdImageXObject, 0, 0);
						contentStream.close();
					}
					last = null;
					odd = !odd;
				}
			}
			pd.save(f);
			pd.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void rotatePDF(File f, boolean autoRotate, boolean autoRBP, boolean toTiff) {
		try (InputStream resource = new FileInputStream(f)) {
			PDDocument document = PDDocument.load(resource);
			Iterator<PDPage> iter = document.getPages().iterator();
			int pages = 0;
			while (iter.hasNext()) {
				PDPage page = iter.next();
				float w = page.getArtBox().getWidth();
				float h = page.getArtBox().getHeight();

				boolean landscape = w > h;
				if (autoRotate) {
					if (MyFrame.rotateIfLandscape.isSelected() && landscape) {
						page.setRotation((int) MyFrame.rotateLandscapeAmount.getSelectedItem());
					} else {
						page.setRotation((int) MyFrame.rotateAmount.getSelectedItem());
					}
				}

				/*
				 * System.out.println("========="); System.out.println(w);
				 * System.out.println(h); System.out.println("=========");
				 */
				if (autoRBP) {
					if ((w < 700f && h < 600f)) {
						document.removePage(page);
						continue;
					}
				}

				pages++;

				if (pages % 4 == 0) {
				}

			}

			document.save(f);
			document.close();

			if (toTiff) {
				Process process = Runtime.getRuntime().exec(String.format(
						"gs9.50/bin/gswin64c -dBATCH -dNOPAUSE -r150 -sDEVICE=tiff24nc -sOutputFile=\"%s\" \"%s\"",
						f.getAbsolutePath().replace("pdf", "tiff"), f.getAbsolutePath()).replace("/", "\\"));
				process.waitFor();
				File folder = new File(f.getParent() + "/READY TO INDEX");
				if (!folder.exists())
					folder.mkdirs();
				FileUtils.moveFile(new File(f.getAbsolutePath().replace("pdf", "tiff")),
						new File(folder, f.getName().replace("pdf", "tiff")));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static BufferedImage joinBufferedImage(BufferedImage img1, BufferedImage img2) {

		// do some calculate first
		int wid = img1.getWidth() + img2.getWidth();
		int height = Math.max(img1.getHeight(), img2.getHeight());
		// create a new buffer and draw two image into the new image
		BufferedImage newImage = new BufferedImage(wid, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = newImage.createGraphics();
		Color oldColor = g2.getColor();
		// fill background
		g2.setPaint(Color.WHITE);
		g2.fillRect(0, 0, wid, height);
		// draw image
		g2.setColor(oldColor);
		g2.drawImage(img1, null, 0, 0);
		g2.drawImage(img2, null, img1.getWidth(), 0);
		g2.dispose();
		return newImage;
	}

}
