package me.samuel81.indexer.gui;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import me.samuel81.indexer.PDFHandler;

public class MyFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2062371638919874756L;

	private JFileChooser chooser;

	private JButton pdfSource = new JButton("Browse");
	private JButton startB = new JButton("Start");

	private JTextField txtD = new JTextField();

	private JLabel lblG = new JLabel("PDF Source");

	private JCheckBox rotate = new JCheckBox("Rotate");
	private JCheckBox rbp = new JCheckBox("Remove BP");
	private JCheckBox fix = new JCheckBox("Fix");
	private JCheckBox convertToTiff = new JCheckBox("To TIFF");
	private JCheckBox convertToPDF = new JCheckBox("To PDF");

	public static JCheckBox rotateIfLandscape = new JCheckBox("Rotate If Landscape");

	private JLabel lblF = new JLabel("Rotate Amount");
	public static JComboBox<Integer> rotateAmount = new JComboBox<>(new Integer[] { 90, 180, 270 });

	private JLabel lblH = new JLabel("Rotate Landscape Amount");
	public static JComboBox<Integer> rotateLandscapeAmount = new JComboBox<>(new Integer[] { 90, 180, 270 });

	private List<File> pdfFiles = new ArrayList<>();

	public MyFrame() {
		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));

		setTitle("YANG MAKE HARUS TAU DIRI");
		setSize(400, 380);
		setLocation(new Point(300, 200));
		setLayout(null);
		setResizable(false);

		initComponent();
		initEvent();
	}

	private void initComponent() {
		startB.setBounds(180, 45, 100, 60);
		pdfSource.setBounds(340, 10, 20, 20);

		txtD.setBounds(130, 10, 200, 20);
		txtD.setEditable(false);
		lblG.setBounds(50, 10, 100, 20);

		rbp.setBounds(45, 55, 100, 20);
		fix.setBounds(45, 35, 100, 20);
		convertToTiff.setBounds(45, 75, 100, 20);
		convertToPDF.setBounds(45, 95, 100, 20);

		rotate.setBounds(45, 135, 100, 20);
		lblF.setBounds(45, 155, 100, 20);
		rotateAmount.setBounds(45, 175, 100, 20);

		lblH.setBounds(45, 220, 200, 20);
		rotateIfLandscape.setBounds(45, 200, 200, 20);
		rotateLandscapeAmount.setBounds(45, 240, 100, 20);

		add(startB);
		add(pdfSource);

		add(lblG);
		add(txtD);

		add(rotate);
		add(rbp);
		add(fix);
		add(convertToTiff);
		add(convertToPDF);

		add(lblF);
		add(rotateAmount);

		add(lblH);
		add(rotateIfLandscape);
		add(rotateLandscapeAmount);
	}

	private void initEvent() {

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(1);
			}
		});

		pdfSource.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browse(e);
			}
		});

		startB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (pdfFiles == null || pdfFiles.size() < 1) {
					JOptionPane.showMessageDialog(null, "Please select PDF file first!", "ERROR",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				for (File file : pdfFiles) {
					PDFHandler.rotatePDF(file, rotate.isSelected(), rbp.isSelected(), convertToTiff.isSelected());
					if (fix.isSelected())
						PDFHandler.fixPDF(file);
				}
				setCursor(null);
				Toolkit.getDefaultToolkit().beep();
				JOptionPane.showMessageDialog(null, "Please do some final QC!", "Done!",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});

	}

	private void browse(ActionEvent e) {

		chooser.setDialogTitle("Select folder!");
		/* if (type != FolderType.PDF_FILES) { */
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setAcceptAllFileFilterUsed(false);
		/*
		 * } else { chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		 * chooser.setAcceptAllFileFilterUsed(true); chooser.setFileFilter(new
		 * FileFilter() {
		 * 
		 * @Override public boolean accept(File file) { if (file.isDirectory()) { return
		 * true; } else { String path = file.getAbsolutePath().toLowerCase(); String
		 * extension = "pdf"; if ((path.endsWith(extension) &&
		 * (path.charAt(path.length() - extension.length() - 1)) == '.')) { return true;
		 * } } return false; }
		 * 
		 * @Override public String getDescription() { return ""; } }); }
		 */
		/*
		 * f (type == FolderType.SOURCE || type == FolderType.PDF_FILES) {
		 */ chooser.setMultiSelectionEnabled(true);
		/*
		 * } else { chooser.setMultiSelectionEnabled(false); }
		 */

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

			pdfFiles.clear();
			/**
			 * Sorting files and folders from selection dialog
			 */
			for (File folders : chooser.getSelectedFiles()) {
				if (folders.isDirectory()) {
					pdfFiles.addAll(pdfOnFolders(folders));
				} else {
					String path = folders.getAbsolutePath().toLowerCase();
					String extension = "pdf";
					if ((path.endsWith(extension) && (path.charAt(path.length() - extension.length() - 1)) == '.')) {
						pdfFiles.add(folders);
					}
				}
			}
			txtD.setText("Selected " + pdfFiles.size() + " pdf file(s)");

		}
		/*
		 * } }
		 */
	}

	public static List<File> pdfOnFolders(File folder) {
		List<File> files = new ArrayList<>();
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				files.addAll(pdfOnFolders(file));
				continue;
			}
			String path = file.getAbsolutePath().toLowerCase();
			String extension = "pdf";
			if ((path.endsWith(extension) && (path.charAt(path.length() - extension.length() - 1)) == '.')) {
				files.add(file);
			}
		}
		return files;
	}

}