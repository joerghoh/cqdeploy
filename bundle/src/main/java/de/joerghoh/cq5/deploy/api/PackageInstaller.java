package de.joerghoh.cq5.deploy.api;

import java.io.File;
import java.io.IOException;

import javax.jcr.RepositoryException;

import com.day.jcr.vault.packaging.PackageException;

/**
 * The PackageInstaller service allows you to trigger the package upload from a configured
 * location on the server-side.
 * 
 *
 */

public interface PackageInstaller {

	/**
	 * Install all CQ content packages sitting in the specified directory
	 * @param path the directory
	 */
	public String installPackages () throws InstallerException;
	
	
	public void validateBundleStatus();
	
}
