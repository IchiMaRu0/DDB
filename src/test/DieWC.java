package test;

import transaction.WorkflowController;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class DieWC {
    public static void main(String[] a) {
        TestUtil.cleanData();
        TestUtil.launch("ALL");

        WorkflowController wc = TestUtil.connectWC();
        try {
            int xid;
            xid = wc.start();
            wc.addFlight(xid, "CZ8886", 100, 640);
            wc.addRooms(xid, "SHANGHAI", 100, 350);
            wc.addCars(xid, "SHANGHAI", 100, 30);
            wc.newCustomer(xid, "Trump");
            wc.commit(xid);

            xid = wc.start();
            wc.addFlight(xid, "CZ8886", 100, 900);
            wc.reserveRoom(xid, "Trump", "SHANGHAI");

            try {
                wc.dieNow("WC");
            }catch (RemoteException e) {
                // e.printStackTrace();
            }

            /////////launch WC
            TestUtil.launch("WC");
            wc = TestUtil.connectWC();
            wc.commit(xid);


            xid = wc.start();
            int r1 = wc.queryFlight(xid, "CZ8886");
            check(200, r1, "queryFlight");
            int r2 = wc.queryFlightPrice(xid, "CZ8886");
            check(900, r2, "queryFlightPrice");
            int r3 = wc.queryRooms(xid, "SHANGHAI");
            check(99, r3, "queryRooms");
            int r4 = wc.queryRoomsPrice(xid, "SHANGHAI");
            check(350, r4, "queryRoomsPrice");
            int r5 = wc.queryCars(xid, "SHANGHAI");
            check(100, r5, "queryCars");
            int r6 = wc.queryCarsPrice(xid, "SHANGHAI");
            check(30, r6, "queryCarsPrice");
            int r7 = wc.queryCustomerBill(xid, "Trump");
            check(350, r7, "queryCustomerBill");
            wc.commit(xid);

            System.out.println("Test pass.");
            TestUtil.shutDownAll(0);

        } catch (Exception e) {
            System.out.println("DieWC exception " + e.getMessage());
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
