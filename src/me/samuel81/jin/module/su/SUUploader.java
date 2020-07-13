package me.samuel81.jin.module.su;

import java.io.File;
import java.util.List;
import java.util.function.Function;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;

import io.appium.java_client.windows.WindowsDriver;
import me.samuel81.jin.Main;
import me.samuel81.jin.module.BotIniter;
import me.samuel81.jin.module.Uploader;

public class SUUploader extends Uploader {

	private boolean firstTime = true;

	private String latestThn = "";

	private Wait<WindowsDriver<WebElement>> waiter = null;

	public SUUploader(List<File> files, boolean override) {
		super(files, override);
		waiter = BotIniter.getFluentWaiter(60 * 3, 1);
	}

	@SuppressWarnings("deprecation")
	@Override
	public int upload(File file) {
		String fName = file.getName();
		String[] fSplitted = fName.split("_");
		try {
			// Searching
			WebElement nomor = session.findElementByAccessibilityId("txtNomor");
			nomor.click();
			nomor.findElement(By.className("TextBox")).sendKeys(Keys.CONTROL + "a");
			nomor.findElement(By.className("TextBox")).sendKeys(Keys.BACK_SPACE);
			nomor.findElement(By.className("TextBox")).sendKeys(fSplitted[2].replaceFirst("^0+(?!$)", ""));

			if (!latestThn.equalsIgnoreCase(fSplitted[3])) {
				WebElement thn = session.findElementByAccessibilityId("Tahun Surat Ukur");
				thn.click();
				thn.findElement(By.className("TextBox")).sendKeys(Keys.CONTROL + "a");
				thn.findElement(By.className("TextBox")).sendKeys(Keys.BACK_SPACE);
				thn.findElement(By.className("TextBox")).sendKeys(fSplitted[3]);
				latestThn = fSplitted[3];
			}

			// Searching for the object in DB
			session.findElementByAccessibilityId("Cari").click();

			// WebElement dbObj = null;

			try {
				// data.findElement(By.name("Warkah.BukuTanahItem")).click();
				// dbObj = waiter.until(ExpectedConditions.presenceOfNestedElementLocatedBy(
				// session.findElementByAccessibilityId("dataPresenter"),
				// By.name("Warkah.BukuTanahItem")));
				// session.findElementByAccessibilityId("dataPresenter").findElement(By.name("Warkah.BukuTanahItem")).click();

				waiter.until(new Function<WindowsDriver<WebElement>, Boolean>() {

					@Override
					public Boolean apply(WindowsDriver<WebElement> t) {
						WebElement presenter = session.findElementByAccessibilityId("dataPresenter");
						WebElement data = null;
						data = waiter.until(ExpectedConditions.presenceOfNestedElementLocatedBy(presenter,
								By.name("Warkah.SuratUkurItem")));
						data.click();
						return true;
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				Main.getMyFrame().log("BT cannot be found on DB " + fName + "\n");
				addFailed();
				return 1;
			} finally {

			}

			// dbObj.click();

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
				session.findElementByAccessibilityId("BarSplitButtonItemLinkbOpenFromWeb").click();
				WebElement input = session.findElementByAccessibilityId("1148");
				if (firstTime) {
					session.getKeyboard().sendKeys(Keys.F4);
					session.getKeyboard().sendKeys(Keys.CONTROL + "a" + Keys.CONTROL);
					session.getKeyboard().sendKeys(Keys.DELETE);
					session.getKeyboard().sendKeys(file.getParent());
					session.getKeyboard().sendKeys(Keys.ENTER);
					session.getKeyboard().sendKeys(Keys.LEFT_ALT + "n" + Keys.LEFT_ALT);
					firstTime = false;
				}
				WebElement edit = input.findElement(By.className("Edit"));
				edit.click();
				edit.sendKeys(fName);
				// session.getKeyboard().sendKeys(fName);
				session.getKeyboard().sendKeys(Keys.ENTER);

				session.findElementByAccessibilityId("Upload").click();
			}

			WebElement bottom = session.findElementByClassName("SuratUkur");
			bottom.findElement(By.name("PART_CloseButton")).click();
			Main.getMyFrame().log("Successfully uploading " + fName + "\n");
			addSuccess();
			return 0;
		} catch (Exception e) {
			Main.getMyFrame().log("Failed to upload " + fName + "\n");
			addFailed();
			return 2;
		}
	}

	@Override
	public void closeTab() {
		WebElement bottom = session.findElementByClassName("SuratUkur");
		bottom.findElement(By.name("PART_CloseButton")).click();
	}

	@Override
	public boolean checkSloka() {
		// TODO Auto-generated method stub
		return false;
	}

}
