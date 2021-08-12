package test;

import transaction.TransactionAbortedException;
import transaction.WorkflowController;

public class DieRMBeforeAbort {
    public static void main(String[] args) {
        TestUtil.cleanData();
        TestUtil.launch("ALL");
        WorkflowController wc = TestUtil.connectWC();
        try{
            int xid;

            xid = wc.start();
            wc.dieRMBeforeAbort("RMRooms");
            wc.dieRMBeforeAbort("RMCars");
            wc.addFlight(xid, "CZ8886", 100, 640);
            wc.addRooms(xid, "SHANGHAI", 100, 350);
            wc.addCars(xid, "SHANGHAI", 100, 30);
            wc.newCustomer(xid, "Trump");
            wc.abort(xid);

            ////////////////launch RMFlights
            TestUtil.launch("RMRooms");
            TestUtil.launch("RMCars");
            wc.reconnect();

            xid = wc.start();
            int ret1 = wc.queryFlight(xid, "CZ8886");
            check(-1, ret1, "queryFlight");

            int ret2 = wc.queryFlightPrice(xid, "CZ8886");
            check(-1, ret2, "queryFlightPrice");

            int ret3 = wc.queryRooms(xid, "SHANGHAI");
            check(-1, ret3, "queryRooms");

            int ret4 = wc.queryRoomsPrice(xid, "SHANGHAI");
            check(-1, ret4, "queryRoomsPrice");

            int ret5 = wc.queryCars(xid, "SHANGHAI");
            check(-1, ret5, "queryCars");

            int ret6 = wc.queryCarsPrice(xid, "SHANGHAI");
            check(-1, ret6, "queryCarsPrice");

            int ret7 = wc.queryCustomerBill(xid, "Trump");
            check(-1, ret7, "queryCustomerBill");
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
