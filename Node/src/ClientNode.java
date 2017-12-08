import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;
import java.util.Scanner;

public class ClientNode implements Runnable
{
    static String[] g_saConnectedIPs = new String[2];		// IP addresses of the neighbor nodes
	static int[] g_saConnectedPorts = new int[2];			// Ports of the neighbor nodes
	static String[] g_saContainingFiles = new String[5];	// File names of the containing files in the node
	public static ArrayList<String> alMatchingFileNameList = new ArrayList<String>();
	
	private static int g_iTimeStamp = 0;	// Logical time stamp
	private static ArrayList<Message> g_oalMessageList = new ArrayList<Message>();	// Message list in the forum
	
    DatagramSocket clientSocket;
    int listeningPort;
    InetAddress IPAddress;
    String bsIP;
    
    public static List<Neighbour> neighbours = new ArrayList<Neighbour>();	// Neighbor list of each node
    byte[] sendData;
    byte[] receiveData;
    public String nodeName = "";
    
	public ClientNode(DatagramSocket clientSocket, InetAddress IPAddress, int listeningPort, String bsIP)
	{
		this.clientSocket = clientSocket;
		this.IPAddress = IPAddress;
		this.listeningPort = listeningPort;
		this.bsIP = bsIP;
	}

	public void run()
    {	
	     			
		      
		while(true)
		{    
			sendData = new byte[1024];
			receiveData = new byte[1024];  
			
			try
			{
				// Incoming packet
				DatagramPacket incomingPacket = receivePacket(clientSocket, receiveData, receiveData.length);
		      
				byte[] data = incomingPacket.getData();
				String[] st = new String(data, 0, data.length).trim().split("\\s+");		         
				String reqResponse = st[1];
				String responseMsg = new String(data);

				// REG OK response  
				if (reqResponse.equals("REGOK"))
				{	    	     
					connectToNodes(st);			    	   
	    	     
					for(int j=0; j<g_saConnectedIPs.length; j++)
					{
						if(g_saConnectedIPs[j] != null)
						{
		    	    		 ArrayList<String> salJoinMsg = new ArrayList<String>(Arrays.asList("JOIN", this.IPAddress.getHostAddress(), Integer.toString(listeningPort)));
		    	    		 String sJoinMsg = createMessage(salJoinMsg);
		    	    		 sendPacket(clientSocket,sJoinMsg.getBytes(), sJoinMsg.length(), InetAddress.getByName(g_saConnectedIPs[j]), g_saConnectedPorts[j]);
						}
					}
	             
					// Assign files
					String sCurrentDir = System.getProperty("user.dir");
					String sFilePath = sCurrentDir + File.separator + "File Names.txt";
					ArrayList<String> alFileNames = readFileNamesFromFile(sFilePath);
					assignFiles(alFileNames);	          
				}
				// UNR OK response
				else if(reqResponse.equals("UNROK"))
				{
					System.out.println();
					System.out.println("Unregistering...");
					//clientSocket.close();
				}
				// Join message
				else if (reqResponse.equals("JOIN"))
				{	        	  
					String selectedIp1 = st[2];
					int selectedPort1 = Integer.parseInt(st[3]);
	              
					ArrayList<String> salJoinResponseMsg = new ArrayList<String>(Arrays.asList("JOINOK"));
	   		
		  		  	boolean result = neighbours.add(new Neighbour(selectedIp1, selectedPort1));
		  		  
		  		  	if(result)
		  		  	{
		  		  		salJoinResponseMsg.add("0");
		  		  	}
		  		  	else
		  		  	{
		  		  		salJoinResponseMsg.add("9999");
		  		  	}
        	  	  
		  		  	String sJoinResponseMsg = createMessage(salJoinResponseMsg);
		  		  	System.out.println();
		  		  	sendPacket(clientSocket, sJoinResponseMsg.getBytes(), sJoinResponseMsg.length(), InetAddress.getByName(selectedIp1), selectedPort1);	        	  
				}
				// JOIN OK response
				else if (reqResponse.equals("JOINOK"))
				{
        	  	  	System.out.println();
					neighbours.add(new Neighbour(incomingPacket.getAddress().getHostAddress(), incomingPacket.getPort()));	        	  
				}
				// Leave message
				else if(reqResponse.equals("LEAVE"))
				{
					String selectedIp1 = st[2];
					int selectedPort = Integer.parseInt(st[3]);
	              
					// Delete the particular neighbor from the list
					boolean result = false;
					for (int i=0; i<neighbours.size(); i++)
					{	            	    
						if (neighbours.get(i).getPort() == selectedPort)
						{
							neighbours.remove(i);
							result = true;
						}
					}           
	              
					ArrayList<String> salLeaveResponseMsg = new ArrayList<String>(Arrays.asList("LEAVEOK"));

        	  	  	if(result)
        	  	  	{
        	  	  		salLeaveResponseMsg.add("0");
        	  	  	}
        	  	  	else
        	  	  	{
        	  	  		salLeaveResponseMsg.add("9999");
        	  	  	}
	        	  
        	  	  	String sLeaveResponseMsg = createMessage(salLeaveResponseMsg);
        	  	  	sendPacket(clientSocket,sLeaveResponseMsg.getBytes(), sLeaveResponseMsg.length(), incomingPacket.getAddress(), incomingPacket.getPort());  	     
					}
				// Leave OK response
				else if(reqResponse.equals("LEAVEOK"))
				{
					System.out.println();
					System.out.println(responseMsg);
				}
				// Search message
				else if(reqResponse.equals("SER"))
				{	        	  
					String result = searchFile(st);
					String[] splittedResult = new String(result).split("\\s+");
					int serResult = Integer.parseInt(splittedResult[2]);
					int searchNodePort = Integer.parseInt(st[3]);
					String searchNodeIP = st[2];
					int hopCount = Integer.parseInt(st[st.length-1]) + 1;
	        	  
					if(serResult == 0 || serResult == 9999 || serResult == 9998)
					{
						// File not found
						// Send packets to neighbors
						for(int j=0; j<neighbours.size(); j++)
						{
								if(neighbours.get(j).getIp() != incomingPacket.getAddress().getHostAddress()
									&& neighbours.get(j).getPort() != incomingPacket.getPort()
									&& hopCount < 11)
							{
							ArrayList<String> salSearchMsg = new ArrayList<String>(Arrays.asList("SER",
																								 st[2],
																								 st[3],
																								 st[4],
																								 splittedResult[splittedResult.length-1]));
							String sSearchMsg = createMessage(salSearchMsg);
							sendPacket(clientSocket, sSearchMsg.getBytes(), sSearchMsg.length(), InetAddress.getByName(neighbours.get(j).getIp()), neighbours.get(j).getPort());
							}
						}
					}
					else
					{
						// File found
						System.out.println("File found at: " + System.nanoTime());
						String fileList = "";
						for(int i=0; i<alMatchingFileNameList.size(); i++)
						{
							fileList += alMatchingFileNameList.get(i);
						}
	        		  
						ArrayList<String> salSearchResponseMsg = new ArrayList<String>(Arrays.asList("SEROK",
        				  																			 splittedResult[2],
        				  																			 this.IPAddress.getHostAddress(),
	  																							     Integer.toString(listeningPort),
	  																							     splittedResult[5],
	  																							     fileList));
						String sSearchResponseMsg = createMessage(salSearchResponseMsg);
						sendPacket(clientSocket, sSearchResponseMsg.getBytes(), sSearchResponseMsg.length(), InetAddress.getByName(searchNodeIP), searchNodePort);		    	      
						if(searchNodePort != incomingPacket.getPort())
						sendPacket(clientSocket, sSearchResponseMsg.getBytes(), sSearchResponseMsg.length(), incomingPacket.getAddress(), incomingPacket.getPort());       	 
					}
				}
				// Search OK response
				else if(reqResponse.equals("SEROK"))
				{
					int iNoOfFiles = Integer.parseInt(st[2]);
		        	  
					if(iNoOfFiles > 0 && iNoOfFiles < 9998)
					{
						System.out.println();
						System.out.println("Searched file is found!!!");
						System.out.println("File found at: " + System.nanoTime());
						System.out.println("Details: " + responseMsg);
					}
				}
				// Comment message
				else if(reqResponse.equals("COM"))
				{
					/* Update logical time stamp */
					int iTimeStampFromMessage = Integer.parseInt(st[3]);
					if(g_iTimeStamp < iTimeStampFromMessage)
					{
						g_iTimeStamp = iTimeStampFromMessage;
					}
					
					/* Create the comment message */
					String sComment = "";
					for (int i=5; i<st.length; i++)
					{
						sComment += st[i] + " ";
					}
					sComment.trim();
					
					/* Create Message object */
					Message oMsg = new Message(st[2], iTimeStampFromMessage, sComment);
					
					/* Add message to message list */
					if(!isAlreadyAvailableComment(oMsg))
					{
						g_oalMessageList.add(oMsg);
					}
					
				
					/* Forward comment message to other nodes */			
					int iHopCount = Integer.parseInt(st[4]) + 1;	// Increment hop count
					for(int j=0; j<neighbours.size(); j++)
					{
						if(neighbours.get(j).getIp() != incomingPacket.getAddress().getHostAddress()
						   && iHopCount < 11)
						{
							ArrayList<String> salCommentMsg = new ArrayList<String>(Arrays.asList("COM",
									  												IPAddress.getHostAddress(),
									  												Integer.toString(g_iTimeStamp),
									  												Integer.toString(iHopCount),
									  												sComment));
							String sCommentMsg = createMessage(salCommentMsg);
							sendPacket(clientSocket, sCommentMsg.getBytes(), sCommentMsg.length(), InetAddress.getByName(neighbours.get(j).getIp()), neighbours.get(j).getPort());
						}
					}
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
    }
	  
	// Send datagram packet
	public static void sendPacket(DatagramSocket clientSocket, byte[] msg, int length, InetAddress ipAddress, int port)
    {
		DatagramPacket sendPacket = new DatagramPacket(msg, length, ipAddress, port);
		
		try
		{
			// Print timestamp
			/*String s = new String(msg, 0, msg.length);
		    String[] splittedString = s.split("\\s+");
			String reqMsg = splittedString[1];
			if(reqMsg.equals("SER"))
				System.out.println("Search started at "+ System.nanoTime());*/

			clientSocket.send(sendPacket);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
    }
	  
	  // Receive datagram packet
	public static DatagramPacket receivePacket(DatagramSocket clientSocket, byte[] msg, int length)
    {
		DatagramPacket receivePacket = new DatagramPacket(msg, length);
		
		try
		{
			clientSocket.receive(receivePacket);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		// Print timestamp with the message
	    byte [] data = receivePacket.getData();  
	    String s = new String(data, 0, receivePacket.getLength());
	    String[] splittedString = s.trim().split("\\s+");
		String reqResponse = splittedString[1];

		if(reqResponse.equals("SER") || reqResponse.equals("SEROK"))
			System.out.println("Received at "+ System.nanoTime() + " from "+ receivePacket.getAddress().getHostAddress() + " : " + receivePacket.getPort() + " - " + s);
		else
			System.out.println(receivePacket.getAddress().getHostAddress() + " : " + receivePacket.getPort() + " - " + s);

	    return receivePacket;
    }
	  
	public void registerNode(String nodeName)
	{	
		this.nodeName = nodeName;
		ArrayList<String> salRegMsg = new ArrayList<String>(Arrays.asList("REG", this.IPAddress.getHostAddress(), Integer.toString(this.listeningPort), nodeName));
	  	String sMsg = createMessage(salRegMsg);
	  	sendData = sMsg.getBytes();	     
	  	getIPFromString(bsIP);
  		sendPacket(clientSocket,sendData, sendData.length, getIPFromString(bsIP), 55555);      
	}
	
	private InetAddress getIPFromString(String IP)
	{	
		InetAddress IPAddress = null;
		try
		{
			IPAddress = InetAddress.getByName(IP);
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		
		return IPAddress;
	}
	  
	public void nodeLeave()
	{		  
		//To test Leave option - should send to all the neighbors
		for(int i=0; i<neighbours.size(); i++)
		{
			ArrayList<String> salLeaveMsg = new ArrayList<String>(Arrays.asList("LEAVE", this.IPAddress.getHostAddress(), Integer.toString(this.listeningPort)));
			String sLeaveMsg = createMessage(salLeaveMsg);
		  
			try
			{	        	   
				sendPacket(clientSocket, sLeaveMsg.getBytes(), sLeaveMsg.length(), InetAddress.getByName(neighbours.get(i).getIp()), neighbours.get(i).getPort());
			}
			catch (UnknownHostException e)
			{
				e.printStackTrace();
			}	
		}
                
		// Unregister from BS     
		System.out.println(nodeName);
		ArrayList<String> salUnregisterMsg = new ArrayList<String>(Arrays.asList("UNREG", this.IPAddress.getHostAddress(), Integer.toString(this.listeningPort), nodeName));
		String sUnregisterMsg = createMessage(salUnregisterMsg);     	   
		try
		{
			sendPacket(clientSocket, sUnregisterMsg.getBytes(), sUnregisterMsg.length(), InetAddress.getByName(bsIP), 55555);
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}   
	}
	  
	  /*
	 * Method					: connectToNodes
	 * Description				: Join the received neighbor nodes
	 * Parameter <p_saSplitted>	: Split list of register response command words, String[]
	 * Return					: -
	 * Created					: 2017/10/27, Rasika Bandara
	 * Updates					: -
	 */
	  private static void connectToNodes(String[] p_saSplitted)
	  {	
			
		int iNoOfNodes = Integer.parseInt((p_saSplitted[2]));
			
		if(iNoOfNodes == 0)
		{
			System.out.println("This is the initial node.");
		}
		else if (iNoOfNodes == 1)
		{
			System.out.println("Only one node in the network.");
			g_saConnectedIPs[0] = p_saSplitted[3];
			g_saConnectedPorts[0] = Integer.parseInt(p_saSplitted[4]);
		}
		else if (iNoOfNodes == 2)
		{
			System.out.println("Exactly two nodes in the network.");
			g_saConnectedIPs[0] = p_saSplitted[3];
			g_saConnectedPorts[0] = Integer.parseInt(p_saSplitted[4]);
			
			g_saConnectedIPs[1] = p_saSplitted[5];
			g_saConnectedPorts[1] = Integer.parseInt(p_saSplitted[6]);
		}
		else
		{
			System.out.println("More than two nodes in the network.");
			
			Random r = new Random();
			int iNode1 = r.nextInt(iNoOfNodes - 1) + 1;
			int iNode2 = 0;
			
			do
			{
				iNode2 = r.nextInt(iNoOfNodes - 1) + 1;
			} while (iNode2 == iNode1);
			
			g_saConnectedIPs[0] = p_saSplitted[(iNode1*2)+1];
			g_saConnectedPorts[0] = Integer.parseInt(p_saSplitted[(iNode1*2)+2]);
			
			g_saConnectedIPs[1] = p_saSplitted[(iNode2*2)+1];
			g_saConnectedPorts[1] = Integer.parseInt(p_saSplitted[(iNode2*2)+2]);
		}
	  }
	
	  /*
	   * Method					: assignFiles
	   * Description				: Assign 3-5 files to the node, from the received file name list
	   * Parameter <p_alFileNames>: Received file name list, ArrayList<String>
	   * Return					: -
	   * Created					: 2017/10/27, Rasika Bandara
	   * Updates					: -
	   */
	  private static void assignFiles(ArrayList<String> p_alFileNames)
	  {
		int iNoOfFileNamesInFile = p_alFileNames.size();
		int[] iaFileIndices = new int[5];
		
		Random r = new Random();
		iaFileIndices[0] = r.nextInt(iNoOfFileNamesInFile);
		iaFileIndices[1] = -1;
		iaFileIndices[2] = -1;
		iaFileIndices[3] = -1;
		iaFileIndices[4] = -1;
		
		do
		{
			iaFileIndices[1] = r.nextInt(iNoOfFileNamesInFile);
		} while (iaFileIndices[1] == iaFileIndices[0]);
		
		do
		{
			iaFileIndices[2] = r.nextInt(iNoOfFileNamesInFile);
		} while (iaFileIndices[2] == iaFileIndices[0] ||
				 iaFileIndices[2] == iaFileIndices[1]);
		
		do
		{
			iaFileIndices[3] = r.nextInt(iNoOfFileNamesInFile);
		} while (iaFileIndices[3] == iaFileIndices[0] ||
				 iaFileIndices[3] == iaFileIndices[1] ||
				 iaFileIndices[3] == iaFileIndices[2]);
		
		do
		{
			iaFileIndices[4] = r.nextInt(iNoOfFileNamesInFile);
		} while (iaFileIndices[4] == iaFileIndices[0] ||
				 iaFileIndices[4] == iaFileIndices[1] ||
				 iaFileIndices[4] == iaFileIndices[2] ||
				 iaFileIndices[4] == iaFileIndices[3]);
		
		int iNoOfFileNamesToAssign = r.nextInt(3) + 3;
		
		for (int i=0; i<iNoOfFileNamesToAssign; i++)
		{
			g_saContainingFiles[i] = p_alFileNames.get(iaFileIndices[i]);
		}
	  }
		
	  /*
	   * Method		: displayAssignedFiles
	   * Description	: Display file names of the assigned files to the node
	   * Parameter	: -
	   * Return		: -
	   * Created		: 2017/10/27, Rasika Bandara
	   * Updates		: -
	   */
	  public void displayAssignedFiles()
	  {
		int iAssignedFileNamesCounter = 0;
		
		for (int i=0; i<g_saContainingFiles.length; i++)
		{
			if (g_saContainingFiles[i] != null)
			{
				iAssignedFileNamesCounter++;
			}
		}
		
		System.out.println("There are " + iAssignedFileNamesCounter + " files assigned.");
		
		for (int i=0; i<iAssignedFileNamesCounter; i++)
		{
			if (g_saContainingFiles[i] != null)
			{
				System.out.println("File " + (i+1) + ": " + g_saContainingFiles[i]);
			}
		}
	  }
		
	  /*
	   * Method		: displayConnectedNodes
	   * Description	: Display IP address and port number of the neighbor nodes
	   * Parameter	: -
	   * Return		: -
	   * Created		: 2017/10/27, Rasika Bandara
	   * Updates		: -
	   */
	  public void displayConnectedNodes()
	  {
		System.out.println("You have "+ neighbours.size() + " neighbours");
		for(int i=0; i<neighbours.size(); i++)
		{
			System.out.println(neighbours.get(i).getIp() + " " + neighbours.get(i).getPort());
		}
	  }
		
	  /*
	   * Method					: readFileNamesFromFile
	   * Description				: Get the file names from the text file
	   * Parameter <p_sFilePath>	: Path of the text file, String
	   * Return					: Read file name list, ArrayList<String>
	   * Created					: 2017/10/27, Rasika Bandara
	   * Updates					: -
	   */
	  private static ArrayList<String> readFileNamesFromFile(String p_sCurrentDir)
	  {
		ArrayList<String> alFileNamesFromFile = null;
		
		try
		{
			Scanner soScanner = new Scanner(new File(p_sCurrentDir));
			alFileNamesFromFile = new ArrayList<String>();
			
			while (soScanner.hasNext())
			{
				alFileNamesFromFile.add(soScanner.nextLine().trim());
			}
			
			soScanner.close();			
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		
		return alFileNamesFromFile;
	  }
		
	  public void fileSearchOption(String fileName)
	  {
		  alMatchingFileNameList.clear();
		  System.out.println("Search started at "+ System.nanoTime());
		  ArrayList<String> salSearchMsg = new ArrayList<String>(Arrays.asList("SER",
			 																   IPAddress.getHostAddress(),
	 																   		   Integer.toString(listeningPort),
	 																   		   fileName,
	 																   		   "0"));
		  
		 
		  String sSearchMsg = createMessage(salSearchMsg);
		  String result = searchFile(sSearchMsg.split("\\s+"));
		  String[] splittedResult = new String(result).split("\\s+");
		  int serResult = Integer.parseInt(splittedResult[2]);
       	  
		  if(serResult == 0 || serResult == 9999 || serResult == 9998)
		  {      		  
			  for(int j=0; j<neighbours.size(); j++)
			  {
				  try
				  {
					  sendPacket(clientSocket,sSearchMsg.getBytes(), sSearchMsg.length(), InetAddress.getByName(neighbours.get(j).getIp()), neighbours.get(j).getPort());
				  }
				  catch (UnknownHostException e)
				  {
					  e.printStackTrace();
				  }
			  }
		  }
		  else
		  {
			  System.out.println(alMatchingFileNameList.size()+" File found at: "+ System.nanoTime());
			  for(int i=0; i<alMatchingFileNameList.size(); i++)
			  {
			  System.out.println(i+1+". "+alMatchingFileNameList.get(i));
			  }
		  }
	  }
		
		/*
		 * Method								: searchFile
		 * Description							: Search a file name in the node
		 * Parameter <p_saSERCommandSplitted>	: Split list of search command words, String[]
		 * Return								: Search response command, String
		 * Created								: 2017/10/27, Rasika Bandara
		 * Updates								: -
		 */
		public static String searchFile(String[] p_saSERCommandSplitted)
		{
			ArrayList<String> salResponseMsg = new ArrayList<String>();
			salResponseMsg.add("SEROK");
			alMatchingFileNameList.clear();
		
			try
			{			
				/*Create file name to search*/
				StringBuilder sbFileNameToSearch = new StringBuilder();
				
				for (int i=4; i<p_saSERCommandSplitted.length-1; i++)
				{
					sbFileNameToSearch.append(" ");
					sbFileNameToSearch.append(p_saSERCommandSplitted[i]);
				}
				
				String sFileNameToSearch = sbFileNameToSearch.toString().replaceAll("\"", "").trim().toLowerCase();
				
				/*Search file name*/
				for (int j=0; j<g_saContainingFiles.length; j++)
				{
					if (g_saContainingFiles[j] != null)
					{
						String sFileName = g_saContainingFiles[j].toLowerCase();
						String[] saWordsInFileName = sFileName.split("\\s+");
						String[] saWordsInQueryFileName = sFileNameToSearch.split("\\s+");
						
						for(int k=0; k<saWordsInQueryFileName.length; k++)
						{
							for(int l=0; l<saWordsInFileName.length; l++)
							{
								if (saWordsInQueryFileName[k].equals(saWordsInFileName[l]))
								{
									alMatchingFileNameList.add(g_saContainingFiles[j]);
								}
							}
						}
					}
				}
				
				/*Construct the reply message*/
				if(alMatchingFileNameList.isEmpty())
				{
					salResponseMsg.add("0");
				}
				else
				{
					/*Remove duplicates*/
					Set<String> oHashSet = new HashSet<String>();
					oHashSet.addAll(alMatchingFileNameList);
					alMatchingFileNameList.clear();
					alMatchingFileNameList.addAll(oHashSet);
					
					salResponseMsg.add(Integer.toString(alMatchingFileNameList.size()));
				}
				
				salResponseMsg.add(p_saSERCommandSplitted[2]);
				salResponseMsg.add(p_saSERCommandSplitted[3]);
				
				/*Increase hop count*/
				int iHopCount = Integer.parseInt(p_saSERCommandSplitted[p_saSERCommandSplitted.length-1]);
				iHopCount++;
				salResponseMsg.add(Integer.toString(iHopCount));
				
				if(!alMatchingFileNameList.isEmpty())
				{
					/*Add searched file names*/
					salResponseMsg.addAll(alMatchingFileNameList);
				}
			}
			catch (Exception e)
			{
				salResponseMsg.clear();
				salResponseMsg.add("SEROK");
				salResponseMsg.add("9998");
			}
			
			return createMessage(salResponseMsg);
		}
		
		/*
		 * Method					: comment
		 * Description				: Function to be executed when the user selected comment option
		 * Parameter <p_sCommentMsg>: Comment message
		 * Return					: None
		 * Created					: 2017/12/06, Rasika Bandara
		 * Updates					: -
		 */
		public void comment(String p_sCommentMsg)
		{
			g_iTimeStamp++;	// Increment logical time stamp by 1
			
			/* Add message to the forum in the current node */			
			Message oMsg = new Message(IPAddress.getHostAddress(), g_iTimeStamp, p_sCommentMsg);
			
			g_oalMessageList.add(oMsg);
			
			/* Create comment message */
			int iHopCount = 0;
			ArrayList<String> salCommentMsg = new ArrayList<String>(Arrays.asList("COM",
				   																  IPAddress.getHostAddress(),
				   																  Integer.toString(g_iTimeStamp),
				   																  Integer.toString(iHopCount),
				   																  p_sCommentMsg));

			String sCommentMsg = createMessage(salCommentMsg);
			
			/* Send message to the forums in the neighbors */
			for(int j=0; j<neighbours.size(); j++)
			{
				try
				{
					sendPacket(clientSocket,sCommentMsg.getBytes(), sCommentMsg.length(), InetAddress.getByName(neighbours.get(j).getIp()), neighbours.get(j).getPort());
				}
				catch (UnknownHostException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		/*
		 * Method					: displayForum
		 * Description				: Display the current forum in the order of the time stamp
		 * Parameter <>				: None
		 * Return					: None
		 * Created					: 2017/12/06, Rasika Bandara
		 * Updates					: -
		 */
		public void displayForum()
		{
			clearConsole();
			
			Collections.sort(g_oalMessageList, Message.COMPARE_BY_TIMESTAMP);
			
			for(int i=0; i<g_oalMessageList.size(); i++)
			{
				System.out.println(g_oalMessageList.get(i).getComment() + " (By: " + g_oalMessageList.get(i).getIp() + ", At: " + g_oalMessageList.get(i).getTimestamp() + ")");
			}
		}

		/*
		 * Method					: createMessage
		 * Description				: Create a message adding the length
		 * Parameter <p_alMsgParts>	: List of command words, ArrayList<String>
		 * Return					: Created message, String
		 * Created					: 2017/10/27, Rasika Bandara
		 * Updates					: -
		 */
		private static String createMessage(ArrayList<String> p_alMsgParts)
		{
			String sMsg = "";
			
			StringBuilder sbMsg = new StringBuilder();
			
			/*Create message from the parts received*/
			for (int i=0; i<p_alMsgParts.size(); i++)
			{
				sbMsg.append(p_alMsgParts.get(i));
				sbMsg.append(" ");
			}
			
			sMsg = sbMsg.toString().trim();
			
			// Add message length to the head
			sMsg = String.format("%04d", sMsg.length()) + " " + sMsg;
				
			return sMsg;
		}
		
		/*
		 * Method					: isAlreadyAvailableComment
		 * Description				: Check whether a comment is already in the node
		 * Parameter <p_oMessage>	: Message to be checked, Message object
		 * Return					: Availability of the comment in the node, boolean
		 * Created					: 2017/12/04, Rasika Bandara
		 * Updates					: -
		 */
		private static boolean isAlreadyAvailableComment(Message p_oMessage)
		{
			boolean bIsAlreadyAvailable = false;
					
			for (int i=0; i<g_oalMessageList.size(); i++)
			{			
				if (g_oalMessageList.get(i).getIp().equals(p_oMessage.getIp())
					&& g_oalMessageList.get(i).getTimestamp() == p_oMessage.getTimestamp())
				{
					
					bIsAlreadyAvailable = true;
					break;
				}
			}
			
			return bIsAlreadyAvailable;
		}
		
		/*
		 * Method					: clearConsole
		 * Description				: Clear console for Windows and Linux
		 * Parameter <>				: -
		 * Return					: -
		 * Created					: 2017/12/07, Rasika Bandara
		 * Updates					: -
		 */
		private final static void clearConsole()
		{		
			System.out.print("\033[H\033[2J");
			System.out.flush();
		}
}
