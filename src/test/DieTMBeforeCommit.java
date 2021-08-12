package test;

import transaction.WorkflowController;

import java.rmi.RemoteException;

public class DieTMBeforeCommit {
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

            xid = wc.start();
            wc.dieTMBeforeCommit();
            wc.addFlight(xid, "CZ8886", 200, 520);
            wc.addRooms(xid, "SHANGHAI", 200, 500);
            wc.addCars(xid, "SHANGHAI", 100, 90);

            try {
                wc.commit(xid);
            } catch (RemoteException e) {
                System.out.println("TM down detected in client.");
            }

            ///////////////launch TM
            TestUtil.launch("TM");
            wc.reconnect();

            try {
                // make sure transaction recovered
                Thread.sleep(5000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            xid = wc.start();
            int r1 = wc.queryFlight(xid, "CZ8886");
            check(100, r1, "queryFlight");

            int r2 = wc.queryFlightPrice(xid, "CZ8886");
            check(640, r2, "queryFlightPrice");

            int r3 = wc.queryRooms(xid, "SHANGHAI");
            check(100, r3, "queryRooms");

            int r4 = wc.queryRoomsPrice(xid, "SHANGHAI");
            check(350, r4, "queryRoomsPrice");

            int r5 = wc.queryCars(xid, "SHANGHAI");
            check(100, r5, "queryCars");

            int r6 = wc.queryCarsPrice(xid, "SHANGHAI");
            check(30, r6, "queryCarsPrice");

            int r7 = wc.queryCustomerBill(xid, "Trump");
            check(0, r7, "queryCustomerBill");

            wc.commit(xid);

            TestUtil.shutDownAll(0);
        } catch (Exception e) {
            System.out.println("DieTMBeforeCommit exception " + e.getMessage());
            e.printStackTrace();
            TestUtil.shutDownAll(1);
        }
    }

    private static void check(int expect, int real, String method) {
        if (expect != real) {
            System.out.println(expect + " " + real);
            System.err.println("Test fail: " + method);
            TestUtil.shutDownAll(1);
        }
    }
}
