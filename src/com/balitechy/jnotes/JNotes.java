package com.balitechy.jnotes;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import javax.swing.ImageIcon;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.JLabel;
import java.awt.Font;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.sql.ResultSet;
import java.util.Vector;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.JTable;


public class JNotes extends JFrame implements ActionListener{

	private JPanel contentPane;
	private JMenu mnFile, mnNote;
	private JMenuItem menuDatabase, menuSync, menuExit;
	private JMenuItem menuAddNote, menuAddGroup, menuAbout;
	private DBUtils dbu;
	private JSplitPane splitPane;
	private JScrollPane scrollPane;
	private JScrollPane scrollPane_1;
	private JTree tree;
	private JToolBar toolBar_1;
	private JButton btnAddGroup;
	private JButton btnAddNote;
	private JPanel panel;
	private JLabel statusBar;
	private JPopupMenu popupMenu;
	private JMenuItem popEdit;
	private JMenuItem popDelete;
	private JTable table;

	public static void main(String[] args) {		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				
				try{
					//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
				}catch(Exception e){}
				
				try {
					JNotes frame = new JNotes();
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public JNotes() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(JNotes.class.getResource("/com/balitechy/resources/page_white_edit.png")));
		setTitle("JNotes - take note anywhere, anytime.");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 400);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		/* File menu */
		mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		menuDatabase = new JMenuItem("Database...");
		menuDatabase.setIcon(new ImageIcon(JNotes.class.getResource("/com/balitechy/resources/server_database.png")));
		mnFile.add(menuDatabase);
		menuDatabase.addActionListener(this);
		
		menuSync = new JMenuItem("Sync...");
		menuSync.setIcon(new ImageIcon(JNotes.class.getResource("/com/balitechy/resources/arrow_refresh.png")));
		mnFile.add(menuSync);
		
		menuExit = new JMenuItem("Exit");
		menuExit.setIcon(new ImageIcon(JNotes.class.getResource("/com/balitechy/resources/door_in.png")));
		mnFile.add(menuExit);
		menuExit.addActionListener(this);
		
		
		/* Notes menu */
		mnNote = new JMenu("Note");
		menuBar.add(mnNote);
		
		menuAddNote = new JMenuItem("Add note...");
		menuAddNote.setIcon(new ImageIcon(JNotes.class.getResource("/com/balitechy/resources/page_white_add.png")));
		mnNote.add(menuAddNote);
		menuAddNote.addActionListener(this);
		
		menuAddGroup = new JMenuItem("Add group...");
		menuAddGroup.setIcon(new ImageIcon(JNotes.class.getResource("/com/balitechy/resources/folder_add.png")));
		mnNote.add(menuAddGroup);
		menuAddGroup.addActionListener(this);
		
		/* Help menu */
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		menuAbout = new JMenuItem("About");
		menuAbout.setIcon(new ImageIcon(JNotes.class.getResource("/com/balitechy/resources/information.png")));
		mnHelp.add(menuAbout);
		menuAbout.addActionListener(this);
		
		
		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		//Split Pane
		splitPane = new JSplitPane();
		splitPane.setAutoscrolls(true);
		splitPane.setDividerLocation(200);
		contentPane.add(splitPane);
		
		scrollPane = new JScrollPane();
		splitPane.setLeftComponent(scrollPane);
		
		// Notes Tree
		tree = new JTree();
		tree.setEditable(false);
		tree.setBorder(new EmptyBorder(5, 5, 5, 5));
		tree.addTreeSelectionListener(new TreeSelectionListener(){

			@Override
			public void valueChanged(TreeSelectionEvent tse) {
				runPopulateNotes();
			}
			
		});
		scrollPane.setViewportView(tree);
		
		//Tree pop menu
		popupMenu = new JPopupMenu();
		addPopup(tree, popupMenu);
		
		popEdit = new JMenuItem("Edit");
		popupMenu.add(popEdit);
		popEdit.addActionListener(this);
		
		popDelete = new JMenuItem("Delete");
		popupMenu.add(popDelete);
		popDelete.addActionListener(this);
		
		
		scrollPane_1 = new JScrollPane();
		splitPane.setRightComponent(scrollPane_1);
		
