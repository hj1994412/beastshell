package beast.app.shell;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.ImageIcon;

import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;

import beast.app.beauti.BeautiDoc;
import beast.app.util.Utils;
import beast.util.AddOnManager;

public class EditorPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	JTabbedPane tabbedPane;
	List<String> fileNames;
	JTextField searchField;
	private JCheckBox regexCB;
	private JCheckBox matchCaseCB;
	
	String cwd = System.getProperty("user.dir");

	public EditorPanel() {
		fileNames = new ArrayList<String>();
		setLayout(new BorderLayout());
		JToolBar toolBar = new JToolBar();
		add(toolBar, BorderLayout.NORTH);

		JButton btnNewButton = new JButton("");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doNew();
			}

		});
		btnNewButton.setIcon(new ImageIcon(EditorPanel.class.getResource("/beast/app/shell/icons/new.png")));
		btnNewButton.setToolTipText("Start new editor");
		toolBar.add(btnNewButton);

		JButton btnNewButton_3 = new JButton("");
		btnNewButton_3.setIcon(new ImageIcon(EditorPanel.class.getResource("/beast/app/shell/icons/open.png")));
		btnNewButton_3.setToolTipText("Open file");
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doOpen();
			}
		});
		toolBar.add(btnNewButton_3);

		JButton btnNewButton_1 = new JButton("");
		btnNewButton_1.setSelectedIcon(new ImageIcon(EditorPanel.class.getResource("/beast/app/shell/icons/recent.png")));
		btnNewButton_1.setToolTipText("Recently opened files");
		toolBar.add(btnNewButton_1);

		JButton btnNewButton_2 = new JButton("");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSave();
			}
		});
		btnNewButton_2.setIcon(new ImageIcon(EditorPanel.class.getResource("/beast/app/shell/icons/save.png")));
		btnNewButton_2.setToolTipText("Save current editor");
		toolBar.add(btnNewButton_2);

		JButton btnNewButton_4 = new JButton("");
		btnNewButton_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSaveAll();
			}
		});
		btnNewButton_4.setIcon(new ImageIcon(EditorPanel.class.getResource("/beast/app/shell/icons/saveall.png")));
		btnNewButton_4.setToolTipText("Save all files");
		toolBar.add(btnNewButton_4);

		toolBar.addSeparator();

		// Create a toolbar with searching options.
		searchField = new JTextField(30);
		toolBar.add(searchField);

		JButton prevButton = new JButton("");
		prevButton.setToolTipText("Find Previous");
		prevButton.setIcon(new ImageIcon(EditorPanel.class.getResource("/beast/app/shell/icons/prev.png")));
		prevButton.setActionCommand("FindPrev");
		prevButton.addActionListener(this);
		toolBar.add(prevButton);

		final JButton nextButton = new JButton("");
		nextButton.setIcon(new ImageIcon(EditorPanel.class.getResource("/beast/app/shell/icons/next.png")));
		nextButton.setToolTipText("Find Next");
		nextButton.setActionCommand("FindNext");
		nextButton.addActionListener(this);
		toolBar.add(nextButton);
		searchField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nextButton.doClick(0);
			}
		});

		regexCB = new JCheckBox("Regex");
		toolBar.add(regexCB);
		matchCaseCB = new JCheckBox("Match Case");
		toolBar.add(matchCaseCB);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane, BorderLayout.CENTER);
		
		restoreEditors();
	}

	public void actionPerformed(ActionEvent e) {

		// "FindNext" => search forward, "FindPrev" => search backward
		String command = e.getActionCommand();
		boolean forward = "FindNext".equals(command);

		// Create an object defining our search parameters.
		SearchContext context = new SearchContext();
		String text = searchField.getText();
		if (text.length() == 0) {
			return;
		}
		context.setSearchFor(text);
		context.setMatchCase(matchCaseCB.isSelected());
		context.setRegularExpression(regexCB.isSelected());
		context.setSearchForward(forward);
		context.setWholeWord(false);

		RSyntaxTextArea textPane = getCurrentTextPane();
		if (textPane == null) {
			JOptionPane.showMessageDialog(this, "No text to search in...");
			return;
		}
		SearchResult result = SearchEngine.find(textPane, context);
		if (!result.wasFound()) {
			JOptionPane.showMessageDialog(this, "Text not found");
		}

	}

	private RSyntaxTextArea getCurrentTextPane() {
		JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
		if (scrollPane == null) {
			return null;
		}
		for (Component c : scrollPane.getViewport().getComponents()) {
			if (c instanceof RSyntaxTextArea) {
				return (RSyntaxTextArea) c;
			}
		}
		return null;
	}

	private void doNew() {
		RSyntaxTextArea /* JTextArea */textPane;
		textPane = new RSyntaxTextArea();
		textPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		textPane.setCodeFoldingEnabled(true);
		textPane.setAntiAliasingEnabled(true);
		addTab("New file", textPane);
		fileNames.add(null);
	}

	private void doOpen() {
		File file = Utils.getLoadFile("Open BEASTScript file", new File(cwd), "BEAST shell script files", "bsh");
		doOpen(file);
	}
	
	private void doOpen(File file) {
		if (file != null) {
			String text = null;
			try {
				text = BeautiDoc.load(file);
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "Could not open file: " + e.getMessage());
				return;
			}
			RSyntaxTextArea /* JTextArea */textPane;
			textPane = new RSyntaxTextArea();
			textPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
			textPane.setCodeFoldingEnabled(true);
			textPane.setAntiAliasingEnabled(true);
			textPane.setText(text);
			addTab(file.getName(), textPane);
			fileNames.add(file.getAbsolutePath());
			cwd = file.getParent();			
		}
	}

	private void doSave() {
		int i = tabbedPane.getSelectedIndex();
		doSave(i);
	}

	private void doSaveAll() {
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			doSave(i);
		}
	}

	private void doSave(int i) {
		File file = null;
		if (fileNames.get(i) == null) {
			tabbedPane.setSelectedIndex(i);
			file = Utils.getSaveFile("Open BEASTscript file", new File(cwd), "BEAST shell script files", "bsh");
			if (file != null) {
				cwd = file.getParent();
			}
		} else {
			file = new File(fileNames.get(i));
		}
		if (file != null) {
			RSyntaxTextArea textPane = getCurrentTextPane();
			String text = textPane.getText();
			try {
				FileWriter outfile = new FileWriter(file);
				outfile.write(text);
				outfile.close();
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "Something went wrong writing the file + " + file.getName() + ": " + e.getMessage());
				return;
			}
			tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), file.getName());
			fileNames.set(i, file.getAbsolutePath());
		}
	}

	void addTab(String title, final JComponent panel) {
	    RTextScrollPane sp = new RTextScrollPane(panel);
	    sp.setFoldIndicatorEnabled(true);
		//JScrollPane scrollPane = new JScrollPane(panel);
		tabbedPane.addTab(title, sp);
		int i = tabbedPane.indexOfComponent(sp);
		tabbedPane.setTabComponentAt(i, new ButtonTabComponent(this));
		tabbedPane.setSelectedIndex(i);
		String s;
	}

	final static String STATUS_FILE = "editors.dat";
	
	private void restoreEditors() {
		File backup = new File(AddOnManager.getPackageUserDir() + "/beastshell/" + STATUS_FILE);
		if (backup.exists()) {
			try {
		        BufferedReader fin = new BufferedReader(new FileReader(backup));
		        String sStr = null;
		        while (fin.ready()) {
		            sStr = fin.readLine();
		            File file = new File(sStr);
		            doOpen(file);
		        }
		        fin.close();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}
	
	public void saveStatus() {
		File backupdir = new File(AddOnManager.getPackageUserDir() + "/beastshell/");
		if (!backupdir.exists()) {
			backupdir.mkdirs();
		}
		File backup = new File(AddOnManager.getPackageUserDir() + "/beastshell/" + STATUS_FILE);
		try {
	        FileWriter outfile = new FileWriter(backup);
	        for (String s : fileNames) {
	        	outfile.write(s);
	        	outfile.write('\n');
	        }
	        outfile.close();				
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
