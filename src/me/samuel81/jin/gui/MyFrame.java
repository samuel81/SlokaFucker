package me.samuel81.jin.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultCaret;

import lombok.Getter;
import me.samuel81.jin.module.BotIniter;
import me.samuel81.jin.module.Checker;
import me.samuel81.jin.module.Converter;
import me.samuel81.jin.module.Uploader;
import me.samuel81.jin.module.bt.BTChecker;
import me.samuel81.jin.module.bt.BTConverter;
import me.samuel81.jin.module.bt.BTUploader;
import me.samuel81.jin.module.su.SUChecker;
import me.samuel81.jin.module.su.SUConverter;
import me.samuel81.jin.module.su.SUUploader;

@Getter
public class MyFrame extends JFrame {

	private static final long serialVersionUID = 2062371638919874756L;

	// Start button
	private JButton startB = new JButton("Start");

	// Chooser
	private JButton imgSource = new JButton("..........");
	private JTextField txtD = new JTextField();
	private JLabel lblG = new JLabel("FILE SOURCE");
	private JFileChooser chooser = new JFileChooser();

	// Mode
	private JSlider toggleMode = new JSlider();
	private JSlider toggleType = new JSlider();

	// Log
	private JScrollPane scrollPane;
	private JTextArea console;

	//
	private JCheckBox override = new JCheckBox("Override");
	private JCheckBox upload = new JCheckBox("Upload");

	// Files source
	private List<File> filesList = new ArrayList<>();

	private int MODE = 0;
	private int CONVERT = 0, UPLOAD = 1, CHECKER = 2;

	private int TYPE = 0;
	private int SU = 0, BT = 1;

	private String KELURAHAN = "", JENIS = "";

	// Monitor
	private ProgressMonitor monitor;

	public MyFrame() {
		setTitle("Image2PDF Batch SUConverter (S4-Development)");
		setSize(800, 1000);
		setLocation(new Point(300, 0));
		setLayout(null);
		setResizable(false);

		initComponent();
		initEvent();
	}

