package test;

import transaction.WorkflowController;

import java.rmi.RemoteException;

public class Atomicity {
    public static void main(String[] args) {
        TestUtil.cleanData();
        TestUtil.launch("ALL");
        WorkflowController wc = TestUtil.connectWC();
        try {
            int xid = wc.start();
            wc.addFlight(xid, "MU2219", 300, 700);
            wc.dieTMBeforeCommit();
            try {
                wc.commit(xid);
            } catch (RemoteException e) {

            }
            TestUtil.launch("TM");
            wc.reconnect();
            int numAvail = wc.queryFlight(xid, "MU2219");
            if (numAvail != -1) {
                System.err.println("Test fail: except -1, get " + numAvail);
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
