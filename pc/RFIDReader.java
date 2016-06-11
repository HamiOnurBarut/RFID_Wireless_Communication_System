/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rfıdreader;

/**
 *
 * @author tahir
 */
// Import Sirit libraries
import com.sirit.data.DataManager;
import com.sirit.driver.ConnectionException;
import com.sirit.driver.IEventListener;
import com.sirit.mapping.EventInfo;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

// Import SQL and JAVA libraries
import java.util.Hashtable;
import java.sql.*;

public class RFIDReader implements IEventListener
{
    
    // PROPERTIES for SIRIT RFID READER CONNECTION
    private static DataManager dataReader;
    private static String ipAddress = "139.179.32.176"; // CHANGE THIS ACCORDINGLY !!!!!!!!
    private static Connection conn;
    
    public static String antenna1() {
        String tag = "Nothing";
        try {
            Hashtable result = dataReader.exec("tag.read_id(antenna=1)", new Hashtable());
            if (!result.isEmpty()) {
                tag = result.get("tag_id").toString();
                System.out.println(tag + " is read from the first antenna.");
                
            }
        } catch (Exception e) {
        	//System.out.println("No tag is read from the first antenna.");
        }
        return tag;
    }
    
    public static String antenna2() {
        String tag = "Nothing";
        try {
            Hashtable result = dataReader.exec("tag.read_id(antenna=2)", new Hashtable());
            if (!result.isEmpty()) {
                tag = result.get("tag_id").toString();
                System.out.println(tag + " is read from the second antenna.");
            }
        } catch (Exception e) {
        	//System.out.println("No tag is read from the second antenna.");
        }
        return tag;
    }

    public void EventFound(Object sender, EventInfo eventInfo) {
        String tagID = eventInfo.getParameter(EventInfo.EVENT_TAG_ARRIVE_PARAMS.TAG_ID);
        System.out.println("Tag ID: " + tagID);
    }
    
    public static void main(String[] args) throws SocketException, IOException {
    	
        try{
            String query = "USE rfid;";

            try{
            dataReader = new DataManager(DataManager.ConnectionTypes.SOCKET, ipAddress, 0);
            dataReader.open();
            }catch(ConnectionException er){
                    System.out.println("Reader Connection Unsuccessful");
            }
            boolean terminate = false;
            String tagRead;
            conn = DriverManager.getConnection("jdbc:mysql://localhost/rfid?autoReconnect=true&useSSL=false","root","password");	
            Statement state = conn.createStatement();          
            ResultSet set = state.executeQuery(query);
            PreparedStatement ps;
            while (true) {
                // MAKE NECESSARY MODIFICATIONS HERE
                tagRead = antenna1();
                if(tagRead != "Nothing"){
                    query = "DELETE FROM RFID where tagNumber = '" + tagRead + "';";
                    ps = conn.prepareStatement(query);
                    ps.execute();

                    query = "INSERT INTO RFID VALUES('" + tagRead + "','1');";
                    state.executeUpdate(query);
                    System.out.println("Database is updated successfully.");
                    getIP(tagRead);                                       
                }
                tagRead = antenna2();
                if(tagRead != "Nothing"){
                    query = "DELETE FROM RFID WHERE tagNumber = '" + tagRead + "';";
                    ps = conn.prepareStatement(query);
                    ps.execute();

                    query = "INSERT INTO RFID VALUES('" + tagRead + "','2');";
                    state.executeUpdate(query);
                    System.out.println("Database is updated successfully.");
                    getIP(tagRead);
                }
                if (terminate == true)
                {

                    try {
                        dataReader.close();
                        conn.close();
                        state.close();
                        set.close();
                    } catch (ConnectionException e) {
                        System.out.println("Connection can not be terminated");
                    }
                    System.exit(0);
                    break;
                }
            }
        }catch(SQLException e){
        	System.out.println("SQL ERROR");
        }
    }
    public static void getIP(String tag) throws SQLException, UnknownHostException, SocketException, IOException{    
        Statement state = conn.createStatement();
        String query = "USE rfid;";
        ResultSet set = state.executeQuery(query);
        query = "SELECT IPaddress FROM users WHERE tagNumber= '" + tag + "';";
        set = state.executeQuery(query);
        String clientIP="";
        while(set.next()){
            clientIP = set.getString("IPaddress");
        }
        InetAddress IPAddress = InetAddress.getByName(clientIP);
        DatagramSocket server = new DatagramSocket();
        String sentence = "";      
        byte[] sendData = sentence.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9000);
        server.send(sendPacket);
        server.close();
    }
    
}