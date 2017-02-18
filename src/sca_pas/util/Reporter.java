package sca_pas.util;

// Pseudo log interface
public class Reporter {
	private String entity;
	
	public Reporter() {
		this("MAIN");
	}
	
	public Reporter(String entity) {
		this.entity = entity;
	}
	
	public void log(String message) {
		StringBuilder buil = new StringBuilder();
		buil.append(entity);
		buil.append(": ");
		buil.append(message);
		System.out.println(buil.toString());
	}
	
	public void log(String message, Exception exception) {
		StringBuilder buil = new StringBuilder();
		buil.append(entity);
		buil.append(": ");
		buil.append(message);
		buil.append('\n');
		buil.append(exception.getMessage());
		System.out.println(buil.toString());
	}
}
