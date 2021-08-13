package test;

import transaction.TransactionAbortedException;
import transaction.WorkflowController;

import java.rmi.RemoteException;

public class Durability {
    public static void main(String[] args) {
        TestUtil.cleanData();
        TestUtil.launch("ALL");
        WorkflowController wc = TestUtil.connectWC();
        try {
            int xid = wc.start();
            wc.addFlight(xid, "MU2219", 300, 700);
            wc.addRooms(xid, "Shanghai", 100, 300);
            wc.dieTMAfterCommit();
            try {
                wc.commit(xid);
            } catch (RemoteException e) {

            }
            TestUtil.launch("TM");
            wc.reconnect();

            xid = wc.start();
            int n1 = wc.queryFlight(xid, "MU2219");
            if (n1 != 300) {
                System.out.println("Test Fail.");
                TestUtil.shutDownAll(1);
            }
            int n2 = wc.queryRooms(xid, "Shanghai");
            if (n2 != 100) {
                System.out.println("Test Fail.");
                TestUtil.shutDownAll(1);
            }
            try {
                wc.commit(xid);
            } catch (TransactionAbortedException e) {

            }
            System.out.println("Test pass.");
            TestUtil.shutDownAll(0);
        } catch (Exception e) {
            System.out.println("Test fail: " + e.getMessage());
            TestUtil.shutDownAll(1);
        }
    }
}
