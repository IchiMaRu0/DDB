package transaction;

import java.io.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

/** 
 * Transaction Manager for the Distributed Travel Reservation System.
 * 
 * Description: toy implementation of the TM
 */

public class TransactionManagerImpl extends java.rmi.server.UnicastRemoteObject implements TransactionManager {
	static Registry _rmiRegistry = null;
	private final static String TM_XID_LOG = "data/tm_xid.log";
	private final static String TM_XID2STATE_LOG = "data/tm_xid2state.log";
	private final static String TM_XID2RMS_LOG = "data/tm_xid2rms.log";
	private  Integer xid;
	private Map<Integer, TransactionState> xid2state;
	private Map<Integer, Set<ResourceManager>> xid2rms;
	private String dieTime;

	@Override
	public int start() throws RemoteException {
		synchronized (xid){
			Integer curXid = xid++;
			persist(TM_XID_LOG, xid);

			xid2state.put(curXid, TransactionState.NEW);
			persist(TM_XID2STATE_LOG, xid2state);

			xid2rms.put(curXid, new HashSet<>());
			persist(TM_XID2RMS_LOG, xid2rms);

			return curXid;
		}


	}

	@Override
	public boolean commit(int xid) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
		if (!xid2state.containsKey(xid)) {
			throw new TransactionAbortedException(xid, "bad xid");
		}

		// write the prepare log to disk and mark xid PREPARED
		xid2state.put(xid, TransactionState.PREPARED);
		persist(TM_XID2STATE_LOG, xid2state);

		Set<ResourceManager> resourceManagers = xid2rms.get(xid);
		for (ResourceManager resourceManager : resourceManagers) {
			try {
				boolean prepared = resourceManager.prepare(xid);
				if (!prepared) {
					abort(xid);
					throw new TransactionAbortedException(xid, "one rm not prepared");
				}
			} catch (Exception e) {
				// occur when RM die before prepare
				abort(xid);
				throw new TransactionAbortedException(xid, "one rm down before prepared detected");
			}
		}


		if (dieTime.equals("BeforeCommit")) {
			dieNow();
		}

		// write the prepare log to disk and mark xid COMMITTED
		synchronized (xid2state) {
			xid2state.put(xid, TransactionState.COMMITTED);
			persist(TM_XID2STATE_LOG, xid2state);
		}
		if (dieTime.equals("AfterCommit")) {
			System.out.println("going to die...");
			dieNow();
		}

		Set<ResourceManager> committedRMs = new HashSet<>();
		for (ResourceManager resourceManager : resourceManagers) {
			try {
				System.out.println("send commit to rm: " + resourceManager.getID());
				resourceManager.commit(xid);
				committedRMs.add(resourceManager);
			} catch (Exception e) {
				System.out.println("one rm down before commit detected");
			}
		}
		if (committedRMs.size() == resourceManagers.size()) {
			xid2rms.remove(xid);
			persist(TM_XID2RMS_LOG, xid2rms);

			// all rm committed, transaction finalize
			xid2state.remove(xid);
			persist(TM_XID2STATE_LOG, xid2state);
		} else {
			synchronized (xid2rms) {
				resourceManagers.removeAll(committedRMs);
				xid2rms.put(xid, resourceManagers);
				persist(TM_XID2RMS_LOG, xid2rms);
			}
		}
		return true;
	}

	@Override
	public void abort(int xid) throws RemoteException, InvalidTransactionException {
		if (!xid2state.containsKey(xid)) {
			throw new InvalidTransactionException(xid, "TM abort");
		}

		xid2state.put(xid, TransactionState.ABORTED);
		persist(TM_XID2STATE_LOG, xid2state);

		Set<ResourceManager> resourceManagers = xid2rms.get(xid);

		for (ResourceManager resourceManager : resourceManagers) {
			try {
				System.out.println("send abort to " + resourceManager.getID());
				resourceManager.abort(xid);

			} catch (Exception e) {
				System.out.println("one rm down after prepare detected: ");
			}
		}

		xid2rms.remove(xid);
		persist(TM_XID2RMS_LOG, xid2rms);

		xid2state.remove(xid);
		persist(TM_XID2STATE_LOG, xid);
	}

	@Override
	public void setDieTime(String time) throws RemoteException {
		dieTime = time;
	}

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
    
	public void enlist(int xid, ResourceManager rm) throws RemoteException, InvalidTransactionException {

		if (!xid2rms.containsKey(xid)) {
			rm.abort(xid);
			return;
		}
		synchronized (xid2state){
			TransactionState xState = xid2state.get(xid);

			// rm down before commit then recover
			if (xState.equals(TransactionState.ABORTED)){
				rm.abort(xid);
				return;
			}
			else if (xState.equals(TransactionState.COMMITTED)) {
				// rm down before commit then recover
				rm.commit(xid);
				System.out.println("receive enlist from: " + rm.getID());

				synchronized (xid2rms) {
					Set<ResourceManager> rms = xid2rms.get(xid);
					rms.remove(rm);
					System.out.println(xid + "now has "+ rms.size() + "rms" );
					// tm receive ack from all rms
					if (rms.size() == 0) {
						xid2rms.remove(xid);
						persist(TM_XID2RMS_LOG, xid2rms);

						xid2state.remove(xid);
						persist(TM_XID2STATE_LOG, xid2state);
					} else {
						persist(TM_XID2RMS_LOG, xid2rms);
					}
				}
				return;
			}else{
				synchronized (xid2rms) {
					Set<ResourceManager> rms = xid2rms.get(xid);
					rms.add(rm);
					persist(TM_XID2RMS_LOG, xid2rms);
				}
			}
		}


	}
	
    public TransactionManagerImpl() throws RemoteException {
		xid = 1;
		xid2state = new Hashtable<>();
		xid2rms =  new Hashtable<>();
		dieTime = "NoDie";
		recover();
    }

    public boolean dieNow() throws RemoteException {
		System.exit(1);
		return true;
    }

	private synchronized void persist(String filePath, Object obj) {
		File file = new File(filePath);
		file.getParentFile().mkdirs();
		try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))){
			oos.writeObject(obj);
		}catch (Exception e){
			e.printStackTrace();
		}

	}

	private Object loadCache(String filePath) {
		File file = new File(filePath);
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))){
			return ois.readObject();
		} catch (Exception e) {
			return null;
		}
	}
	private void recover() {
		Object cacheXid = loadCache(TM_XID_LOG);
		if (cacheXid != null) {
			xid = (Integer) cacheXid;
		}

		Object cacheXid2state = loadCache(TM_XID2STATE_LOG);
		if (cacheXid != null) {
			xid2state = (Map<Integer, TransactionState>) cacheXid2state;
		}

		Object cacheXid2rms = loadCache(TM_XID2RMS_LOG);
		if (cacheXid2rms != null) {
			xid2rms = (Map<Integer, Set<ResourceManager>>) cacheXid2rms;
		}

		// if TM die, all related xids must be aborted
		// except for those who have committed: dieTMAfterCommit
		Set<Integer> keys = xid2state.keySet();
		for (Integer key : keys) {
			if (!xid2state.get(key).equals(TransactionState.COMMITTED)) {
				xid2state.put(key, TransactionState.ABORTED);
			}
		}

	}
}
