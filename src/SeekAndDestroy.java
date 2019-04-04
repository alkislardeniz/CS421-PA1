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

    // create controlSocket, input and output stream
    private Socket controlSocket;
    private BufferedReader controlReader;
    private DataOutputStream out;
    private BufferedReader dataReader;
    private Socket dataSocket;
    private ServerSocket serverSocket;

    private final String username = "bilkent";
    private final String password = "cs421";
    private boolean keepSearch;

    // constructor
    public SeekAndDestroy(String address, int controlPort, int dataPort)
    {
        this.keepSearch = true;

        // establish a connection
        try
        {
            controlSocket = new Socket(address, controlPort);
            System.out.println("Connected!");

            // input and output init
            controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
            out = new DataOutputStream(controlSocket.getOutputStream());

            // send the username
            out.writeBytes(generateCommand(USER, username));
            out.flush();
            System.out.println(USER + ": " + controlReader.readLine());

            // send the password
            out.writeBytes(generateCommand(PASS, password));
            out.flush();
            System.out.println(PASS + ": " + controlReader.readLine());

            // create dataReader controlSocket
            serverSocket = new ServerSocket(dataPort,0, InetAddress.getByName(address));

            // send port info
            out.writeBytes(generateCommand(PORT, dataPort + ""));
            out.flush();
            System.out.println(PORT + ": " + controlReader.readLine());
        }
        catch(Exception e)
        {
            System.err.println(e);
        }
    }

    public void closeSockets()
    {
        try
        {
            dataSocket.close();
            serverSocket.close();
            Thread.sleep(250);
            controlSocket.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
                //System.out.println(NLST + ": " + controlReader.readLine());

                dataSocket = serverSocket.accept();
                InputStream stream = dataSocket.getInputStream();
                byte[] dat = new byte[2];
                stream.read(dat);

                dataReader = new BufferedReader(new InputStreamReader(stream));
                String line = dataReader.readLine();
                if (!(dat[1] == 0 && dat[0] == 0) )
                {
                    while (line != null) {
                        sharedStrings.add(line);
                        line = dataReader.readLine();
                    }
                    //System.out.println(sharedStrings.toString());

                    // for each item check for a directory
                    for (int i = 0; i < sharedStrings.size() && keepSearch; i++)
                    {
                        String[] item = sharedStrings.get(i).split(":");
                        if (item[1].equals("d"))
                        {
                            out.writeBytes(generateCommand(CWD, item[0]));
                            out.flush();
                            //System.out.println(CWD + " " + item[0] + " " + controlReader.readLine());
                            imageSearch();
                        } else if (item[0].equals("target.jpg"))
                        {
                            System.err.println("Image found!");
                            out.writeBytes(generateCommand(RETR, item[0]));
                            out.flush();
                            System.out.println(RETR + " " + item[0] + " " + controlReader.readLine());

                            dataSocket = serverSocket.accept();
                            InputStream imageStream = dataSocket.getInputStream();

                            // get image size
                            byte[] imgSizeBytes = new byte[2];
                            imageStream.read(imgSizeBytes);
                            int imgSize = 0;
                            imgSize = (imgSize << 8) + (imgSizeBytes[0] & 0xff);
                            imgSize = (imgSize << 8) + (imgSizeBytes[1] & 0xff);

                            // read the image
                            byte[] imgBytes = new byte[imgSize];
                            imageStream.read(imgBytes);

                            // save the image
                            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imgBytes));
                            ImageIO.write(img, "jpg", new File("received.jpg") );
                            System.err.println("Image saved.");

                            // delete the image
                            out.writeBytes(generateCommand(DELE, item[0]));
                            out.flush();
                            System.out.println(DELE + " " + item[0] + " " + controlReader.readLine());

                            // quit from the server and break the recursion
                            out.writeBytes(generateCommand(QUIT, ""));
                            out.flush();
                            System.out.println(QUIT + " " + controlReader.readLine());
                            keepSearch = false;
                            return;
                        }
                    }
                }
                // change to parent's directory
                out.writeBytes(generateCommand(CDUP, ""));
                out.flush();
                //System.out.println(CDUP + ": " + controlReader.readLine());
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
        if (args.length != 2)
        {
            System.out.println("Invalid format: SeekAndDestroy <Addr> <Port>");
            return;
        }
        String host = args[0];
        int controlPort = Integer.parseInt(args[1]);
        SeekAndDestroy client = new SeekAndDestroy(host, controlPort, 12345);
        client.imageSearch();
        client.closeSockets();
    }
}