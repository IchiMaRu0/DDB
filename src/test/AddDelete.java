package test;

import transaction.WorkflowController;

public class AddDelete {
    public static void main(String[] args) {
        TestUtil.cleanData();
        TestUtil.launch("ALL");
        WorkflowController wc = TestUtil.connectWC();
        try {
            int xid = wc.start();
            wc.addRooms(xid, "Shanghai", 100, 300);
            wc.addCars(xid, "Shanghai", 200, 800);
            wc.commit(xid);

            xid = wc.start();
            int n = wc.queryRooms(xid, "Shanghai");
            int p = wc.queryRoomsPrice(xid, "Shanghai");
            if (n != 100) {
                System.out.println("Test Fail.");
                TestUtil.shutDownAll(1);
            }
            if (p != 300) {
                System.out.println("Test Fail.");
                TestUtil.shutDownAll(1);
            }
            n = wc.queryCars(xid, "Shanghai");
            p = wc.queryCarsPrice(xid, "Shanghai");
            if (n != 200) {
                System.out.println("Test Fail.");
                TestUtil.shutDownAll(1);
            }
            if (p != 800) {
                System.out.println("Test Fail.");
                TestUtil.shutDownAll(1);
            }
            wc.commit(xid);

            xid = wc.start();
            wc.deleteRooms(xid, "Shanghai", 20);
            wc.deleteCars(xid, "Shanghai", 30);
            n = wc.queryRooms(xid, "Shanghai");
            if (n != 80) {
                System.out.println("Test Fail.");
                TestUtil.shutDownAll(1);
            }
            n = wc.queryCars(xid, "Shanghai");
            if (n != 170) {
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
