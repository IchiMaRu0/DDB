package test;

import transaction.TransactionAbortedException;
import transaction.WorkflowController;

public class DieRMAfterPrepare {
    public static void main(String[] args) {
        TestUtil.cleanData();
        TestUtil.launch("ALL");
        WorkflowController wc = TestUtil.connectWC();
        try{
            int xid;

            xid = wc.start();
            wc.addFlight(xid, "CZ8886", 100, 640);
            wc.addRooms(xid, "SHANGHAI", 100, 350);
            wc.addCars(xid, "SHANGHAI", 100, 30);
            wc.newCustomer(xid, "Trump");
            wc.commit(xid);

            xid = wc.start();
            wc.addFlight(xid, "CZ8886", 100, 520);
            wc.addRooms(xid, "SHANGHAI", 200, 300);
            wc.addCars(xid, "SHANGHAI", 300, 60);
            wc.dieRMAfterPrepare("RMFlights");

            try {
                wc.commit(xid);
            } catch (TransactionAbortedException e) {
                // e.printStackTrace();
                System.out.println("transaction aborted detected in client.");
            }
            ////////////////except transaction.exceptions.TransactionAbortedException
            ////////////////launch RMFlights
            TestUtil.launch("RMFlights");
            wc.reconnect();

            xid = wc.start();
            int ret1 = wc.queryFlight(xid, "CZ8886");
            check(100, ret1, "queryFlight");

            int ret2 = wc.queryFlightPrice(xid, "CZ8886");
            check(640, ret2, "queryFlightPrice");

            int ret3 = wc.queryRooms(xid, "SHANGHAI");
            check(100, ret3, "queryRooms");

            int ret4 = wc.queryRoomsPrice(xid, "SHANGHAI");
            check(350, ret4, "queryRoomsPrice");

            int ret5 = wc.queryCars(xid, "SHANGHAI");
            check(100, ret5, "queryCars");

            int ret6 = wc.queryCarsPrice(xid, "SHANGHAI");
            check(30, ret6, "queryCarsPrice");

            int ret7 = wc.queryCustomerBill(xid, "Trump");
            check(0, ret7, "queryCustomerBill");
            wc.commit(xid);

            TestUtil.shutDownAll(0);
        }catch(Exception e){
            System.out.println("DieRMAfterPrepare exception "+e.getMessage());
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
