package com.balitechy.jnotes;

public class NoteNode {
	public int note_id;
	public int note_group;
	public String note_title;
	public String note_note;
	
	public NoteNode(int id, String title, String note, int group_id){
		note_id = id;
		note_title = title;
		note_note = note;
		note_group = group_id;
	}
	
	public String toString(){
		return note_title;
	}
}
