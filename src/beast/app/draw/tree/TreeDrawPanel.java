package beast.app.draw.tree;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;

import beast.core.BEASTObject;
import beast.util.XMLParser;

public class TreeDrawPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	TreeDrawingGrid grid;
	
	public TreeDrawPanel(TreeDrawingGrid grid) {
		this.grid = grid;
	}
	
	@Override
	public void paint(Graphics g) {
		grid.paint((Graphics2D) g);
	}

	public static void main(String[] args) throws Exception {
		XMLParser parser = new XMLParser();
		BEASTObject o = parser.parseFile(new File(args[0]));
		if (!(o instanceof TreeDrawingGrid)) {
			throw new RuntimeException("Expected top level element of type TreeDrawingGrid");
		}
		TreeDrawingGrid grid = (TreeDrawingGrid) o;
		JFrame frame = new JFrame();
		TreeDrawPanel panel = new TreeDrawPanel(grid);
		frame.add(panel);
		frame.setSize(1024, 768);
		frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
