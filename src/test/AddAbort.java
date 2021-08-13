package test;

import transaction.WorkflowController;

public class AddAbort {
    public static void main(String[] args) {
        TestUtil.cleanData();
        TestUtil.launch("ALL");
        WorkflowController wc = TestUtil.connectWC();
        try {
            int xid = wc.start();
            wc.addCars(xid, "Shanghai", 50, 1000);
            wc.commit(xid);

            xid = wc.start();
            wc.addCars(xid, "shanghai", 10, 2000);
            wc.abort(xid);

            xid = wc.start();
            int n = wc.queryCars(xid, "Shanghai");
            if (n != 50) {
                System.out.println("Test Fail.");
                TestUtil.shutDownAll(1);
            }
            int p = wc.queryCarsPrice(xid, "Shanghai");
            if (p != 1000) {
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
