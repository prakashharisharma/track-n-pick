package com.example.controller.model;

public class Person {

	String name;
	String email;
	String message;
	
	public Person() {
		super();
	}
	
	public Person(String name, String email, String message) {
		super();
		this.name = name;
		this.email = email;
		this.message = message;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	@Override
	public String toString() {
		return "Person [name=" + name + ", email=" + email + ", message=" + message + "]";
	}
	
	
}
