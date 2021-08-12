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
            wc.addRooms(xid,"Shanghai",100,300);
            wc.dieTMBeforeCommit();
            try {
                wc.commit(xid);
            } catch (RemoteException e) {

            }
            TestUtil.launch("TM");
            wc.reconnect();
            int numAvail1 = wc.queryFlight(xid, "MU2219");
            int numAvail2 = wc.queryRooms(xid,"Shanghai");
            if (numAvail1 != -1) {
                System.err.println("Test fail: except -1, get " + numAvail1);
                TestUtil.shutDownAll(1);
            }
            if (numAvail2 != -1) {
                System.err.println("Test fail: except -1, get " + numAvail2);
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
