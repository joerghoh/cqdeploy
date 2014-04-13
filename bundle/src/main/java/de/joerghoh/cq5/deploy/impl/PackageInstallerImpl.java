package de.joerghoh.cq5.deploy.impl;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.FileUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.jcr.vault.fs.io.ImportOptions;
import com.day.jcr.vault.packaging.JcrPackage;
import com.day.jcr.vault.packaging.JcrPackageManager;
import com.day.jcr.vault.packaging.PackageException;
import com.day.jcr.vault.packaging.Packaging;

import de.joerghoh.cq5.deploy.api.InstallerException;
import de.joerghoh.cq5.deploy.api.PackageInstaller;


@Component(metatype=false,immediate=true)
@Service
public class PackageInstallerImpl implements PackageInstaller {

	final static Logger LOG = LoggerFactory.getLogger(PackageInstallerImpl.class);

	@Reference
	SlingRepository repo;
	Session adminSession;

	@Reference
	Packaging packaging;


	private static final String DEFAULT_PACKAGE_ROOT = "packages";
	@Property(label="content package path")
	private static final String PROPERTY_PACKAGE_ROOT = "package.root";
	String packageRoot;


	@Activate
	protected void activate (ComponentContext ctx)  {
		packageRoot = PropertiesUtil.toString(ctx.getProperties().get(PROPERTY_PACKAGE_ROOT), DEFAULT_PACKAGE_ROOT);

		final File directory = new File (packageRoot);

		if (directory.exists() && directory.isDirectory()) {
			LOG.info("Looking for packages in {}",directory.getAbsolutePath());
			try {
				//installPackages (directory);
			} catch (Exception e) {
				LOG.error("Got exception during service activation",e);
			}
		} else {
			LOG.warn ("Package root directory {} does not exist or is not a directory", directory.getAbsolutePath());
		}


	}


	public String installPackages () throws InstallerException {
		return installPackages (new File (packageRoot));
	}


	private String installPackages (File directory) throws InstallerException  {
		
		final StringBuilder outputBuffer = new StringBuilder();
		int count = 0;
		Iterator<File> files = getFiles (directory);
		while (files.hasNext()) {

			final File f = files.next();
			LOG.info("Checking file " + f.getAbsolutePath());
			try {
				if (shouldInstall(f)) {
					installSinglePackage (f);
					count++;
					outputBuffer.append("installed package " + f.getName());
				}
			} catch (IOException e) {
				throw new InstallerException ("Exception when installing file " + f.getName(),e);
			} catch (RepositoryException e) {
				throw new InstallerException ("Exception when installing file " + f.getName(),e);
			} catch (PackageException e) {
				throw new InstallerException ("Exception when installing file " + f.getName(),e);
			}

		}
		if (count > 0) {
			LOG.info("Finished installing {} packages",count);
		} else {
			LOG.info("Found no packages for installation");
		}
		return outputBuffer.toString();
	}


	private Iterator<File> getFiles (File directory) {
		return FileUtils.iterateFiles (directory, new String[] {"zip"},true);
	}


	private void installSinglePackage (File file) throws RepositoryException, PackageException, IOException {

		Session adminSession = null;
		try {
			adminSession = repo.loginAdministrative(null);
			JcrPackageManager packman = packaging.getPackageManager(adminSession);

			JcrPackage pack = packman.upload(file,false,true,"");
			ImportOptions options = new ImportOptions();
			pack.install(options); // all default
			markAsInstalled(file);
			LOG.info("Package {} successfully installed", file.getName());
		} finally {
			if (adminSession != null && adminSession.isLive()) {
				adminSession.logout();
			}
		}
	}

	private boolean shouldInstall (File file) throws IOException {

		File timestampFile = getTimestampFile(file);
		if (!timestampFile.exists()) {
			return true;
		}
		return file.lastModified() > timestampFile.lastModified();

	}

	private void markAsInstalled (File file) throws IOException {
		File timestampFile = getTimestampFile(file);
		if (timestampFile.exists()) {
			timestampFile.setLastModified(System.currentTimeMillis());
		} else {
			timestampFile.createNewFile();
		}
	} 

	private File getTimestampFile (File orig) {
		return new File (orig.getAbsolutePath() + ".timestamp");
	}


	public void validateBundleStatus() {
		// TODO Auto-generated method stub

	}

}
