package com.push.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class JavaMailAuth extends Authenticator {
	String userName = null;
	String password = null;

	public JavaMailAuth() {
		
	}
	
	public JavaMailAuth(String username, String password) {
		this.userName = username;
		this.password = password;
	}

	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(userName, password);
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}