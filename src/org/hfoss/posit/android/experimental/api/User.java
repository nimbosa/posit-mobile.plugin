/*
 * File: User.java
 * 
 * Copyright (C) 2011 The Humanitarian FOSS Project (http://www.hfoss.org)
 * 
 * This file is part of the ACDI/VOCA plugin for POSIT, Portable Open Source 
 * and Information Tool.
 *
 * This plugin is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) as published 
 * by the Free Software Foundation; either version 3.0 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU LGPL along with this program; 
 * if not visit http://www.gnu.org/licenses/lgpl.html.
 * 
 */

package org.hfoss.posit.android.experimental.api;


import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hfoss.posit.android.experimental.R;
import org.hfoss.posit.android.experimental.api.database.DbManager;
import org.hfoss.posit.android.experimental.plugin.acdivoca.AcdiVocaUser;
import org.hfoss.posit.android.experimental.plugin.acdivoca.AcdiVocaUser.UserType;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * 
 * The User object for creating and persisting data for the 
 * user table in the database. This is used with the Login
 * Activity, which is meant for apps where multiple users 
 * share the same phone. 
 */
public class User {

	public static final String TAG = "User";

	public enum UserType {
		ADMIN, USER
	};

	public static final String USER_TYPE_STRING = "UserType";
	public static final String USER_TYPE_KEY = "UserLoginType";

	/**
	 * Default user accounts.
	 */
	public static final String USER_DEFAULT_NAME = "b"; // For testing purposes
	public static final String USER_DEFAULT_PASSWORD = "b";
	public static final String ADMIN_USER_NAME = "r";
	public static final String ADMIN_USER_PASSWORD = "a";

	/**
	 * The fields annotated with @DatabaseField are persisted to the Db.
	 */
	// id is generated by the database and set on the object automagically
	@DatabaseField(generatedId = true)
	int id;
	@DatabaseField(uniqueIndex = true)
	String name;
	@DatabaseField
	String password;
	@DatabaseField
	int type;

	User() {
		// needed by ormlite
	}

	public User(String name, String password, int type) {
		this.name = name;
		this.password = password;
		this.type = type;
	}

	/**
	 * Creates the table associated with this object. And creates the default
	 * users. The table's name is 'user', same as the class name.
	 * 
	 * @param connectionSource
	 */
	public static void createTable(ConnectionSource connectionSource,
			Dao<User, Integer> dao) {
		try {
			TableUtils.createTable(connectionSource, User.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Create some users
		if (!insertUser(dao, ADMIN_USER_NAME, ADMIN_USER_PASSWORD,
				UserType.ADMIN))
			Log.e(TAG, "Error adding user = " + ADMIN_USER_NAME);
		if (!insertUser(dao, USER_DEFAULT_NAME, USER_DEFAULT_PASSWORD,
				UserType.USER))
			Log.e(TAG, "Error adding user = " + USER_DEFAULT_NAME);

		Log.i(TAG, "Created User Accounts");

	}

	/**
	 * Inserts new users into the database given the username, password, and
	 * type. Uses ORMlite's DAO.
	 * 
	 * @param username
	 * @param password
	 * @param usertype
	 *            one of SUPER, ADMIN, USER
	 * @return
	 */
	private static boolean insertUser(Dao<User, Integer> userDao,
			String username, String password, UserType usertype) {
		Log.i(TAG, "insertUser " + username + " of type = " + usertype);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("name", username);

		// Query for the username in the user table
		List<User> list = null;
		try {
			list = userDao.queryForFieldValues(map);
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}

		// If the user doesn't already exist, insert it.
		if (list.size() == 0) {
			try {
				userDao.create(new User(username, password, usertype.ordinal()));
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	/**
	 * Utility method to display the list of users in the acdivocauser table.
	 */
	private static void displayUsers(Dao<User, Integer> avUserDao) {
		Log.i(TAG, "Displaying user table");

		List<User> list = null;
		try {
			list = avUserDao.queryForAll();
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}

		for (User item : list) {
			Log.i(TAG, item.toString());
		}
	}

	public static int authenticateUser(Context context, String username, String password, UserType rqdUserType, Dao<User, Integer> userDao) {
		Log.i(TAG, "Authenticating user = " + username + " Access type = " + rqdUserType);		
		
		//DbManager db = new DbManager(context);
		//Dao<User, Integer> userDao = null;
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("name", username);
		map.put("password", password);
		List<User> list = null;
		int result = 0;
		int userType = 0;
		try {
			//userDao = db.getUserDao();
			list = userDao.queryForFieldValues(map);
		} catch (SQLException e) {
			Log.e(TAG, "SQL Exception " + e.getMessage());
			e.printStackTrace();
		}
		Log.i(TAG, "List size = " + list.size());
		if (list.size() != 1) 
			result =  -1;
		else {
			User user = list.get(0);
			userType = user.type;
			result = userType;
			Log.i(TAG, "User type = " + userType);
			switch (rqdUserType) {
			case ADMIN:
				if (userType != rqdUserType.ordinal()) {
					Log.i(TAG, "Sorry you must be ADMIN USER to do this.");
					Toast.makeText(context, context.getString(R.string.toast_adminuser), Toast.LENGTH_SHORT);
					result = -1;
				}
				break;
	
			}			
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("id=").append(id);
		sb.append(", ").append("name=").append(name);
		sb.append(", ").append("password=").append(password);
		sb.append(", ").append("type=").append(type);
		return sb.toString();
	}
}
