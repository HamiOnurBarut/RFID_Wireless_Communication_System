package rfıdreader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class serverSide {
    public static String ipadd = "139.179.99.35";
    public static void main(String[] args) throws IOException
    {
        int id = 0;
        // Server Socket for accepting requests
        ServerSocket serverside = new ServerSocket(7676);
        while(true)
        {
                // this is for multi-thread server
                Socket clientSocket = serverside.accept();
                // this creates a object that I wrote as inner class object.
                seSocket serv = new seSocket(clientSocket,id++);
                serv.start();
        }
    }

    public static class seSocket extends Thread {
        // PROPERTIES
        public BufferedReader message;
        public BufferedWriter writer;
        public String input;
        public Socket client;
        public int id = 0;

        // CONSTRUCTOR
        public seSocket(Socket client,int id)
        {
            this.client = client;
            this.id = id;
            System.out.println("New request arrived!");
        }

        // Thread's run function
        // this method will send the response to the client
        public void run(){
            super.run();
            try 
            {
                message = new BufferedReader(new InputStreamReader(client.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                // client ip address
                String ipaddress = client.getInetAddress().toString();
                System.out.println(ipaddress);				
                while((input = message.readLine())!= null)
                {   
                    // HTTP 302 Found response message with desired URL
                    writer.write("HTTP/1.1 302 Found\n");
                    String ur = getShopAddress(ipaddress);
                    boolean add = false;
                    // if IP address is not in the database, redirect it to registration page
                    if (ur.equalsIgnoreCase("FirstEntry")){
                        add = true;
                        //ur = "http://" + ipadd + "/add.php";

                    }

                writer.write("Location: " + ur);
                writer.flush();
                writer.close();
                break;
            }

            } catch (IOException e) {
                e.printStackTrace();
            }			
        }

        // This method will return the URL for the given ip address from database		
        public String getShopAddress(String ipaddress) {
                    // If not found on database, this is the URL that will be returned
            String url = "http://" + ipadd + "/welcome.php";
            ipaddress = ipaddress.replace("/","");
            try{
                // Connection to database
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/rfid?autoReconnect=true&useSSL=false","root","password");
                System.out.println("Connected to Database.");
                Statement state = conn.createStatement();
                String query = "USE rfid;";
                ResultSet set = state.executeQuery(query);
                query = "SELECT tagNumber FROM users WHERE IPaddress = '" + ipaddress + "';";
                set = state.executeQuery(query);
                // If IP address is not in the database
                if(!set.isBeforeFirst()){
                    url = "FirstEntry";
                }
                else{
                    // take URL from databases using mySQL commands
                    String tag = "";
                    while(set.next()){
                        tag = set.getString("tagNumber");
                    }

                    System.out.println("Tag read is " + tag);
                    query = "SELECT antennaID FROM RFID where tagNumber = '" + tag + "';";
                    set = state.executeQuery(query);
                    String aid = "";
                    while(set.next()){
                        aid = set.getString("antennaID");
                    }
                    System.out.println("Antenna ID read is : " + aid);
                    query = "SELECT shopURL FROM shops WHERE antennaID = '" + aid + "';";
                    set = state.executeQuery(query);
                    while(set.next()){
                        url = set.getString("shopURL");
                    }
                    System.out.println("Shop URL is :" + url);
                }
                set.close();
                conn.close();
                state.close();
            }
            catch(SQLException e){
                System.out.println("Something went wrong while retrieving shop URL.");
            }

            return url;
        }
    }
}