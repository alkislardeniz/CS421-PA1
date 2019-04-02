// A Java program for a Client
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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
    private BufferedReader data = null;
    private Socket clientSocket = null;
    private ServerSocket serverSocket = null;
    private boolean imageFound;


    // constructor to put ip address and port
    public SeekAndDestroy(String address, int port)
    {
        this.controlPort = port + "";
        this.dataPort = "12345";
        this.username = "bilkent";
        this.password = "cs421";
        this.imageFound = false;

        // establish a connection
        try
        {
            socket = new Socket(address, Integer.parseInt(controlPort));
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
            serverSocket = new ServerSocket(Integer.parseInt(dataPort),0, InetAddress.getByName(address));

            out.writeBytes(generateCommand(PORT, dataPort));
            out.flush();
            System.out.println(PORT + ": " + in.readLine());

            out.writeBytes(generateCommand(NLST, ""));
            out.flush();
            System.out.println(NLST + "1 : " + in.readLine());

            clientSocket = serverSocket.accept();
            data = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            search();

        }
        catch(Exception e)
        {
            System.err.println(e);
        }
    }

    private void search()
    {
        try
        {
            ArrayList<String> sharedStrings = new ArrayList<>();
            out.writeBytes(generateCommand(NLST, ""));
            out.flush();
            System.out.println(NLST + ": " + in.readLine());

            clientSocket = serverSocket.accept();
            data = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String line = data.readLine();
            byte[] size = line.getBytes();

            if (!(size[0] == 0 && size[1] == 0)) {
                byte[] size2 = new byte[size.length - 2];
                for (int i = 2; i < size.length; i++)
                    size2[i - 2] = size[i];

                line = new String(size2);
                while (line != null) {
                    sharedStrings.add(line);
                    line = data.readLine();
                }
                System.out.println(sharedStrings.toString());

                for (int i = 0; i < sharedStrings.size(); i++) {
                    String[] item = sharedStrings.get(i).split(":");
                    if (item[1].equals("d")) {
                        out.writeBytes(generateCommand(CWD, item[0]));
                        out.flush();
                        System.out.println(CWD + " " + item[0] + " " + in.readLine());
                        search();
                    } else if (item[0].equals("target.jpg")) {
                        out.writeBytes(generateCommand(RETR, item[0]));
                        out.flush();
                        System.out.println(RETR + " " + item[0] + " " + in.readLine());
                        out.writeBytes(generateCommand(DELE, item[0]));
                        out.flush();
                        System.out.println(DELE + " " + item[0] + " " + in.readLine());
                        System.err.println("Found!");

                        clientSocket = serverSocket.accept();
                        data = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                        String img = data.readLine();
                        System.out.println(img);
                    }
                }
                System.err.println("For biter");
            }
            else
            {
                System.err.println("İçim boş");
            }
            out.writeBytes(generateCommand(CDUP, ""));
            out.flush();
            System.out.println(CDUP + ": " + in.readLine());
        }
        catch(Exception e)
        {
            System.out.println(e);
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
        SeekAndDestroy client = new SeekAndDestroy("127.0.0.1", 11111);
    }
}