		table = new JTable();
		table.setFillsViewportHeight(true);
		table.setShowVerticalLines(false);
		table.setBorder(new EmptyBorder(5, 0, 5, 0));
		table.setAutoCreateRowSorter(true);
		table.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent me){
				if(me.getClickCount() == 2){
					JTable tbl = (JTable) me.getSource();
					int row = tbl.getSelectedRow();
					if(row != -1){
						NoteTableModel nmodel = (NoteTableModel) table.getModel();
						NoteNode note = (NoteNode) nmodel.getValueAt(row, 0);
						openNoteEditor(note);
					}
				}
			}
		});
		scrollPane_1.setViewportView(table);
		
		//Toolbar
		toolBar_1 = new JToolBar();
		toolBar_1.setFloatable(false);
		contentPane.add(toolBar_1, BorderLayout.NORTH);
		
		btnAddGroup = new JButton("New Group");
		btnAddGroup.setIcon(new ImageIcon(JNotes.class.getResource("/com/balitechy/resources/folder_add_1.png")));
		toolBar_1.add(btnAddGroup);
		btnAddGroup.addActionListener(this);
		
		btnAddNote = new JButton("New Note");
		btnAddNote.setIcon(new ImageIcon(JNotes.class.getResource("/com/balitechy/resources/page_white_add_1.png")));
		toolBar_1.add(btnAddNote);
		btnAddNote.addActionListener(this);
		
		panel = new JPanel();
		panel.setBorder(new EmptyBorder(3, 5, 3, 5));
		contentPane.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		statusBar = new JLabel("Ready.");
		statusBar.setFont(new Font("Dialog", Font.PLAIN, 12));
		panel.add(statusBar);
		
		//Setup database
		dbu = new DBUtils();
		if(dbu.is_db_active()){
			resetStatus();
			allowOperation(true);
			populateGroupTree();
			populateNotes(0);
		}else{
			setStatus(DBUtils.INACTIVE_MSG);
			allowOperation(false);
		}
	}
	
	//DBUtils getter
	public DBUtils getDBUtils(){
		return dbu;
	}
	
	//Action handler
	public void actionPerformed(ActionEvent evt){
		Object s = evt.getSource();
		
		if(s == menuExit){
			closeProgram();
		}else if(s == menuAbout){
			showAbout();
		}else if(s == menuAddGroup || s == btnAddGroup){
			addNewGroup();
		}else if(s == menuAddNote || s == btnAddNote){
			openNoteEditor();
		}else if(s == popEdit){
			treeMenuEditAction();
		}else if(s == popDelete){
			treeMenuDeleteAction();
		}
	}
	
	// Add new group
	private void addNewGroup(){
		final String group = JOptionPane.showInputDialog(this, "Group name:");
		if(group != null && !group.isEmpty()){
			EventQueue.invokeLater(new Runnable(){
				public void run(){
					try{
						dbu.newGroup(group);
						populateGroupTree();
					}catch(Exception e){
						setStatus("Adding group failed!");
					}
				}
			});
		}
	}
	
	//Edit group
	private void editGroup(final int group_id, String current_group_title){
		final String new_title = JOptionPane.showInputDialog("Group name:", current_group_title);
		if(new_title != null && !new_title.isEmpty() && !new_title.equals(current_group_title)){
			EventQueue.invokeLater(new Runnable(){
				public void run(){
					try{
						dbu.updateGroup(group_id, new_title);
						populateGroupTree();
					}catch(Exception e){
						e.printStackTrace();
						setStatus("Update failed!");
					}
				}
			});
		}
	}
	
	//Delete Group
	private void deleteGroup(final int id, String title){
		int confirm = JOptionPane.showConfirmDialog(this, "Are you sure want to delete group '"+title+"'?");
		if(confirm == 0){
			EventQueue.invokeLater(new Runnable(){
				public void run(){
					try{
						dbu.deleteGroup(id);
						populateGroupTree();
					}catch(Exception e){
						e.printStackTrace();
						setStatus("Delete failed!");
					}
				}
			});
		}
	}
	
	//populate JTree
	public void populateGroupTree(){
		EventQueue.invokeLater(new Runnable(){
			public void run(){
				setStatus("Loading groups...");
				ResultSet groups = null;
				
				DefaultMutableTreeNode topNode = new DefaultMutableTreeNode("Notes");
				DefaultMutableTreeNode category = null;
				
				try{
					//Get all groups from database
					groups = dbu.allGroups();
					
					// Loop through groups 
					while(groups.next()){
						String group_title = groups.getString("title");
						int group_id = groups.getInt("id");
						
						category = new DefaultMutableTreeNode(new GroupNode(group_id, group_title));
						topNode.add(category);
					}
					tree.setModel(new DefaultTreeModel(topNode));
					resetStatus();
				}catch(Exception e){}
			}
		});

	}	
	
	//Populate notes into table
	private void populateNotes(final int group_id){
		EventQueue.invokeLater(new Runnable(){
			public void run(){
				setStatus("Loading notes...");
				try{
					NoteTableModel dmodel = new NoteTableModel(new Object[][] {}, new String[] {"Title", "Note"});
					table.setModel(dmodel);
					
					ResultSet notes;
					
					if(group_id == 0){
						notes = dbu.allNotes();
					}else{
						notes = dbu.notesByGroup(group_id);
					}
					
					while(notes.next()){
						NoteNode note = new NoteNode(
													notes.getInt("id"),
													notes.getString("title"),
													notes.getString("note"), 
													notes.getInt("group_id"));
						Vector<Object> newrow = new Vector<Object>();
						newrow.add(note);
						newrow.add(note.note_note);
						dmodel.addRow(newrow);
					}
				resetStatus();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}
	
	//run populate notes table
	public void runPopulateNotes(){
		
		try{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			
			if(node.isLeaf()){
				GroupNode nodeInfo = (GroupNode) node.getUserObject();
				populateNotes(nodeInfo.group_id);
			}else if(node.isRoot()){
				populateNotes(0);
			}
		}catch(Exception e){
			populateNotes(0);
		}
	}
	
	//Edit action on Tree pop menu
	private void treeMenuEditAction(){
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if(node == null) return;
		
		Object nodeInfo = node.getUserObject();
		String className = nodeInfo.getClass().getSimpleName();
		
		if(className.equals("GroupNode")){
			GroupNode group = (GroupNode) nodeInfo;
			editGroup(group.group_id, group.group_title);
		}
	}
	
	//Delete action on Tree pop menu
	private void treeMenuDeleteAction(){
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if(node == null) return;
		
		Object nodeInfo = node.getUserObject();
		String className = nodeInfo.getClass().getSimpleName();
		
		if(className.equals("GroupNode")){
			GroupNode group = (GroupNode) nodeInfo;
			deleteGroup(group.group_id, group.group_title);
		}else if(className.equals("NoteNode")){
			System.out.println("Deleting Note");
		}
	}
	
	//Open Note Editor | New mode
	private void openNoteEditor(){
		
		NoteEditor dialog = new NoteEditor(this);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}
	
	//Open Note Editor | Edit mode
	private void openNoteEditor(NoteNode note){
		
		NoteEditor dialog = new NoteEditor(this, note);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}
	
	//Status Bar setter thread
	public void setStatus(final String message){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				statusBar.setText(message);
			}
		});
	}

	//reset Status Bar setter thread
	public void resetStatus(){
		EventQueue.invokeLater(new Runnable(){
			public void run(){
				statusBar.setText("Ready.");
			}
		});
	}
	//Enable - Disabled operation
	private void allowOperation(final boolean tof){
		EventQueue.invokeLater(new Runnable(){
			public void run(){
				menuDatabase.setEnabled(tof);
				menuSync.setEnabled(tof);
				mnNote.setEnabled(tof);
				btnAddGroup.setEnabled(tof);
				btnAddNote.setEnabled(tof);
			}
		});
	}
	
	//Show About program
	private void showAbout(){
		String message = "JNotes is a simple note application for your daily activity.\n\n(c) 2012 Eka Putra";
		JOptionPane.showMessageDialog(this, message, "About JNotes", JOptionPane.INFORMATION_MESSAGE, null);
	}
	
	//Quit program
	private void closeProgram(){
		System.exit(0);
	}
	
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}
