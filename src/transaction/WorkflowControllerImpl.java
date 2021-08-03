package transaction;

import lockmgr.DeadlockException;
import transaction.entity.Car;
import transaction.entity.Flight;
import transaction.entity.Hotel;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Set;

/**
 * Workflow Controller for the Distributed Travel Reservation System.
 * <p>
 * Description: toy implementation of the WC.  In the real
 * implementation, the WC should forward calls to either RM or TM,
 * instead of doing the things itself.
 */

public class WorkflowControllerImpl extends java.rmi.server.UnicastRemoteObject implements WorkflowController {

    protected int flightcounter, flightprice, carscounter, carsprice, roomscounter, roomsprice;
    protected int xidCounter;

    protected ResourceManager rmFlights = null;
    protected ResourceManager rmRooms = null;
    protected ResourceManager rmCars = null;
    protected ResourceManager rmCustomers = null;
    protected TransactionManager tm = null;
    static Registry _rmiRegistry = null;

    protected Set<Integer> xids;

    public static void main(String args[]) {

//		System.setSecurityManager(new RMISecurityManager());
        String rmiName = WorkflowController.RMIName;
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
            WorkflowControllerImpl obj = new WorkflowControllerImpl();
            _rmiRegistry.bind(rmiName, obj);
            System.out.println("WC bound");
        } catch (Exception e) {
            System.err.println("WC not bound:" + e);
            System.exit(1);
        }
    }


    public WorkflowControllerImpl() throws RemoteException {
        flightcounter = 0;
        flightprice = 0;
        carscounter = 0;
        carsprice = 0;
        roomscounter = 0;
        roomsprice = 0;
        flightprice = 0;

        xidCounter = 1;

        while (!reconnect()) {
            // would be better to sleep a while
        }
    }


    // TRANSACTION INTERFACE
    public int start() throws RemoteException {
        int xid = tm.start();
        xids.add(xid);
        return xid;
    }

    public boolean commit(int xid)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        System.out.println("Committing");
        return true;
    }

    public void abort(int xid)
            throws RemoteException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "abort");
        tm.abort(xid);
        xids.remove(xid);
    }


    // ADMINISTRATIVE INTERFACE
    public boolean addFlight(int xid, String flightNum, int numSeats, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        flightcounter += numSeats;
        flightprice = price;
        return true;
    }

    public boolean deleteFlight(int xid, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        flightcounter = 0;
        flightprice = 0;
        return true;
    }

    public boolean addRooms(int xid, String location, int numRooms, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        roomscounter += numRooms;
        roomsprice = price;
        return true;
    }

    public boolean deleteRooms(int xid, String location, int numRooms)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        roomscounter = 0;
        roomsprice = 0;
        return true;
    }

    public boolean addCars(int xid, String location, int numCars, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        carscounter += numCars;
        carsprice = price;
        return true;
    }

    public boolean deleteCars(int xid, String location, int numCars)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        carscounter = 0;
        carsprice = 0;
        return true;
    }

    public boolean newCustomer(int xid, String custName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        return true;
    }

    public boolean deleteCustomer(int xid, String custName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        return true;
    }


    // QUERY INTERFACE
    public int queryFlight(int xid, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "queryFlight");
        if (flightNum == null)
            return -1;
        try {
            String rmiName = rmFlights.getID();
            ResourceItem resourceItem = rmFlights.query(xid, rmiName, flightNum);
            if (resourceItem == null)
                return -1;
            int numAvail = ((Flight) resourceItem).getNumAvail();
            return numAvail;
        } catch (DeadlockException e) {
            abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
    }

    public int queryFlightPrice(int xid, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "queryFlightPrice");
        if (flightNum == null)
            return -1;
        try {
            String rmiName = rmFlights.getID();
            ResourceItem resourceItem = rmFlights.query(xid, rmiName, flightNum);
            if (resourceItem == null)
                return -1;
            int price = ((Flight) resourceItem).getPrice();
            return price;
        } catch (DeadlockException e) {
            abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
    }

    public int queryRooms(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "queryRooms");
        if (location == null)
            return -1;
        try {
            String rmiName = rmRooms.getID();
            ResourceItem resourceItem = rmRooms.query(xid, rmiName, location);
            if (resourceItem == null)
                return -1;
            int numAvail = ((Hotel)resourceItem).getNumAvail();
            return numAvail;
        } catch (DeadlockException e) {
            abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
    }

    public int queryRoomsPrice(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "queryRoomsPrice");
        if (location == null)
            return -1;
        try {
            String rmiName = rmRooms.getID();
            ResourceItem resourceItem = rmRooms.query(xid, rmiName, location);
            if (resourceItem == null)
                return -1;
            int numAvail = ((Hotel)resourceItem).getPrice();
            return numAvail;
        } catch (DeadlockException e) {
            abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
    }

    public int queryCars(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "queryCars");
        if (location == null)
            return -1;
        try {
            String rmiName = rmCars.getID();
            ResourceItem resourceItem = rmCars.query(xid, rmiName, location);
            if (resourceItem == null)
                return -1;
            int numAvail = ((Car)resourceItem).getNumAvail();
            return numAvail;
        } catch (DeadlockException e) {
            abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
    }

    public int queryCarsPrice(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "queryCarsPrice");
        if (location == null)
            return -1;
        try {
            String rmiName = rmCars.getID();
            ResourceItem resourceItem = rmCars.query(xid, rmiName, location);
            if (resourceItem == null)
                return -1;
            int numAvail = ((Car)resourceItem).getPrice();
            return numAvail;
        } catch (DeadlockException e) {
            abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
    }

    public int queryCustomerBill(int xid, String custName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        return 0;
    }


    // RESERVATION INTERFACE
    public boolean reserveFlight(int xid, String custName, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        flightcounter--;
        return true;
    }

    public boolean reserveCar(int xid, String custName, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        carscounter--;
        return true;
    }

    public boolean reserveRoom(int xid, String custName, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        roomscounter--;
        return true;
    }

    // TECHNICAL/TESTING INTERFACE
    public boolean reconnect() throws RemoteException {

        try {
            rmFlights =
                    (ResourceManager) Naming.lookup(Util.getLookupName(ResourceManager.RMINameFlights));
            System.out.println("WC bound to RMFlights");
            rmRooms =
                    (ResourceManager) Naming.lookup(Util.getLookupName(ResourceManager.RMINameRooms));
            System.out.println("WC bound to RMRooms");
            rmCars =
                    (ResourceManager) Naming.lookup(Util.getLookupName(ResourceManager.RMINameCars));
            System.out.println("WC bound to RMCars");
            rmCustomers =
                    (ResourceManager) Naming.lookup(Util.getLookupName(ResourceManager.RMINameCustomers));
            System.out.println("WC bound to RMCustomers");
            tm =
                    (TransactionManager) Naming.lookup(Util.getLookupName(TransactionManager.RMIName));
            System.out.println("WC bound to TM");
        } catch (Exception e) {
            System.err.println("WC cannot bind to some component:" + e);
            return false;
        }

        try {
            if (rmFlights.reconnect() && rmRooms.reconnect() &&
                    rmCars.reconnect() && rmCustomers.reconnect()) {
                return true;
            }
        } catch (Exception e) {
            System.err.println("Some RM cannot reconnect:" + e);
            return false;
        }

        return false;
    }

    public boolean dieNow(String who)
            throws RemoteException {
        if (who.equals(TransactionManager.RMIName) ||
                who.equals("ALL")) {
            try {
                tm.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(ResourceManager.RMINameFlights) ||
                who.equals("ALL")) {
            try {
                rmFlights.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(ResourceManager.RMINameRooms) ||
                who.equals("ALL")) {
            try {
                rmRooms.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(ResourceManager.RMINameCars) ||
                who.equals("ALL")) {
            try {
                rmCars.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(ResourceManager.RMINameCustomers) ||
                who.equals("ALL")) {
            try {
                rmCustomers.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(WorkflowController.RMIName) ||
                who.equals("ALL")) {
            System.exit(1);
        }
        return true;
    }

    public boolean dieRMAfterEnlist(String who)
            throws RemoteException {
        return true;
    }

    public boolean dieRMBeforePrepare(String who)
            throws RemoteException {
        return true;
    }

    public boolean dieRMAfterPrepare(String who)
            throws RemoteException {
        return true;
    }

    public boolean dieTMBeforeCommit()
            throws RemoteException {
        return true;
    }

    public boolean dieTMAfterCommit()
            throws RemoteException {
        return true;
    }

    public boolean dieRMBeforeCommit(String who)
            throws RemoteException {
        return true;
    }

    public boolean dieRMBeforeAbort(String who)
            throws RemoteException {
        return true;
    }
}
