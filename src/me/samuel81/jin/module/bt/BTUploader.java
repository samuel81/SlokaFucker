package me.samuel81.jin.module.bt;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.List;
import java.util.function.Function;

import javax.swing.JOptionPane;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import io.appium.java_client.windows.WindowsDriver;
import me.samuel81.jin.Main;
import me.samuel81.jin.module.Uploader;

public class BTUploader extends Uploader {

	private boolean firstTime = true;

	private Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

	private String DESA, HAK;

	public BTUploader(List<File> files, boolean override) {
		super(files, override);
	}

	@SuppressWarnings("deprecation")
	@Override
	public int upload(File file) {

		String fName = file.getName();
		String[] fSplitted = fName.split("_");
		try {
			// Searching
			WebElement nomor = session.findElementByAccessibilityId("Nomor Hak");
			nomor.click();
			nomor.findElement(By.className("TextBox")).sendKeys(Keys.chord(Keys.CONTROL, "a"));
			nomor.findElement(By.className("TextBox")).sendKeys(Keys.chord(Keys.BACK_SPACE));

			String myString = fSplitted[1].replaceFirst("^0+(?!$)", "").replace(".pdf", "");
			clipboard.setContents(new StringSelection(myString), null);

			// nomor.findElement(By.className("TextBox"))
			// .sendKeys(fSplitted[1].replaceFirst("^0+(?!$)", "").replace(".pdf", ""));
			nomor.findElement(By.className("TextBox")).sendKeys(Keys.chord(Keys.CONTROL, "v"));

			// Searching for the object in DB
			session.findElementByAccessibilityId("Cari").click();

			try {

				WebElement data = waiter.until(new Function<WindowsDriver<WebElement>, WebElement>() {

					@Override
					public WebElement apply(WindowsDriver<WebElement> t) {
						WebElement presenter = session.findElementByAccessibilityId("dataPresenter");
						return waiter.until(ExpectedConditions.presenceOfNestedElementLocatedBy(presenter,
								By.name("Warkah.BukuTanahItem")));
					}
				});
				data.click();

			} catch (Exception e) {
				e.printStackTrace();
				Main.getMyFrame().log("BT cannot be found on DB " + fName + "\n");
				addFailed();
				return 1;
			} finally {

			}

			if (!isTabOpened())
				toggleTabOpen();
			// dbObj.click();

			WebElement browser = waiter.until(new Function<WindowsDriver<WebElement>, WebElement>() {
				@Override
				public WebElement apply(WindowsDriver<WebElement> t) {
					return session.findElementByAccessibilityId("BarSplitButtonItemLinkbOpenFromWeb");
				}
			});

			boolean uploaded = false;

			if (!override) {
				try {
					uploaded = session.findElementByAccessibilityId("Update").isEnabled();
				} catch (Exception e) {
					uploaded = false;
				}
			}

			if (!uploaded) {
				// Uploading
				// session.findElementByAccessibilityId("BarSplitButtonItemLinkbOpenFromWeb").click();
				// WebElement input = session.findElementByAccessibilityId("1148");
				browser.click();
				WebElement el = waiter.until(new Function<WindowsDriver<WebElement>, WebElement>() {
					@Override
					public WebElement apply(WindowsDriver<WebElement> t) {
						return session.findElementByAccessibilityId("1148").findElement(By.className("Edit"));
					}
				});
				
				if (firstTime) {
					session.getKeyboard().sendKeys(Keys.chord(Keys.F4));
					session.getKeyboard().sendKeys(Keys.chord(Keys.CONTROL, "a"));
					session.getKeyboard().sendKeys(Keys.chord(Keys.DELETE));
					// session.getKeyboard().sendKeys(file.getParent());
					clipboard.setContents(new StringSelection(file.getParent()), null);
					session.getKeyboard().sendKeys(Keys.chord(Keys.CONTROL, "v"));
					session.getKeyboard().sendKeys(Keys.chord(Keys.ENTER));
					firstTime = false;
				}
				el.click();
				clipboard.setContents(new StringSelection(fName), null);
				Thread.sleep(500);
				el.sendKeys(Keys.chord(Keys.CONTROL, "v"));
				el.sendKeys(Keys.chord(Keys.ENTER));

				session.findElementByAccessibilityId("Upload").click();

				// Wait until the file is successfully uploaded to the database
				uploaded = waiter.until(new Function<WindowsDriver<WebElement>, Boolean>() {

					@Override
					public Boolean apply(WindowsDriver<WebElement> t) {
						return session.findElementByAccessibilityId("Update").isEnabled();
					}
				});

			}

			closeTab();
			if (isTabOpened())
				toggleTabOpen();
			Main.getMyFrame()
					.log(uploaded ? "Successfully uploading " + fName + "\n" : "Failed to upload " + fName + "\n");
			if (uploaded)
				addSuccess();
			else
				addFailed();
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			Main.getMyFrame().log("Failed to upload " + fName + "\n");
			addFailed();
			return 2;
		}
	}

	@Override
	public void closeTab() {
		WebElement bottom = session.findElementByClassName("BukuTanah");
		bottom.findElement(By.name("PART_CloseButton")).click();
	}

	public String convertSlokaToFolder(String sloka) {
		switch (sloka.toUpperCase()) {
		case "HAK MILIK":
			return "HM";
		case "HAK GUNA BANGUNAN":
			return "HGB";
		case "HAK PAKAI":
			return "HP";
		case "HAK WAKAF":
			return "HW";
		}
		return "";
	}

	public String fixSlokaDesa(String sloka) {
		return sloka.toUpperCase().replaceAll(" ", "_");
	}

	@Override
	public boolean checkSloka() {
		DESA = session.findElementByAccessibilityId("cmbDesa").getText();
		HAK = session.findElementByAccessibilityId("cmbTipeHak").getText();

		if (!(fixSlokaDesa(DESA).equalsIgnoreCase(Main.getMyFrame().getCurrentKelurahan())
				&& convertSlokaToFolder(HAK).equalsIgnoreCase(Main.getMyFrame().getCurrentJenis()))) {
			JOptionPane.showMessageDialog(null, "Please make sure that the identity is \nthe same in sloka and folder",
					"ERROR", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;
	}

}
