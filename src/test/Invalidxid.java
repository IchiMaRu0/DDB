package test;

import transaction.InvalidTransactionException;
import transaction.WorkflowController;

public class Invalidxid {
    public static void main(String[] args) {
        TestUtil.cleanData();
        TestUtil.launch("ALL");
        WorkflowController wc = TestUtil.connectWC();
        try {
            int xid = wc.start();
            try {
                wc.addCars(111, "Shanghai", 100, 800);
            } catch (InvalidTransactionException e) {
                System.out.println(e.getMessage());
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
