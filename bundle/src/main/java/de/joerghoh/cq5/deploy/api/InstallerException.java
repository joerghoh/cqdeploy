package de.joerghoh.cq5.deploy.api;


/**
 * This exception type is used to signal exceptions by the Package Installer 
 *
 */
public class InstallerException extends Exception {

	public InstallerException (String message) {
		super (message);
	}
	
	public InstallerException (String message, Throwable t ) {
		super (message, t);
	}
	
}
