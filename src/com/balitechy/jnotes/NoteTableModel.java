package com.balitechy.jnotes;

import javax.swing.table.DefaultTableModel;

public class NoteTableModel extends DefaultTableModel{
	
	public NoteTableModel(Object[][] objects, String[] strings) {
		super(objects, strings);
	}
	
	public boolean isCellEditable(int row, int col) {
		return false;
    }
}
