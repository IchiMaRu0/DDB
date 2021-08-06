package transaction;

import lockmgr.DeadlockException;
import transaction.entity.*;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collection;
import java.util.Collections;
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

    public ResourceItem queryItem(ResourceManager rm, int xid, String key)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "");
        ResourceItem resourceItem;
        try {
            resourceItem = rm.query(xid, rm.getID(), key);
        } catch (DeadlockException e) {
            abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
        return resourceItem;
    }


    // ADMINISTRATIVE INTERFACE
    public boolean addFlight(int xid, String flightNum, int numSeats, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "addFlight");
        if (flightNum == null)
            return false;
        if (numSeats < 0)
            return false;
        ResourceItem resourceItem = queryItem(rmFlights, xid, flightNum);
        if (resourceItem == null) {
            price = Math.max(0, price);
            Flight flight = new Flight(flightNum, price, numSeats);
            try {
                return rmFlights.insert(xid, rmFlights.getID(), flight);
            } catch (DeadlockException e) {
                abort(xid);
                throw new TransactionAbortedException(xid, e.getMessage());
            }
        } else {
            Flight flight = (Flight) resourceItem;
            flight.addSeats(numSeats);
            if (price >= 0)
                flight.setPrice(price);
            try {
                return rmFlights.update(xid, rmFlights.getID(), flightNum, flight);
            } catch (DeadlockException e) {
                abort(xid);
                throw new TransactionAbortedException(xid, e.getMessage());
            }
        }
    }

    public boolean deleteFlight(int xid, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "deleteFlight");
        if (flightNum == null)
            return false;
        ResourceItem resourceItem = queryItem(rmFlights, xid, flightNum);
        if (resourceItem == null)
            return false;
        try {
            Collection<ResourceItem> resvs = rmCustomers.query(xid, ResourceManager.TableNameReservations, Reservation.INDEX_RESERV_KEY, flightNum);
            if (!resvs.isEmpty())
                return false;
            resourceItem.delete();
            return rmFlights.delete(xid, rmFlights.getID(), flightNum);
        } catch (InvalidIndexException e) {
            System.err.println(e.getMessage());
        } catch (DeadlockException e) {
            abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
        return true;
    }

    public boolean addRooms(int xid, String location, int numRooms, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "addRooms");
        if (location == null)
            return false;
        if (numRooms < 0)
            return false;
        ResourceItem resourceItem = queryItem(rmRooms, xid, location);
        if (resourceItem == null) {
            price = Math.max(0, price);
            Hotel hotel = new Hotel(location, price, numRooms);
            try {
                return rmRooms.insert(xid, rmRooms.getID(), hotel);
            } catch (DeadlockException e) {
                abort(xid);
                throw new TransactionAbortedException(xid, e.getMessage());
            }
        } else {
            Hotel hotel = (Hotel) resourceItem;
            hotel.addRooms(numRooms);
            if (price >= 0)
                hotel.setPrice(price);
            try {
                return rmRooms.update(xid, rmRooms.getID(), location, hotel);
            } catch (DeadlockException e) {
                abort(xid);
                throw new TransactionAbortedException(xid, e.getMessage());
            }
        }
    }

    public boolean deleteRooms(int xid, String location, int numRooms)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "deleteRooms");
        if (location == null)
            return false;
        if (numRooms < 0)
            return false;
        ResourceItem resourceItem = queryItem(rmRooms, xid, location);
        if (resourceItem == null)
            return false;
        Hotel hotel = (Hotel) resourceItem;
        boolean deleted = hotel.deleteRooms(numRooms);
        if (!deleted)
            return false;
        try {
            return rmRooms.update(xid, rmRooms.getID(), location, hotel);
        } catch (DeadlockException e) {
            abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }

    }

    public boolean addCars(int xid, String location, int numCars, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "addCars");
        if (location == null)
            return false;
        if (numCars < 0)
            return false;
        ResourceItem resourceItem = queryItem(rmCars, xid, location);
        if (resourceItem == null) {
            price = Math.max(0, price);
            Car car = new Car(location, price, numCars);
            try {
                return rmCars.insert(xid, rmCars.getID(), car);
            } catch (DeadlockException e) {
                abort(xid);
                throw new TransactionAbortedException(xid, e.getMessage());
            }
        } else {
            Car car = (Car) resourceItem;
            car.addCars(numCars);
            if (price >= 0)
                car.setPrice(price);
            try {
                return rmCars.update(xid, rmCars.getID(), location, car);
            } catch (DeadlockException e) {
                abort(xid);
                throw new TransactionAbortedException(xid, e.getMessage());
            }
        }
    }

    public boolean deleteCars(int xid, String location, int numCars)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "deleteCars");
        if (location == null)
            return false;
        if (numCars < 0)
            return false;
        ResourceItem resourceItem = queryItem(rmCars, xid, location);
        if (resourceItem == null)
            return false;
        Car car = (Car) resourceItem;
        boolean deleted = car.deleteCars(numCars);
        if (!deleted)
            return false;
        try {
            return rmCars.update(xid, rmCars.getID(), location, car);
        } catch (DeadlockException e) {
            abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
    }

    public boolean newCustomer(int xid, String custName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "newCustomer");
        ResourceItem resourceItem = queryItem(rmCustomers, xid, custName);
        if (resourceItem != null)
            return true;
        Customer customer = new Customer(custName);
        try {
            return rmCustomers.insert(xid, rmCustomers.getID(), customer);
        } catch (DeadlockException e) {
            abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
    }

    public void unReserveAll(int xid, String custName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException,
            InvalidIndexException,
            DeadlockException {
        Collection<ResourceItem> resvs = rmCustomers.query(xid, ResourceManager.TableNameReservations, Reservation.INDEX_CUSTNAME, custName);
        for (ResourceItem resouceItem : resvs) {
            Reservation resv = (Reservation) resouceItem;
            String resvKey = resv.getResvKey();
            int resvType = resv.getResvType();
            switch (resvType) {
                case Reservation.RESERVATION_TYPE_FLIGHT: {
                    Flight flight = (Flight) queryItem(rmFlights, xid, resvKey);
                    flight.cancelResv();
                    rmFlights.update(xid, rmFlights.getID(), resvKey, flight);
                    break;
                }
                case Reservation.RESERVATION_TYPE_HOTEL: {
                    Hotel hotel = (Hotel) queryItem(rmRooms, xid, resvKey);
                    hotel.cancelResv();
                    rmRooms.update(xid, rmRooms.getID(), resvKey, hotel);
                    break;
                }
                case Reservation.RESERVATION_TYPE_CAR: {
                    Car car = (Car) queryItem(rmCars, xid, resvKey);
                    car.cancelResv();
                    rmCars.update(xid, rmCars.getID(), resvKey, car);
                }
            }
        }
    }

    public boolean deleteCustomer(int xid, String custName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "deleteCustomer");
        if (custName == null)
            return false;
        ResourceItem resourceItem = queryItem(rmCustomers, xid, custName);
        if (resourceItem == null)
            return false;
        try {
            rmCustomers.delete(xid, rmCustomers.getID(), custName);
        } catch (DeadlockException e) {
            abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        }
        try {
            unReserveAll(xid, custName);
            rmCustomers.delete(xid, ResourceManager.TableNameReservations, Reservation.INDEX_CUSTNAME, custName);
        } catch (DeadlockException e) {
            abort(xid);
            throw new TransactionAbortedException(xid, e.getMessage());
        } catch (InvalidIndexException e) {
            System.err.println(e.getMessage());
        }
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
        ResourceItem resourceItem = queryItem(rmFlights, xid, flightNum);
        if (resourceItem == null)
            return -1;
        return ((Flight) resourceItem).getNumAvail();
    }

    public int queryFlightPrice(int xid, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "queryFlightPrice");
        if (flightNum == null)
            return -1;
        ResourceItem resourceItem = queryItem(rmFlights, xid, flightNum);
        if (resourceItem == null)
            return -1;
        return ((Flight) resourceItem).getPrice();
    }

    public int queryRooms(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "queryRooms");
        if (location == null)
            return -1;
        ResourceItem resourceItem = queryItem(rmRooms, xid, location);
        if (resourceItem == null)
            return -1;
        return ((Hotel) resourceItem).getNumAvail();
    }

    public int queryRoomsPrice(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "queryRoomsPrice");
        if (location == null)
            return -1;
        ResourceItem resourceItem = queryItem(rmRooms, xid, location);
        if (resourceItem == null)
            return -1;
        return ((Hotel) resourceItem).getPrice();
    }

    public int queryCars(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "queryCars");
        if (location == null)
            return -1;
        ResourceItem resourceItem = queryItem(rmCars, xid, location);
        if (resourceItem == null)
            return -1;
        return ((Car) resourceItem).getNumAvail();
    }

    public int queryCarsPrice(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        if (!xids.contains(xid))
            throw new InvalidTransactionException(xid, "queryCarsPrice");
        if (location == null)
            return -1;
        ResourceItem resourceItem = queryItem(rmCars, xid, location);
        if (resourceItem == null)
            return -1;
        return ((Car) resourceItem).getPrice();
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
