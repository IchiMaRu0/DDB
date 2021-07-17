package transaction;

import java.io.FileInputStream;
import java.util.Properties;

public class Util {
    public static String getRMIPort(String rmiName){
        Properties prop = new Properties();
        try
        {
            prop.load(new FileInputStream("../../conf/ddb.conf"));
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
            return null;
        }
        if (rmiName.substring(0,2).equals("RM"))
            return  prop.getProperty("rm." + rmiName + ".port");
        if (rmiName.substring(0,2).equals("TM"))
            return prop.getProperty("tm.port");
        if (rmiName.substring(0,2).equals("WC"))
            return prop.getProperty("wc.port");
        return null;
    }
    public static String getLookupName(String rmiName){
        String rmiPort = Util.getRMIPort(rmiName);
        if (rmiPort == null)
        {
            rmiPort = "";
        }
        else if (!rmiPort.equals(""))
        {
            rmiPort = "//:" + rmiPort + "/";
        }
        return rmiPort + rmiName;
    }
}
