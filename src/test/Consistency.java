package test;

import transaction.WorkflowController;

public class Consistency {
    public static void main(String[] args) {
        TestUtil.cleanData();
        TestUtil.launch("ALL");
        WorkflowController wc = TestUtil.connectWC();
        try {
            int xid = wc.start();
            wc.newCustomer(xid, "steve");
            wc.newCustomer(xid, "tony");
            wc.addCars(xid, "Shanghai", 50, 1000);
            int numAvail = wc.queryCars(xid, "Shanghai");
            if (numAvail != 50) {
                System.out.println("Test Fail.");
                TestUtil.shutDownAll(1);
            }
            wc.commit(xid);

            xid = wc.start();
            wc.reserveCar(xid, "steve", "Shanghai");
            wc.reserveCar(xid, "tony", "Shanghai");
            wc.commit(xid);

            xid = wc.start();
            numAvail = wc.queryCars(xid, "Shanghai");
            if (numAvail != 48) {
                System.out.println("Test Fail.");
                TestUtil.shutDownAll(1);
            }
            System.out.println("Test pass.");
            TestUtil.shutDownAll(0);
        } catch (Exception e) {
            System.out.println("Test fail: " + e.getMessage());
            TestUtil.shutDownAll(1);
        }
    }
}
