package me.samuel81.indexer.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lombok.Getter;
import me.samuel81.indexer.Main;
import me.samuel81.indexer.PDFHandler;
import me.samuel81.indexer.object.FolderType;

@Getter
public class MyFrame extends JFrame {

	private static final long serialVersionUID = 2062371638919874756L;

	private JFileChooser chooser = new JFileChooser();

	private JButton startB = new JButton("Start");

	/**
	 * PDF Chooser component
	 */
	private JButton pdfSource = new JButton("Browse");
	private JTextField txtD = new JTextField();
	private JLabel lblG = new JLabel("PDF Source");

	/**
	 * NAS Chooser component
	 */
	private JButton nasTarget = new JButton("Browse");
	private JTextField txtB = new JTextField();
	private JLabel lblE = new JLabel("NAS Folder");

	/**
	 * PDF Process component
	 */
	private JCheckBox rotate = new JCheckBox("Rotate");
	private JCheckBox rbp = new JCheckBox("Remove BP");
	private JCheckBox fix = new JCheckBox("Fix");
	private JCheckBox convert = new JCheckBox("Convert To Tiff");
	private JCheckBox split = new JCheckBox("Split");

	/**
	 * Rotate function component
	 */
	private JLabel lblF = new JLabel("Rotate Amount");
	private JComboBox<Integer> rotateAmount = new JComboBox<>(new Integer[] { 90, 180, 270 });

	/**
	 * This is for landscape component
	 */
	private JCheckBox rotateIfLandscape = new JCheckBox("Rotate If Landscape");
	private JLabel lblH = new JLabel("Rotate Landscape Amount");
	private JComboBox<Integer> rotateLandscapeAmount = new JComboBox<>(new Integer[] { 90, 180, 270 });

	/**
	 * Apps mode chooser
	 */
	private JSlider toggleMode = new JSlider();

	private List<File> pdfFiles = new ArrayList<>();
	private File nasFolder = null;

	private int MODE = 0;
	private int PDF = 0, FETCHING = 1, MOVING = 2;

