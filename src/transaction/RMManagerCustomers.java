/*
 * Created on 2005-5-29
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package transaction;

import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RMManagerCustomers {
	static Registry _rmiRegistry = null;

	public static void main(String[] args) {
		String rmiName = ResourceManager.RMINameCustomers;
		if (rmiName == null || rmiName.equals("")) {
			System.err.println("No RMI name given");
			System.exit(1);
		}
		String rmiPort = Util.getRMIPort(rmiName);
		if (rmiPort == null || rmiPort.equals("")) {
			System.err.println("No RMI port given");
			System.exit(1);
		}
		
		try {
			_rmiRegistry = LocateRegistry.createRegistry(Integer.parseInt(rmiPort));
		} catch (RemoteException e2) {
			e2.printStackTrace();
			return;
		}

		try {
			ResourceManagerImpl obj = new ResourceManagerImpl(rmiName);
			_rmiRegistry.bind(rmiName, obj);
			System.out.println(rmiName + " bound");
		} catch (Exception e) {
			System.err.println(rmiName + " not bound:" + e);
			System.exit(1);
		}

	}
}
