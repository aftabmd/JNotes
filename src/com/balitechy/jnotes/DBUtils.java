package com.balitechy.jnotes;

import java.io.File;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtils{
	private Connection conn;
	private Statement stat;
	private boolean is_active;
	public static final String INACTIVE_MSG = "Database not active. Can't do any database operation.";
	
	public DBUtils(){
		setupDB();
	}
	
	private String getAppDir(){
		CodeSource codeSource = JNotes.class.getProtectionDomain().getCodeSource();
		File jarFile = null;
		try {
			jarFile = new File(codeSource.getLocation().toURI().getPath());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		String appDir = jarFile.getParentFile().getPath();
		return appDir;
	}
	
	private void setupDB(){
	
		try{
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:"+getAppDir()+File.separator+"jnotes.db");
			conn.setAutoCommit(true);
			is_active = true;
		}catch(Exception e){
			is_active = false;
		}
		
		//Create table category
		String sql_create_group = "CREATE TABLE groups" +
				" (id INTEGER PRIMARY KEY," +
				" title VARCHAR(30))";
		try{
			stat = conn.createStatement();
			stat.executeUpdate(sql_create_group);
			stat.close();
		} catch (SQLException e) {}
		
		//Create table note
		String sql_create_note = "CREATE TABLE notes" +
				" (id INTEGER PRIMARY KEY," +
				" title VARCHAR(30)," +
				" note TEXT," +
				" group_id INTEGER," +
				" FOREIGN KEY(group_id) REFERENCES groups(id))";
		try {
			stat = conn.createStatement();
			stat.executeUpdate(sql_create_note);
			stat.close();
		} catch (SQLException e) {}
	}
	
	// check if database is active
	public boolean is_db_active(){
		return is_active;
	}
	
	public ResultSet allGroups() throws Exception{
		stat = conn.createStatement();
		return stat.executeQuery("SELECT * FROM groups ORDER BY title");
	}
	
	public ResultSet allNotes() throws Exception{
		stat = conn.createStatement();
		return stat.executeQuery("SELECT * FROM notes ORDER BY title");
	}
	
	public ResultSet notesByGroup(int group_id) throws Exception{
		ResultSet result = null;
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM notes WHERE group_id=? ORDER BY title");
		ps.setInt(1, group_id);
		return ps.executeQuery();
	}
	
	public void newGroup(String name) throws Exception{
		PreparedStatement ps = conn.prepareStatement("INSERT INTO groups VALUES(null, ?)");
		ps.setString(1, name);
		ps.executeUpdate();
		ps.close();
	}
	
	public void updateGroup(int id, String new_title) throws Exception{
		PreparedStatement ps = conn.prepareStatement("UPDATE groups SET title=? WHERE id=?");
		ps.setString(1, new_title);
		ps.setInt(2, id);
		ps.executeUpdate();
		ps.close();
	}
	
	
	public void deleteGroup(int group_id) throws Exception{
		PreparedStatement ps = conn.prepareStatement("DELETE FROM groups WHERE id=?");
		ps.setInt(1, group_id);
		ps.executeUpdate();
		ps.close();
	}
	
	public int newNote(String title, String note, int group_id) throws Exception{
		PreparedStatement ps;
		int key = 0;

		ps = conn.prepareStatement("INSERT INTO notes VALUES(null, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
		ps.setString(1, title);
		ps.setString(2, note);
		ps.setInt(3, group_id);
		ps.executeUpdate();
		
		ResultSet keys = ps.getGeneratedKeys();
		keys.next();
		key = keys.getInt(1);
		ps.close();
		return key;
	}
	
	public void updateNote(String title, String note, int group_id, int note_id) throws Exception{
		PreparedStatement ps = conn.prepareStatement("UPDATE notes SET title=?, note=?, group_id=? WHERE id=?");
		ps.setString(1, title);
		ps.setString(2, note);
		ps.setInt(3, group_id);
		ps.setInt(4, note_id);
		ps.executeUpdate();
		ps.close();
	}
	
	public void deleteNote(int note_id) throws Exception{
		PreparedStatement ps = conn.prepareStatement("DELETE FROM notes WHERE id=?");
		ps.setInt(1, note_id);
		ps.executeUpdate();
		ps.close();
	}
}
