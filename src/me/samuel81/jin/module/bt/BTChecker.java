package me.samuel81.jin.module.bt;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.swing.JOptionPane;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Wait;

import io.appium.java_client.windows.WindowsDriver;
import me.samuel81.jin.Main;
import me.samuel81.jin.module.BotIniter;
import me.samuel81.jin.module.Checker;
import me.samuel81.jin.module.bt.BTPos.PI;

public class BTChecker extends Checker {

	private Wait<WindowsDriver<WebElement>> waiter = null;

	private TreeMap<String, File> files = new TreeMap<>();

	private boolean firstTime = true;

	private Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

	private File posFile = null;
	private BTPos position = null;

	private String DESA, HAK;

	public BTChecker(List<File> ff, boolean upload, boolean override) {
		super(upload, override);
		waiter = BotIniter.getFluentWaiter(60 * 5, 1);
		ff.forEach(v -> {
			files.put(
					String.format("%05d",
							Integer.parseInt(v.getName().split("_")[1].replace(".pdf", "").replaceAll("[^0-9]", ""))),
					v);
		});
	}

	@Override
	public void savePos() {
		FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream(posFile);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(position);
			out.close();
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public BTPos loadPos() {
		BTPos pos = new BTPos(DESA, HAK);
		try {
			posFile = BotIniter.getPos("BT_" + DESA + "_" + HAK);
			if (posFile.length() > 0) {
				FileInputStream fileIn = new FileInputStream(posFile);
				ObjectInputStream in = new ObjectInputStream(fileIn);
				pos = (BTPos) in.readObject();
				fileIn.close();
				in.close();
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return pos;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void startCheck() {
		DESA = session.findElementByAccessibilityId("cmbDesa").getText();
		HAK = session.findElementByAccessibilityId("cmbTipeHak").getText();

		try {
			position = loadPos();
			// Searching for the object in DB
			if ((position != null && !position.isNeedToResume()) && !upload)
				session.findElementByAccessibilityId("Cari").click();

			WebElement footer = session.findElementByAccessibilityId("LayoutFooter");

			int totalOnDB = 0;
			int totalPage = 0;

			int currentPage = 0;

			try {

				// Waiting for the object that appear in the box to show up, if its not showing
				// up until 3 minutes, its not registered on database
				String total = waiter.until(new Function<WindowsDriver<WebElement>, String>() {
					@Override
					public String apply(WindowsDriver<WebElement> t) {
						return footer.findElement(By.className("TextBlock")).getText();
					}
				});
				totalOnDB = Integer.valueOf(
						total.substring(total.indexOf("("), total.indexOf(")")).replace("(", "").replace(")", ""));
				String[] ts = total.split(" ");
				totalPage = Integer.valueOf(ts[2]);
				currentPage = Integer.valueOf(ts[0]);

			} catch (Exception e) {
				e.printStackTrace();
				Main.getMyFrame().log("There's no BT in DB \n");
				addFailed();
				return;
			}

			if (position.getPos().size() == totalOnDB && !position.isNeedToResume()) {
				useSavedPos = true;
			} else if (position.getPos().size() < totalOnDB && !position.isNeedToResume()) {
				position = new BTPos(DESA, HAK);
			} else {
				Main.getMyFrame().log("ON RESUME!\n Latest Check: " + position.getLatestCheck() + "\n\n");
			}

			if (totalOnDB > 0) {

				if ((!useSavedPos) && (!upload)) {
					// Check if the pos has already checked before but paused
					String latest = position.getLatestCheck();
					int latestCheck = latest.equalsIgnoreCase("") ? 0 : position.getPos(latest).getX();

					WebElement nextButtton = footer.findElement(By.name("Next"));
					WebElement presenter = session.findElementByAccessibilityId("dataPresenter");

					// If yes, click next button until hit the latest page before latest check got
					// error
					if (latestCheck > 0) {
						if ((currentPage - 1) != latestCheck) {
							JOptionPane.showMessageDialog(null,
									"Please direct the page to the latest checking page (" + (latestCheck + 1) + ")",
									"ERROR", JOptionPane.ERROR_MESSAGE);
							position.setResume(true);
							return;
						}
					}

					// Dont know what we're doing here, but as u can see, im indexing all of the
					// items in database
					for (int p = latestCheck; p < totalPage; p++) {
						List<WebElement> items = waiter
								.until(new Function<WindowsDriver<WebElement>, List<WebElement>>() {
									@Override
									public List<WebElement> apply(WindowsDriver<WebElement> t) {
										return presenter.findElements(By.name("Warkah.BukuTanahItem"));
									}
								});

						for (int i = 0; i < items.size(); i++) {
							WebElement item = items.get(i);
							List<WebElement> locator = waiter
									.until(new Function<WindowsDriver<WebElement>, List<WebElement>>() {
										@Override
										public List<WebElement> apply(WindowsDriver<WebElement> t) {
											return item.findElements(By.xpath(".//*"));
										}
									});

							String noHak = locator.get(1).getText();
							noHak = noHak.substring(noHak.length() - 5);
							if (!useSavedPos) {
								Main.getMyFrame().log(noHak + "   |   " + p + "   |   " + i + "\n");
								position.putPoint(noHak, p, i);
							}
						}

						// Sleep for 5 seconds after clicking 50
						if (p % 50 == 0)
							Thread.sleep(TimeUnit.MILLISECONDS.toSeconds(5));
						nextButtton.click();
					}

				} else {
					if (!(fixSlokaDesa(DESA).equalsIgnoreCase(Main.getMyFrame().getCurrentKelurahan())
							&& convertSlokaToFolder(HAK).equalsIgnoreCase(Main.getMyFrame().getCurrentJenis()))) {
						JOptionPane.showMessageDialog(null,
								"Please make sure that the identity is \nthe same in sloka and folder", "ERROR",
								JOptionPane.ERROR_MESSAGE);
						return;
					}

					int p = currentPage - 1;
					WebElement nextButtton = footer.findElement(By.name("Next"));
					WebElement presenter = session.findElementByAccessibilityId("dataPresenter");

					Iterator<Entry<String, File>> queue = files.entrySet().iterator();

					Entry<String, File> centry = null;

					boolean fMove = true;

					int counter = 0;
					while (queue.hasNext()) {
						centry = queue.next();
						String k = centry.getKey();
						File v = centry.getValue();
						PI pi = position.getPos(k);

						if (pi == null) {
							Main.getMyFrame().log("BT cannot be found on DB " + k + "\n");
						} else {
							int page = pi.getX();
							int index = pi.getY();

							if (fMove) {
								if (p != page) {
									JOptionPane.showMessageDialog(null,
											"Please direct the page to the latest checking page (" + (page + 1) + ")",
											"ERROR", JOptionPane.ERROR_MESSAGE);
									break;
								}
								fMove = false;
							} else {
								while (p < page) {
									nextButtton.click();
									p++;
								}
							}

							WebElement item = waiter.until(new Function<WindowsDriver<WebElement>, WebElement>() {
								@Override
								public WebElement apply(WindowsDriver<WebElement> t) {
									return presenter.findElements(By.name("Warkah.BukuTanahItem"))
											.get(index > 10 ? index - (page * 10) : index);
								}
							});

							String fName = v.getName();

							item.click();
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
									clipboard.setContents(new StringSelection(v.getParent()), null);
									session.getKeyboard().sendKeys(Keys.chord(Keys.CONTROL, "v"));
									session.getKeyboard().sendKeys(Keys.chord(Keys.ENTER));
									firstTime = false;
								}
								
								el.click();
								// session.getKeyboard().sendKeys(Keys.chord(Keys.LEFT_ALT, "n"));
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

							WebElement bottom = session.findElementByClassName("BukuTanah");
							bottom.findElement(By.name("PART_CloseButton")).click();
							Main.getMyFrame().log(uploaded ? "Successfully uploading " + fName + "\n"
									: "Failed to upload " + fName + "\n");
							if (uploaded) {
								addSuccess();
								Main.getMyFrame().addProggress(totalSuccess());
							}
						}
						if (counter++ % 50 == 0)
							Thread.sleep(2000);

					}
				}
			}
			if (!useSavedPos)
				position.setResume(false);
		} catch (Exception e) {
			e.printStackTrace();
			if (!useSavedPos)
				position.setResume(true);
			Main.getMyFrame().log("Failed to check \n");
			addFailed();
			return;
		}
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

}