	/**
	 * Initing component
	 */
	private void initComponent() {

		chooser.setCurrentDirectory(new java.io.File("."));

		toggleMode.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
				"CONVERT          |          UPLOAD          |          CHECK", TitledBorder.CENTER, TitledBorder.TOP,
				null, new Color(0, 0, 0)));

		toggleType.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "SU          |          BT",
				TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));

		toggleMode.setSnapToTicks(false);
		toggleMode.setPaintTicks(true);
		toggleMode.setPaintLabels(false);
		toggleMode.setPaintTrack(false);
		toggleMode.setMaximum(2);
		toggleMode.setValue(0);

		toggleType.setSnapToTicks(false);
		toggleType.setPaintTicks(true);
		toggleType.setPaintLabels(false);
		toggleType.setPaintTrack(false);
		toggleType.setMaximum(1);
		toggleType.setValue(0);

		toggleMode.setBounds(140, 80, 500, 50);
		toggleType.setBounds(140, 140, 500, 50);
		startB.setBounds(350, 210, 100, 40);
		imgSource.setBounds(710, 20, 70, 40);
		add(startB);
		add(imgSource);
		add(toggleMode);
		add(toggleType);

		txtD.setBounds(200, 20, 500, 40);
		txtD.setEditable(false);
		txtD.setFont(new Font("Dialog", Font.BOLD, 20));
		lblG.setFont(new Font("Dialog", Font.BOLD, 24));
		lblG.setBounds(12, 20, 200, 40);
		add(lblG);
		add(txtD);

		scrollPane = new JScrollPane(console = new JTextArea());
		console.setEditable(false);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setViewportBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Log",
				TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		scrollPane.setBounds(12, 302, 770, 500);
		add(scrollPane);
		DefaultCaret caret = ((DefaultCaret) console.getCaret());
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		override.setBounds(140, 200, 200, 30);
		upload.setBounds(140, 230, 200, 30);
		override.setVisible(false);
		upload.setVisible(false);
		add(override);
		add(upload);

	}

	/**
	 * Initing component event
	 */
	private void initEvent() {

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(1);
			}
		});

		// Files browse
		imgSource.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					browse(e);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		// Start button
		startB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startProcessing();
			}
		});

		// Change mode
		toggleMode.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				MODE = toggleMode.getValue();
				if (MODE == CONVERT) {
					override.setVisible(false);
					upload.setVisible(false);
					imgSource.setEnabled(true);

					lblG.setText("Image Source");
				} else if (MODE == UPLOAD) {
					override.setVisible(true);
					upload.setVisible(false);
					imgSource.setEnabled(true);
					lblG.setText("PDF Source");
				} else if (MODE == CHECKER) {
					override.setVisible(true);
					upload.setVisible(true);
					imgSource.setEnabled(false);
					lblG.setText("PDF Source");
				}
				filesList.clear();
				txtD.setText("");
			}
		});

		upload.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				AbstractButton abstractButton = (AbstractButton) e.getSource();
				ButtonModel buttonModel = abstractButton.getModel();
				if (buttonModel.isSelected()) {
					imgSource.setEnabled(true);
				} else {
					imgSource.setEnabled(false);
				}
			}
		});

		toggleType.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				TYPE = toggleType.getValue();
				if (MODE == SU) {

				} else if (MODE == BT) {

				}
				filesList.clear();
				txtD.setText("");
			}
		});

	}

	/**
	 * Start processing folder / files
	 */
	private void startProcessing() {
		if (MODE != CHECKER && (filesList == null || filesList.size() < 1)) {
			JOptionPane.showMessageDialog(null, "Please select source first!", "ERROR", JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			BotIniter.init();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		startB.setEnabled(false);
		imgSource.setEnabled(false);
		console.setText(null);
		setupBar(filesList.size());
		if (MODE == CONVERT) {
			Converter converter = TYPE == SU ? new SUConverter(filesList) : new BTConverter(filesList);
			converter.start();
		} else if (MODE == UPLOAD) {
			Uploader uploader = TYPE == SU ? new SUUploader(filesList, override.isSelected())
					: new BTUploader(filesList, override.isSelected());
			uploader.start();

		} else if (MODE == CHECKER) {
			Checker checker = TYPE == SU ? new SUChecker(filesList, upload.isSelected(), override.isSelected())
					: new BTChecker(filesList, upload.isSelected(), override.isSelected());
			checker.start();
		}
	}

	public void doneTask(int success, int failed, long totalTime) {
		startB.setEnabled(true);
		imgSource.setEnabled(true);
		setCursor(null);
		monitor.close();
		Toolkit.getDefaultToolkit().beep();
		if (MODE == CONVERT) {
			JOptionPane.showMessageDialog(null,
					"Total processed : " + success + "\n Time elapsed: " + TimeUnit.MILLISECONDS.toSeconds(totalTime),
					"Please do some final QC!", JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null,
					"Total success : " + success + "\n Total failed : " + failed + "\n Time elapsed: "
							+ TimeUnit.MILLISECONDS.toSeconds(totalTime),
					"Done " + (MODE == CONVERT ? "uploading" : "checking") + " file!", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public void log(String log) {
		console.append(log);
	}

	public void setupBar(int max) {
		monitor = new ProgressMonitor(this, "Processing...", "", 0, max);
		String message = String.format("%d / %d", 0, monitor.getMaximum());
		monitor.setNote(message);
		monitor.setProgress(1);
	}

	public void addProggress(int i) {
		String message = String.format("%d / %d", i, monitor.getMaximum());
		monitor.setNote(message);
		monitor.setProgress(i);
	}

	public boolean isCanceled() {
		return monitor.isCanceled();
	}
	
	public String getCurrentKelurahan() {
		return KELURAHAN;
	}
	
	public String getCurrentJenis() {
		return JENIS;
	}

	/**
	 * Open files / folder browser filter and option based on mode and which button
	 * is pressed
	 * 
	 * @param e
	 * @param type
	 * @throws IOException
	 */
	private void browse(ActionEvent e) throws IOException {

		chooser.setDialogTitle("Select " + (MODE == CONVERT ? "images" : "pdf") + " source folder!");

		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setMultiSelectionEnabled(MODE == CONVERT ? true : false);

		chooser.setAcceptAllFileFilterUsed(false);
		Set<File> folder = new HashSet<>();
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			filesList.clear();

			if (TYPE == SU) {
				if (MODE == CONVERT) {
					for (File folders : chooser.getSelectedFiles()) {
						try (Stream<Path> paths = Files.walk(folders.toPath())) {
							paths.filter(Files::isRegularFile)
									.filter(x -> (x.getFileName().toString().toLowerCase().endsWith(".jpg")
											|| x.getFileName().toString().toLowerCase().endsWith(".bmp")))
									.filter(x -> ((x.toFile().getParentFile().getName().contains("SU")))
											|| ((x.toFile().getParentFile().getName().contains("GS"))))
									.forEach(x -> folder.add(x.toFile().getParentFile()));
						}
					}
				} else {
					File f = chooser.getSelectedFile();
					try (Stream<Path> paths = Files.walk(f.toPath())) {
						paths.filter(Files::isRegularFile)
								.filter(x -> (x.getFileName().toString().toLowerCase().endsWith(".pdf")))
								.filter(x -> (((x.toFile().getName().contains("SU"))
										|| (x.toFile().getName().contains("GS")))
										&& ((!x.toFile().getParentFile().getName().contains("DONE"))
												&& (!x.toFile().getParentFile().getName().contains("FAILED")))))
								.forEach(x -> {
									folder.add(x.toFile());
								});
					}
				}
			} else {
				File f = chooser.getSelectedFile();
				if (!f.getName().equalsIgnoreCase("HGB") && !f.getName().equalsIgnoreCase("HM")
						&& !f.getName().equalsIgnoreCase("HP") && !f.getName().equalsIgnoreCase("HW")) {
					JOptionPane.showMessageDialog(null, "Please select HAK folder!", "ERROR",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				JENIS = f.getName().toUpperCase();
				KELURAHAN = f.getParentFile().getName().toUpperCase();

				try (Stream<Path> paths = Files.walk(f.toPath())) {
					paths.filter(Files::isRegularFile)
							.filter(x -> (x.getFileName().toString().toLowerCase().endsWith(".pdf")))
							.filter(x -> ((x.toFile().getName().startsWith("M")))
									|| ((x.toFile().getName().startsWith("B")))
									|| ((x.toFile().getName().startsWith("W")))
									|| ((x.toFile().getName().startsWith("P"))))
							.forEach(x -> folder.add(x.toFile()));
				}
			}

			Set<File> imgFiles = folder;
			txtD.setText("Selected " + folder.size() + " " + (MODE == CONVERT ? "folder(s)" : "file(s)"));

			filesList = new ArrayList<>(imgFiles);

			Collections.sort(filesList, new Comparator<File>() {
				public int compare(File f1, File f2) {
					return f1.getName().replace(".pdf", "").compareTo(f2.getName().replace(".pdf", ""));
				}
			});

			console.setText(null);
			for (File fi : filesList) {
				console.append(fi.getName() + "\n");
			}
		}

	}

}