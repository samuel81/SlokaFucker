package me.samuel81.indexer.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.itextpdf.text.DocumentException;

import lombok.Getter;
import me.samuel81.indexer.PDFHandler;
import me.samuel81.indexer.Uploader;

@Getter
public class MyFrame extends JFrame {

	private static final long serialVersionUID = 2062371638919874756L;

	private JFileChooser chooser = new JFileChooser();

	private JButton startB = new JButton("Start");

	/**
	 * PDF Chooser component
	 */
	private JButton imgSource = new JButton("Browse");
	private JTextField txtD = new JTextField();
	private JLabel lblG = new JLabel("Image Source");

	/**
	 * Apps mode chooser
	 */
	private JSlider toggleMode = new JSlider();

	private Set<File> imgFiles = new HashSet<>();

	private int MODE = 0;
	private int CONVERT = 0, UPLOAD = 1;

	public MyFrame() {

		setTitle("Image2PDF Batch Converter (S4-Development");
		setSize(400, 200);
		setLocation(new Point(300, 200));
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
				"CONVERT          |          UPLOAD", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));

		toggleMode.setSnapToTicks(false);
		toggleMode.setPaintTicks(true);
		toggleMode.setPaintLabels(false);
		toggleMode.setPaintTrack(false);
		toggleMode.setMaximum(1);
		toggleMode.setValue(0);

		toggleMode.setBounds(70, 40, 250, 50);
		startB.setBounds(150, 105, 100, 40);
		imgSource.setBounds(340, 10, 20, 20);

		txtD.setBounds(130, 10, 200, 20);
		txtD.setEditable(false);
		lblG.setBounds(30, 10, 100, 20);

		add(startB);
		add(imgSource);

		add(lblG);
		add(txtD);

		add(toggleMode);

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

		imgSource.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					browse(e);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		startB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startProcessing();
			}
		});

		toggleMode.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				MODE = toggleMode.getValue();
				if (MODE == CONVERT) {

					lblG.setText("Image Source");
				} else if (MODE == UPLOAD) {

					lblG.setText("PDF Source");
				}
				imgFiles.clear();
				txtD.setText("");
			}
		});

	}

	/**
	 * Start processing folder / files
	 */
	private void startProcessing() {
		if (imgFiles == null || imgFiles.size() < 1) {
			JOptionPane.showMessageDialog(null, "Please select images source first!", "ERROR",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		long start = System.currentTimeMillis();
		int success = 0, failed = 0;
		if (MODE == CONVERT) {
			for (File file : imgFiles) {
				try {
					PDFHandler.processImage(file);
				} catch (DocumentException | IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			Uploader uploader = new Uploader(imgFiles);
			uploader.setup();
			uploader.startUpload();
			uploader.saveLog();
			success = uploader.totalSuccess();
			failed = uploader.totalFailed();
		}
		long totalTime = System.currentTimeMillis() - start;
		setCursor(null);
		Toolkit.getDefaultToolkit().beep();
		if(MODE == CONVERT) {
		JOptionPane.showMessageDialog(null,
				"Total processed : " + PDFHandler.totalProcessed() + "\n Time elapsed: "
						+ TimeUnit.MILLISECONDS.toSeconds(totalTime),
				"Please do some final QC!", JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null,
					"Total success : " + success + "\n Total failed : " + failed + "\n Time elapsed: "
							+ TimeUnit.MILLISECONDS.toSeconds(totalTime),
					"Done uploading file!", JOptionPane.INFORMATION_MESSAGE);
		}
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
			imgFiles.clear();
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
							.filter(x -> ((x.toFile().getName().contains("SU")))
									|| ((x.toFile().getName().contains("GS"))))
							.forEach(x ->  {
								folder.add(x.toFile());
								//System.out.println(x.toFile().getAbsolutePath());
							});
				}
			}
			imgFiles = folder;
			txtD.setText("Selected " + folder.size() + " " + (MODE == CONVERT ? "folder(s)" : "file(s)"));
		}

	}

}