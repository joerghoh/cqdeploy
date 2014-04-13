package de.joerghoh.cq5.deploy.impl.checks;

import java.util.Dictionary;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Activate;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;


@Component()
@Service(value=BundleStatusChecker.class)
public class BundleStatusChecker {
	
	private Bundle thisBundle;
	
	
	@Activate
	protected void Activate (ComponentContext ctx) {
		thisBundle = ctx.getBundleContext().getBundle();
	}
	
	
	/**
	 * Check the status of all registered bundles
	 * @param deep if all contained services should be checked as well
	 * @return true if all bundles are up and all contained services are working correctly as well.
	 */
	public boolean allBundlesOk(boolean deep) {
		boolean result = true;
		Bundle[] allBundles = thisBundle.getBundleContext().getBundles();
		for (int i=0;i<allBundles.length;i++) {
			result = result & isBundleActive (allBundles[i]);
			if (deep) {
				result = result & areAllServicesActive(allBundles[i]);
			}
		}
		return result;
	}
	

	/**
	 * Checks if a bundle is active; fragment bundles are also
	 * considered "active"
	 * @param bundle
	 * @return
	 */
	public boolean isBundleActive(Bundle bundle) {
		boolean active = (bundle.getState() == Bundle.ACTIVE);
		Dictionary<String, String> headers = bundle.getHeaders();
        boolean isFragment = (headers.get("Fragment-Host") != null);
        
        return (active || isFragment);
	}
	
	
	
	private boolean areAllServicesActive (Bundle bundle) {
		
		boolean allServicesOk = true;
		BundleContext ctx = bundle.getBundleContext();
		ServiceReference[] refs = bundle.getRegisteredServices();
		for (int i=0;i<refs.length;i++) {
			ServiceReference ref = refs[i];
			Object myService = ctx.getService(ref);
			
			if (myService == null) {
				allServicesOk = false;
			}
			
			ctx.ungetService(ref);
		}
		return allServicesOk;
		
	}
	
	
}
