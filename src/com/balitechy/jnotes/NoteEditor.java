package com.balitechy.jnotes;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTextPane;
import javax.swing.ImageIcon;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JComboBox;

public class NoteEditor extends JDialog implements ActionListener{
	private JTextField editTitle;
	private int mode;
	private JNotes parent;
	private JComboBox<GroupNode> selectGroup;
	private JTextPane textNote;
	private JButton btnEdit, btnSave, btnDelete, btnCancel, btnClose;
	private NoteNode editedNote = null;
	
	//Constructor for New note mode
	/**
	 * @wbp.parser.constructor
	 */
	public NoteEditor(JNotes p) {
		parent = p;
		mode = 0;
		setupUI();
		setInitialData();
		setUINewMode();
	}
	
	//Constructor for Edit note mode
	public NoteEditor(JNotes p, NoteNode note){
		parent = p;
		mode = 1;
		editedNote = note;
		setupUI();
		setInitialData();
		setUIViewMode();
	}
	
	private void setupUI(){
		if(mode == 1){
			setTitle("Edit Note");
		}else{
			setTitle("New Note");
		}
		setResizable(false);
		setModal(true);
		setBounds(100, 100, 420, 340);
		getContentPane().setLayout(new BorderLayout(0, 0));
		{
			JToolBar toolBar = new JToolBar();
			toolBar.setFloatable(false);
			getContentPane().add(toolBar, BorderLayout.NORTH);
		
			btnEdit = new JButton("Edit");
			btnEdit.setIcon(new ImageIcon(NoteEditor.class.getResource("/com/balitechy/resources/page_white_edit.png")));
			toolBar.add(btnEdit);
			btnEdit.addActionListener(this);

			btnSave = new JButton("Save");
			btnSave.setIcon(new ImageIcon(NoteEditor.class.getResource("/com/balitechy/resources/save_as.png")));
			toolBar.add(btnSave);
			btnSave.addActionListener(this);
			
			btnCancel = new JButton("Cancel");
			btnCancel.setIcon(new ImageIcon(NoteEditor.class.getResource("/com/balitechy/resources/cancel.png")));
			toolBar.add(btnCancel);
			btnCancel.addActionListener(this);

			btnDelete = new JButton("Delete");
			btnDelete.setIcon(new ImageIcon(NoteEditor.class.getResource("/com/balitechy/resources/cross.png")));
			toolBar.add(btnDelete);
			btnDelete.addActionListener(this);

		}
		{
			JPanel panel = new JPanel();
			getContentPane().add(panel, BorderLayout.CENTER);
			panel.setLayout(null);
			
			JLabel lblTitle = new JLabel("Title");
			lblTitle.setBounds(12, 12, 70, 15);
			panel.add(lblTitle);
			
			JLabel lblGroup = new JLabel("Group");
			lblGroup.setBounds(12, 43, 70, 15);
			panel.add(lblGroup);
			
			editTitle = new JTextField();
			editTitle.setBounds(92, 6, 316, 28);
			panel.add(editTitle);
			editTitle.setColumns(10);
			
			selectGroup = new JComboBox<GroupNode>();
			selectGroup.setBounds(92, 38, 314, 24);
			panel.add(selectGroup);
			
			btnClose = new JButton("Close");
			btnClose.setBounds(323, 237, 83, 24);
			panel.add(btnClose);
			
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(12, 74, 394, 151);
			panel.add(scrollPane);
			
			textNote = new JTextPane();
			scrollPane.setViewportView(textNote);
			btnClose.addActionListener(this);
		}
	}
	
	private void setInitialData(){
		//Fill initial data if exist
		if(mode == 1){
			editTitle.setText(editedNote.note_title);
			textNote.setText(editedNote.note_note);
		}
		
		try{
			DefaultComboBoxModel<GroupNode> cbModel = (DefaultComboBoxModel<GroupNode>) selectGroup.getModel();
			cbModel.removeAllElements();
			
			GroupNode selectedGroup = null;
			ResultSet groups = parent.getDBUtils().allGroups();
			while(groups.next()){
				GroupNode item = new GroupNode(groups.getInt("id"), groups.getString("title"));
				selectGroup.addItem(item);
				//If in edit mode
				if(mode == 1){
					if(item.group_id == editedNote.note_group){
						selectedGroup = item;
					}
				}
			}
			
			//set initial group when available
			if(selectedGroup != null){
				selectGroup.setSelectedItem(selectedGroup);
			}

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	

	@Override
	public void actionPerformed(ActionEvent evt) {
		Object s = evt.getSource();
		
		if(s == btnSave){
			saveNote();
		}else if(s == btnEdit){
			setUIEditMode();
		}else if(s == btnCancel){
			cancelEditing();
		}else if(s == btnDelete){
			deleteNote();
		}else if(s == btnClose){
			dispose();
		}
	}
	
	//Save Note
	private void saveNote(){
		final String title = editTitle.getText();
		final String note = textNote.getText();
		final int group_id;
		
		//Validate note title
		if(title.isEmpty()){
			JOptionPane.showMessageDialog(this, "Title can't be empty!", "Error", JOptionPane.OK_OPTION, null);
			return;
		}
		
		//don't run if selected index = -1 (no item selected)
		if(selectGroup.getSelectedIndex() != -1){
			GroupNode obj = (GroupNode) selectGroup.getSelectedItem();
			group_id = obj.group_id;
		}else{
			JOptionPane.showMessageDialog(this, "Please select note's group!", "Error", JOptionPane.OK_OPTION, null);
			return;
		}
		
		EventQueue.invokeLater(new Runnable(){
			public void run(){
				try{
					//if save new note
					if(editedNote == null){
						int key = parent.getDBUtils().newNote(title, note, group_id);
						editedNote = new NoteNode(key, title, note, group_id);
					}else{
						parent.getDBUtils().updateNote(title, note, group_id, editedNote.note_id);
					}
					setUIViewMode();
					parent.runPopulateNotes();
				}catch(Exception e){
					e.printStackTrace();
					parent.setStatus("Failed saving note!");
				}
			}
		});
	}
	
	//Cancel editing
	private void cancelEditing(){
		setInitialData();
		setUIViewMode();
	}
	
	//Delete this note
	private void deleteNote(){
		int confirm = JOptionPane.showConfirmDialog(this, "Are you sure want to delete this note?");
		if(confirm == 0){
			EventQueue.invokeLater(new Runnable(){
				public void run(){
					try{
						parent.getDBUtils().deleteNote(editedNote.note_id);
						parent.runPopulateNotes();
					}catch(Exception e){
						e.printStackTrace();
					}finally{
						NoteEditor.this.dispose();
					}
				}
			});
		}
	}
	
	//State add new
	private void setUINewMode(){
		btnEdit.setVisible(false);
		btnDelete.setVisible(false);
		btnCancel.setVisible(false);
	}
	
	//state view | not-editable
	private void setUIViewMode(){
		btnEdit.setVisible(true);
		btnDelete.setVisible(true);
		btnCancel.setVisible(false);
		btnSave.setVisible(false);
		editTitle.setEditable(false);
		textNote.setEditable(false);
		selectGroup.setEnabled(false);
	}
	
	//State edit
	private void setUIEditMode(){
		btnEdit.setVisible(false);
		btnDelete.setVisible(false);
		btnCancel.setVisible(true);
		btnSave.setVisible(true);
		editTitle.setEditable(true);
		textNote.setEditable(true);
		selectGroup.setEnabled(true);
	}
}
