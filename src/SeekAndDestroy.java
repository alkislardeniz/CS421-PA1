// A Java program for a Client
import java.net.*;
import java.util.*;
import java.io.*;
       class MyThread extends Thread {
List<String> strings;
 MyThread(List<String> strings) {
        this.strings = strings;
    }
    public void run() {
         try
        {
            
            System.out.println("zaart");
           
            ServerSocket serverSocket = new ServerSocket(1634,0, InetAddress.getByName("127.0.0.1"));
            
            Socket clientSocket = serverSocket.accept();
            System.out.println("zaart2");
            BufferedReader in = new BufferedReader(
        new InputStreamReader(clientSocket.getInputStream()));
           boolean flag = true;
           String line;
          while(true){
           
            
            
            if((line = in.readLine())!=null){
                flag = false;
                 System.out.println("zaa"+line);
                this.strings.add(line+"\r\n");
            }else if(!flag){
                break;
            }

        
       
    }

         }

        catch(Exception e)
        {
            System.err.println("asass");
        }
       
  }
}

public class SeekAndDestroy 
{
    // initialize commands
    private final String USER = "USER";
    private final String PASS = "PASS";
    private final String PORT = "PORT";
    private final String NLST = "NLST";
    private final String CWD = "CWD";
    private final String CDUP = "CDUP";
    private final String RETR = "RETR";
    private final String DELE = "DELE";
    private final String QUIT = "QUIT";

    private String username;
    private String password;
    private String port;

    // create socket, input and output stream
    private Socket socket = null;
    private BufferedReader in = null;
    private DataOutputStream out = null;

    // constructor to put ip address and port
    public SeekAndDestroy(String address, int port)
    {
        this.port = port + "";
        this.username = "bilkent";
        this.password = "cs421";

        // establish a connection
        try
        {
            socket = new Socket(address, port);
            System.out.println("Connected");

            // sends output to the socket
             in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());

            out.writeBytes(generateCommand(USER, username));
            out.flush();
           
            System.out.println(in.readLine());
    
            out.writeBytes(generateCommand(PASS, password));
            out.flush();
           
            System.out.println(in.readLine());
            List<String> sharedStrings = new ArrayList<String>();
             MyThread thread = new MyThread(sharedStrings);
             thread.start();
          
           
            
           

            out.writeBytes(generateCommand(PORT,""+ 1634));
            out.flush();
           
            System.out.println(in.readLine());
            out.writeBytes(generateCommand(NLST,""  ));
            out.flush();
            System.out.println("zaa1");
            System.out.println(in.readLine());
            System.out.println("zaa");
       
            thread.join();
            System.out.println("xdd"+sharedStrings.toString());


            // takes input from terminal
           
        }
        catch(Exception e)
        {
            System.err.println(e);
        }
    }

    private String generateCommand(String name, String arg)
    {
        return name + " " + arg + "\r\n";
    }

    public static void main(String args[])
    {
        SeekAndDestroy client = new SeekAndDestroy("127.0.0.1", 60000);
    }
}