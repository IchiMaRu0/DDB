package transaction;

import java.rmi.*;

/** 
 * Interface for the Transaction Manager of the Distributed Travel
 * Reservation System.
 * <p>
 * Unlike WorkflowController.java, you are supposed to make changes
 * to this file.
 */

public interface TransactionManager extends Remote {
    public static final String RMIName = "TM";
    public boolean dieNow() throws RemoteException;

    public void ping() throws RemoteException;
    
	public void enlist(int xid, ResourceManager rm) throws RemoteException, InvalidTransactionException;

    public int start() throws RemoteException;

    public boolean commit(int xid) throws RemoteException, InvalidTransactionException, TransactionAbortedException;

    public void abort(int xid) throws RemoteException, InvalidTransactionException;

    public void setDieTime(String time) throws RemoteException;
    /** The RMI name a TransactionManager binds to. */

    public enum TransactionState{
        NEW, PREPARED, COMMITTED, ABORTED
    }
}
