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

    // create socket, input and output stream
    private Socket socket;
    private BufferedReader in;
    private DataOutputStream out;
    private BufferedReader data;
    private Socket clientSocket;
    private ServerSocket serverSocket;

    private String username;
    private String password;
    private boolean keepSearch;

    // constructor
    public SeekAndDestroy(String address, int controlPort, int dataPort)
    {
        this.username = "bilkent";
        this.password = "cs421";
        this.keepSearch = true;

        // establish a connection
        try
        {
            socket = new Socket(address, controlPort);
            System.out.println("Connected!");

            // input and output init
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());

            // send the username
            out.writeBytes(generateCommand(USER, username));
            out.flush();
            System.out.println(USER + ": " + in.readLine());

            // send the password
            out.writeBytes(generateCommand(PASS, password));
            out.flush();
            System.out.println(PASS + ": " + in.readLine());

            // create data socket
            serverSocket = new ServerSocket(dataPort,0, InetAddress.getByName(address));

            // send port info
            out.writeBytes(generateCommand(PORT, dataPort + ""));
            out.flush();
            System.out.println(PORT + ": " + in.readLine());
        }
        catch(Exception e)
        {
            System.err.println(e);
        }
    }

    public void imageSearch()
    {
        if (keepSearch)
        {
            try
            {
                // show the current directory
                ArrayList<String> sharedStrings = new ArrayList<>();
                out.writeBytes(generateCommand(NLST, ""));
                out.flush();
                //System.out.println(NLST + ": " + in.readLine());

                clientSocket = serverSocket.accept();
                InputStream stream = clientSocket.getInputStream();
                byte[] dat = new byte[2];
                stream.read(dat);

                data = new BufferedReader(new InputStreamReader(stream));
                String line = data.readLine();
                if (!(dat[1] == 0 && dat[0] == 0) )
                {
                    while (line != null) {
                        sharedStrings.add(line);
                        line = data.readLine();
                    }
                    //System.out.println(sharedStrings.toString());

                    // for each item in the directory
                    for (int i = 0; i < sharedStrings.size(); i++)
                    {
                        String[] item = sharedStrings.get(i).split(":");
                        if (item[1].equals("d"))
                        {
                            out.writeBytes(generateCommand(CWD, item[0]));
                            out.flush();
                            //System.out.println(CWD + " " + item[0] + " " + in.readLine());
                            imageSearch();
                        } else if (item[0].equals("target.jpg"))
                        {
                            System.err.println("Image found!");
                            out.writeBytes(generateCommand(RETR, item[0]));
                            out.flush();
                            System.out.println(RETR + " " + item[0] + " " + in.readLine());

                            clientSocket = serverSocket.accept();
                            InputStream imageStream = clientSocket.getInputStream();

                            // get image size
                            byte[] imgSizeBytes = new byte[2];
                            imageStream.read(imgSizeBytes);
                            int imgSize = 0;
                            imgSize = (imgSize << 8) + (imgSizeBytes[0] & 0xff);
                            imgSize = (imgSize << 8) + (imgSizeBytes[1] & 0xff);

                            // read the image
                            byte[] img = new byte[imgSize];
                            imageStream.read(img);

                            // save the image
                            BufferedImage bImage2 = ImageIO.read(new ByteArrayInputStream(img));
                            ImageIO.write(bImage2, "jpg", new File("received.jpg") );
                            System.err.println("Image saved.");

                            // delete the image
                            out.writeBytes(generateCommand(DELE, item[0]));
                            out.flush();
                            System.out.println(DELE + " " + item[0] + " " + in.readLine());

                            // quit from the server and break the recursion
                            out.writeBytes(generateCommand(QUIT, ""));
                            out.flush();
                            System.out.println(QUIT + " " + in.readLine());
                            keepSearch = false;
                            return;
                        }
                    }
                }
                // change to parent's directory
                out.writeBytes(generateCommand(CDUP, ""));
                out.flush();
                //System.out.println(CDUP + ": " + in.readLine());
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
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
        SeekAndDestroy client = new SeekAndDestroy("127.0.0.1", 60001, 12345);
        client.imageSearch();
    }
}