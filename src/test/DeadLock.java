package test;

import lockmgr.DeadlockException;
import transaction.WorkflowController;

import java.rmi.RemoteException;

public class DeadLock {
    public static void main(String[] a) {
        TestUtil.cleanData();
        TestUtil.launch("ALL");

        WorkflowController wc = TestUtil.connectWC();
        try {
            int xid = wc.start();
            wc.addFlight(xid, "CZ8886", 100, 640);
            wc.addRooms(xid, "SHANGHAI", 100, 350);
            wc.addCars(xid, "SHANGHAI", 100, 30);
            wc.newCustomer(xid, "Trump");
            wc.commit(xid);

            int xid2 = wc.start();
            wc.dieTMAfterCommit();
            wc.addFlight(xid2, "CZ8886", 200, 520);
            wc.addRooms(xid2, "SHANGHAI", 200, 500);

            int xid3 = wc.start();
            wc.addCars(xid3, "SHANGHAI", 100, 30);
            wc.addRooms(xid3, "SHANGHAI", 100, 300);
            wc.addCars(xid2, "SHANGHAI", 100, 90);

        } catch (Exception e) {
            System.out.println("DeadLock exception " + e.getMessage());
            e.printStackTrace();
            TestUtil.shutDownAll(0);
        }
    }
}
