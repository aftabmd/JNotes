package com.balitechy.jnotes;

public class GroupNode {
	public int group_id;
	public String group_title;
	
	public GroupNode(int id, String title){
		group_id = id;
		group_title = title;
	}
	
	public String toString(){
		return group_title;
	}
}
