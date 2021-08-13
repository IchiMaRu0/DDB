package test;

import transaction.TransactionAbortedException;
import transaction.WorkflowController;

public class Isolation {
    public static void main(String[] args) {
        TestUtil.cleanData();
        TestUtil.launch("ALL");
        WorkflowController wc = TestUtil.connectWC();
        try {
            int xid1 = wc.start();
            wc.addFlight(xid1, "MU2219", 300, 700);
            wc.commit(xid1);

            xid1 = wc.start();
            wc.addFlight(xid1, "MU2219", 100, 600);

            int xid2 = wc.start();
            try {
                wc.addFlight(xid2, "MU2219", 100, 500);
            } catch (TransactionAbortedException e) {

            }

            int numAvail = wc.queryFlight(xid1, "MU2219");
            if (numAvail != 400) {
                System.out.println("Test Fail.");
                TestUtil.shutDownAll(1);
            }
            wc.commit(xid1);

            xid2 = wc.start();
            wc.addFlight(xid2, "MU2219", 100, 500);
            numAvail = wc.queryFlight(xid2, "MU2219");
            if (numAvail != 500) {
                System.out.println("Test Fail.");
                TestUtil.shutDownAll(1);
            }
            wc.commit(xid2);
            System.out.println("Test pass.");
            TestUtil.shutDownAll(0);

        } catch (Exception e) {
            System.out.println("Test fail: " + e.getMessage());
            TestUtil.shutDownAll(1);
        }
    }
}
