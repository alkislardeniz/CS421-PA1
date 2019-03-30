// A Java program for a Client
import java.net.*;
import java.io.*;

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

            out.writeBytes(generateCommand(PORT, this.port));
            out.flush();
           
            System.out.println(in.readLine());

            // takes input from terminal
           
        }
        catch(Exception e)
        {
            System.err.println(e);
        }
    }

    private String generateCommand(String name, String arg)
    {
        return name + " " + arg + "\r\n\r\n";
    }

    public static void main(String args[])
    {
        SeekAndDestroy client = new SeekAndDestroy("127.0.0.1", 60000);
    }
}