	public MyFrame() {

		setTitle("YANG MAKE HARUS TAU DIRI");
		setSize(400, 380);
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

		nasFolder = new File(Main.config.getNasTarget());

		chooser.setCurrentDirectory(new java.io.File("."));

		toggleMode.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
				"PDF          |          FETCH          |          PURGE", TitledBorder.CENTER, TitledBorder.TOP, null,
				new Color(0, 0, 0)));

		toggleMode.setSnapToTicks(false);
		toggleMode.setPaintTicks(true);
		toggleMode.setPaintLabels(false);
		toggleMode.setPaintTrack(false);
		toggleMode.setMaximum(2);
		toggleMode.setValue(0);

		startB.setBounds(150, 250, 100, 40);
		pdfSource.setBounds(340, 10, 20, 20);
		nasTarget.setBounds(340, 50, 20, 20);

		txtD.setBounds(130, 10, 200, 20);
		txtD.setEditable(false);
		lblG.setBounds(50, 10, 100, 20);

		txtB.setBounds(130, 50, 200, 20);
		txtB.setEditable(false);
		txtB.setText(Main.config.getNasTarget());
		lblE.setBounds(50, 50, 100, 20);

		rbp.setBounds(45, 55, 100, 20);
		fix.setBounds(45, 35, 100, 20);
		convert.setBounds(150, 35, 150, 20);
		toggleMode.setBounds(75, 300, 250, 50);
		split.setBounds(45, 75, 100, 20);

		rotate.setBounds(45, 95, 100, 20);
		lblF.setBounds(45, 115, 100, 20);
		rotateAmount.setBounds(45, 135, 100, 20);

		lblH.setBounds(45, 180, 200, 20);
		rotateIfLandscape.setBounds(45, 160, 200, 20);
		rotateLandscapeAmount.setBounds(45, 200, 100, 20);

		add(nasTarget);
		add(txtB);
		add(lblE);
		nasTarget.setVisible(false);
		txtB.setVisible(false);
		lblE.setVisible(false);

		add(startB);
		add(pdfSource);

		add(lblG);
		add(txtD);

		add(rotate);
		add(rbp);
		add(fix);
		add(convert);
		add(split);
		add(toggleMode);

		add(lblF);
		add(rotateAmount);

		add(lblH);
		add(rotateIfLandscape);
		add(rotateLandscapeAmount);
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

		this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if ((e.getKeyCode() == KeyEvent.VK_S) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
					startProcessing();
				}
			}
		});

		pdfSource.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (MODE == PDF) {
					browse(e, FolderType.PDF);
				} else {
					browse(e, FolderType.LOCAL_FOLDER);
				}
			}
		});

		nasTarget.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browse(e, FolderType.NAS);
			}
		});

		toggleMode.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				MODE = toggleMode.getValue();
				if (MODE == PDF) {
					rbp.setVisible(true);
					split.setVisible(true);
					rotate.setVisible(true);
					fix.setVisible(true);
					convert.setVisible(true);
					lblF.setVisible(true);
					rotateAmount.setVisible(true);
					lblH.setVisible(true);
					rotateIfLandscape.setVisible(true);
					rotateLandscapeAmount.setVisible(true);

					nasTarget.setVisible(false);
					txtB.setVisible(false);
					lblE.setVisible(false);
				} else if (MODE == FETCHING) {
					rbp.setVisible(false);
					split.setVisible(false);
					rotate.setVisible(false);
					fix.setVisible(false);
					convert.setVisible(false);
					lblF.setVisible(false);
					rotateAmount.setVisible(false);
					lblH.setVisible(false);
					rotateIfLandscape.setVisible(false);
					rotateLandscapeAmount.setVisible(false);

					nasTarget.setVisible(true);
					txtB.setVisible(true);
					lblE.setVisible(true);
				} else if (MODE == MOVING) {
					rbp.setVisible(false);
					split.setVisible(false);
					convert.setVisible(false);
					rotate.setVisible(false);
					fix.setVisible(false);
					lblF.setVisible(false);
					rotateAmount.setVisible(false);
					lblH.setVisible(false);
					rotateIfLandscape.setVisible(false);
					rotateLandscapeAmount.setVisible(false);

					nasTarget.setVisible(false);
					txtB.setVisible(false);
					lblE.setVisible(false);
				}
				pdfFiles.clear();
				txtD.setText("");
			}
		});

		startB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startProcessing();
			}
		});

	}

	/**
	 * Start processing folder / files
	 */
	private void startProcessing() {
		if (pdfFiles == null || pdfFiles.size() < 1) {
			JOptionPane.showMessageDialog(null, "Please select PDF / FOLDER file first!", "ERROR",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		if (MODE == PDF) {
			for (File file : pdfFiles) {
				PDFHandler.prosesPDF(file, rotate.isSelected(), rbp.isSelected(), split.isSelected(),
						convert.isSelected());
				if (fix.isSelected())
					PDFHandler.mergeBrokenPDF(file);
			}
		} else if (MODE == FETCHING) {

		} else if (MODE == MOVING) {
			for (File folder : pdfFiles) {
				File indexFolder = new File(folder + "/READY TO INDEX");
				if (!indexFolder.exists())
					continue;
				if (folder.listFiles().length > 1) {
					for (File f : folder.listFiles()) {
						if (f.getName().equalsIgnoreCase(indexFolder.getName()))
							continue;
						f.delete();
					}
				}
				for (File f : indexFolder.listFiles()) {
					String path = f.getAbsolutePath().toLowerCase();
					String extension = "tif";
					if ((path.endsWith(extension) && (path.charAt(path.length() - extension.length() - 1)) == '.')) {
						f.delete();
					}
				}
				/*
				 * try { FileUtils.copyDirectory(folder, new File(nasFolder + "/" +
				 * folder.getName())); } catch (IOException e1) { // TODO Auto-generated catch
				 * block e1.printStackTrace(); }
				 */
			}
		}
		setCursor(null);
		Toolkit.getDefaultToolkit().beep();
		JOptionPane.showMessageDialog(null, "Please do some final QC!", "Done!", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Open files / folder browser filter and option based on mode and which button
	 * is pressed
	 * 
	 * @param e
	 * @param type
	 */
	private void browse(ActionEvent e, FolderType type) {

		chooser.setDialogTitle(type == FolderType.PDF ? "Select PDF File(s)!" : "Select folder(s)!");

		if (type == FolderType.NAS) {
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setMultiSelectionEnabled(false);
		} else {
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			chooser.setMultiSelectionEnabled(true);
		}
		chooser.setAcceptAllFileFilterUsed(false);

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

			/**
			 * Sorting files and folders from selection dialog
			 */
			if (type == FolderType.PDF) {
				pdfFiles.clear();
				for (File folders : chooser.getSelectedFiles()) {
					if (folders.isDirectory()) {
						pdfFiles.addAll(pdfOnFolders(folders));
					} else {
						String path = folders.getAbsolutePath().toLowerCase();
						String extension = MODE == PDF ? "pdf" : "tif";
						if ((path.endsWith(extension)
								&& (path.charAt(path.length() - extension.length() - 1)) == '.')) {
							pdfFiles.add(folders);
						}
					}
					txtD.setText("Selected " + pdfFiles.size() + " file(s)");
				}
			} else if (type == FolderType.NAS) {
				nasFolder = chooser.getSelectedFile();
				txtB.setText(nasFolder.getAbsolutePath());
				Main.config.setNasTarget(nasFolder.getAbsolutePath());
				Main.saveYaml();
			} else if (type == FolderType.LOCAL_FOLDER) {
				pdfFiles.clear();
				for (File folders : chooser.getSelectedFiles()) {
					pdfFiles.add(folders);
				}
				txtD.setText("Selected " + pdfFiles.size() + " folder(s)");
			}

		}
	}

	/**
	 * List all PDF or TIFF file in a folder
	 * 
	 * @param folder
	 * @return an array PDF or TIFF file
	 */
	private List<File> pdfOnFolders(File folder) {
		List<File> files = new ArrayList<>();
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				files.addAll(pdfOnFolders(file));
				continue;
			}
			String path = file.getAbsolutePath().toLowerCase();
			String extension = MODE == PDF ? "pdf" : "tif";
			if ((path.endsWith(extension) && (path.charAt(path.length() - extension.length() - 1)) == '.')) {
				files.add(file);
			}
		}
		return files;
	}

}