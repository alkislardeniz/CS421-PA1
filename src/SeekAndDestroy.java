// A Java program for a Client
import java.net.*;
import java.util.*;
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
    private String controlPort;
    private String dataPort;

    // create socket, input and output stream
    private Socket socket = null;
    private BufferedReader in = null;
    private DataOutputStream out = null;

    // constructor to put ip address and port
    public SeekAndDestroy(String address, int port)
    {
        this.controlPort = port + "";
        this.dataPort = "12345";
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
            System.out.println(USER + ": " + in.readLine());

            out.writeBytes(generateCommand(PASS, password));
            out.flush();
            System.out.println(PASS + ": " + in.readLine());

            List<String> sharedStrings = new ArrayList<String>();

            out.writeBytes(generateCommand(PORT, dataPort));
            out.flush();
            System.out.println(PORT + ": " + in.readLine());

            out.writeBytes(generateCommand(NLST, ""));
            out.flush();

            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(dataPort),0, InetAddress.getByName(address));
            Socket clientSocket = serverSocket.accept();
            BufferedReader data = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            boolean flag = true;
            String line;
            while (true)
            {
                if ((line = data.readLine()) != null)
                {
                    flag = false;
                    sharedStrings.add(line);
                }
                else if (!flag)
                {
                    break;
                }
            }

            System.out.println("List: " + sharedStrings.toString());
        }
        catch(Exception e)
        {
            System.err.println(e);
        }
    }

    private String generateCommand(String name, String arg)
    {
        if (arg.equals(""))
            return name + "\r\n";
        return name + " " + arg + "\r\n";
    }

    public static void main(String args[])
    {
        SeekAndDestroy client = new SeekAndDestroy("127.0.0.1", 50000);
    }
}