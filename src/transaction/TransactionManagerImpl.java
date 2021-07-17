package transaction;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/** 
 * Transaction Manager for the Distributed Travel Reservation System.
 * 
 * Description: toy implementation of the TM
 */

public class TransactionManagerImpl extends java.rmi.server.UnicastRemoteObject implements TransactionManager {
	static Registry _rmiRegistry = null;


    public static void main(String args[]) {
	//	System.setSecurityManager(new RMISecurityManager());
		String rmiName = TransactionManager.RMIName;
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
	    TransactionManagerImpl obj = new TransactionManagerImpl();
	    _rmiRegistry.bind(rmiName, obj);
	    System.out.println("TM bound");
	} 
	catch (Exception e) {
	    System.err.println("TM not bound:" + e);
	    System.exit(1);
	}
    }
    
	public void ping() throws RemoteException {
	}
    
	public void enlist(int xid, ResourceManager rm) throws RemoteException {
	}
	
    public TransactionManagerImpl() throws RemoteException {
    }

    public boolean dieNow() 
	throws RemoteException {
	System.exit(1);
	return true; // We won't ever get here since we exited above;
	             // but we still need it to please the compiler.
    }

}
