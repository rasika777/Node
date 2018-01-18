import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Scanner;

public class ClientNode_ThreadMain
{
	public static DatagramSocket clientSocket;
	public static InetAddress IPv4Address;
	static int listeningPort;
	static String bsIP;    
	
 	public static void main(String args[])
    {	    
 		try
		{
 			Scanner reader = new Scanner(System.in);
 			System.out.println("Please enter Bootstrap Server IP: ");
 			String BsIP = reader.nextLine();
 			
 			System.out.println("Please enter your listening Port: ");
 			int port = reader.nextInt();
 			
 			ClientNode_ThreadMain.bsIP = BsIP;
			listeningPort = port;
			clientSocket = new DatagramSocket(listeningPort);
		    IPv4Address = getThisNodeIPv4Address();
	    }
 		catch (IOException e)
 		{
			e.printStackTrace();
		}
	 
	 	ClientNode_ThreadNodeOperations thread_class_1 = new ClientNode_ThreadNodeOperations(clientSocket, IPv4Address, listeningPort, bsIP);
	 	Thread thread_1 = new Thread(thread_class_1);
	 	thread_1.start();
        
        ClientNode dcCommandHandling = new ClientNode(clientSocket, IPv4Address, listeningPort, bsIP);
        Thread responseThread = new Thread(dcCommandHandling);
        responseThread.start();
    }
 	
	/*
	 * Method					: getThisNodeIPv4Address
	 * Description				: Get the current node's IPv4 address (Excluding localhost)
	 * Parameter <p_alMsgParts>	: -
	 * Return					: IPv4 address, InetAddress
	 * Created					: 2017/10/27, Rasika Bandara
	 * Updates					: -
	 */
	private static InetAddress getThisNodeIPv4Address()
	{
		InetAddress oIPv4Address = null;
		
		try
		{
			// Loop through all the addresses
			for(NetworkInterface oNI : Collections.list(NetworkInterface.getNetworkInterfaces()))
			{
				for(InetAddress oAddress : Collections.list(oNI.getInetAddresses()))
				{
					// Consider only IPv4
					// Remove localhost from consideration
					if(oAddress instanceof Inet4Address && !oAddress.getHostAddress().equals("127.0.0.1"))
					{
						oIPv4Address = oAddress;
					}
				}
			}
		}
		catch(SocketException e)
		{
			e.printStackTrace();
		}
		
		return oIPv4Address;
	}
}
