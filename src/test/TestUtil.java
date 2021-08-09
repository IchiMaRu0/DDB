package test;

import transaction.ResourceManager;
import transaction.TransactionManager;
import transaction.Util;
import transaction.WorkflowController;

import java.io.IOException;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestUtil {
    public static void cleanData() {
        try {
            if (Runtime.getRuntime().exec("rm -rf data").waitFor() != 0) {
                System.err.println("Clean data not successful");
            }
        } catch (IOException e) {
            System.err.println("Cannot clean data: " + e);
            System.exit(1);
        } catch (InterruptedException e) {
            System.err.println("WaitFor interrupted.");
            System.exit(1);
        }
    }

    public static void launch(String who) {
        String[] rmiNames = new String[]{TransactionManager.RMIName,
                ResourceManager.RMINameFlights,
                ResourceManager.RMINameRooms,
                ResourceManager.RMINameCars,
                ResourceManager.RMINameCustomers,
                WorkflowController.RMIName};
        String[] classNames = new String[]{"TransactionManagerImpl",
                "RMManagerFlights",
                "RMManagerHotels",
                "RMManagerCars",
                "RMManagerCustomers",
                "WorkflowControllerImpl"};

        for (int i = 0; i < rmiNames.length; i++) {
            if (who.equals(rmiNames[i]) || who.equals("ALL")) {
                try {
                    Runtime.getRuntime().exec(new String[]{
                            "sh",
                            "-c",
                            "java -classpath .. "  +
                                    " -Djava.security.policy=./security-policy transaction." + classNames[i] +
                                    " >>" + "results/" + System.getProperty("testName") + "/" + rmiNames[i] + ".log" + " 2>&1"});
                } catch (Exception e) {
                    System.err.println("Cannot launch " + rmiNames[i] + ": " + e);
                    shutDownAll(1);
                }

                System.out.println(rmiNames[i] + " launched");

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    System.err.println("Sleep interrupted.");
                    System.exit(1);
                }
            }
        }
    }

    public static void shutDownAll(int status) {
        try {
            WorkflowController wc = (WorkflowController) Naming.lookup(Util.getLookupName(WorkflowController.RMIName));
            wc.dieNow("ALL");
        } catch (Exception e) {
            System.exit(status);
        }
    }

    public static WorkflowController connectWC() {
        WorkflowController wc = null;

        try {
            wc = (WorkflowController) Naming.lookup(Util.getLookupName(WorkflowController.RMIName));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Cannot bind to WC: " + e.getMessage());
            System.exit(1);
        }
        return wc;
    }
}
