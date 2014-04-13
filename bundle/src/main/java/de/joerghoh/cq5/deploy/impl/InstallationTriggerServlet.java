package de.joerghoh.cq5.deploy.impl;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.joerghoh.cq5.deploy.api.InstallerException;
import de.joerghoh.cq5.deploy.api.PackageInstaller;
import de.joerghoh.cq5.deploy.impl.checks.BundleStatusChecker;

@SlingServlet ( paths = {"/bin/installtrigger"})
public class InstallationTriggerServlet extends SlingSafeMethodsServlet {

	private static final Logger LOG = LoggerFactory.getLogger(InstallationTriggerServlet.class);

	@Reference
	PackageInstaller installer;

	@Reference
	BundleStatusChecker checker;

	/**
	 * 
	 */
	private static final long serialVersionUID = 8540363447712840243L;

	protected void doGet (SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
		try {
			response.setStatus(200);
			response.setContentType("text/html");
			printHeader(response.getWriter());
			response.getWriter().print (installer.installPackages() + "</br/><br/>");
			boolean status = checker.allBundlesOk(false);
			if (status) {
				response.getWriter().print ("Bundle status: all ok<br/>");
			} else {
				response.getWriter().print ("Bundle status: not ok<br/>");
			}

			printFooter (response.getWriter());

		} catch (InstallerException e) {
			LOG.error("Exception",e);
		} 

	}


	private void printHeader (final PrintWriter writer ) {
		writer.print ("<html><body>");
		writer.print ("<b>Installtrigger </b><br/>");
	}

	private void printFooter (final PrintWriter writer) {
		writer.print ("</bdoy></html>");
	}

}
