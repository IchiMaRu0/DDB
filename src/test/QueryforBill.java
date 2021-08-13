package test;

import transaction.WorkflowController;

public class QueryforBill {
    public static void main(String[] args) {
        TestUtil.cleanData();
        TestUtil.launch("ALL");
        WorkflowController wc = TestUtil.connectWC();
        try {
            int xid = wc.start();
            wc.addFlight(xid, "MU2219", 300, 700);
            wc.addRooms(xid, "Shanghai", 100, 300);
            wc.addCars(xid, "Shanghai", 100, 800);
            wc.newCustomer(xid, "tony");
            wc.commit(xid);

            xid = wc.start();
            wc.reserveFlight(xid, "tony", "MU2219");
            wc.reserveRoom(xid, "tony", "Shanghai");
            wc.reserveCar(xid, "tony", "Shanghai");
            wc.commit(xid);

            xid = wc.start();
            int n = wc.queryCustomerBill(xid, "tony");
            if (n != 1800) {
                System.out.println("Test Fail.");
                TestUtil.shutDownAll(1);
            }
            wc.commit(xid);
            System.out.println("Test pass.");
            TestUtil.shutDownAll(0);
        } catch (Exception e) {
            System.out.println("Test fail: " + e.getMessage());
            TestUtil.shutDownAll(1);
        }
    }
}
