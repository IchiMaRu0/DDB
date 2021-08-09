package test;

import transaction.WorkflowController;

public class Simple {
    public static void main(String[] args) {
        TestUtil.cleanData();
        TestUtil.launch("ALL");
        WorkflowController wc = TestUtil.connectWC();
        try {
            int xid = wc.start();
            wc.commit(xid);
            System.out.println("Test pass.");
            TestUtil.shutDownAll(0);
        } catch (Exception e) {
            System.out.println("Test fail:" + e.getMessage());
            TestUtil.shutDownAll(1);
        }
    }
}
