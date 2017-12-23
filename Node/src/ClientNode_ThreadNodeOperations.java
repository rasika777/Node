import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
	
class ClientNode_ThreadNodeOperations implements Runnable
{	
 	int listeningPort;
    DatagramSocket clientSocket;
    InetAddress IPAddress;
    String bsIP;    
	    
	public ClientNode_ThreadNodeOperations(DatagramSocket clientSocket, InetAddress IPAddress, int listeningPort, String bsIP)
	{
		this.clientSocket = clientSocket;
		this.IPAddress = IPAddress;
		this.listeningPort = listeningPort;
		this.bsIP = bsIP;
	}
	
	public void run()
    {
		int m_iUserChoice = -1;
	
		Scanner reader = new Scanner(System.in);
		String modifiedSentence = "";
      
		ClientNode dcCommandHandling = new ClientNode(clientSocket, IPAddress, listeningPort, bsIP);
      
		while(true)
		{
	  		System.out.println("Select the action you want to perform:");
			System.out.println("1: Node Registration");
			System.out.println("2: Display Connected Nodes");
			System.out.println("3: Display Assigned Files");
			System.out.println("4: Search a File");
			System.out.println("5: Comment");
			System.out.println("6: Rank a Comment");			
			System.out.println("7: Rank a File");
			System.out.println("8: Display Forum");
			System.out.println("9: Leave");
			System.out.println("0: Exit");
			System.out.println("");
		
			System.out.print("Insert your choice: ");
					
			m_iUserChoice = reader.nextInt();	
		
			switch (m_iUserChoice)
			{
				case 0:
					System.out.println("Exit...");
					reader.close();
					System.exit(0);
					break;
				case 1:
					System.out.println("1: Node Registration is selected");
					System.out.println("Enter node name: ");
					String sNodeName = reader.next().trim();
					dcCommandHandling.registerNode(sNodeName);
					break;
				case 2:
					System.out.println("2: Display Connected Nodes is selected");
					dcCommandHandling.displayConnectedNodes();
					break;
				case 3:
					System.out.println("3: Display Assigned Files is selected");
					dcCommandHandling.displayAssignedFiles();
					break;
				case 4:
					System.out.println("4: Search a File is selected");
					System.out.println("Enter file name to search: ");
					Scanner soReadFileName = new Scanner(System.in);
					String sFileName = soReadFileName.nextLine();
					dcCommandHandling.fileSearchOption("\"" + sFileName + "\"");
					break;
				case 5:
					System.out.println("5: Comment is selected");
					System.out.println("Enter comment message: ");
					Scanner soReadCommentMsg = new Scanner(System.in);
					String sCommentMsg = soReadCommentMsg.nextLine();
					dcCommandHandling.comment("\"" + sCommentMsg + "\"");
					break;
				case 6:
                                        System.out.println("6: Rank a Comment is selected");
					System.out.println("Enter Comment sent IP: ");
                                        Scanner ipSent = new Scanner(System.in);

					System.out.println("Enter ID : ");
					Scanner commentId = new Scanner(System.in);
                                        
                                        System.out.println("Enter Rank : ");
                                        Scanner rank = new Scanner(System.in);
                                        
                                        
					String ipGiven = ipSent.nextLine();
                                        String idGiven = commentId.nextLine();
                                        
  
					String rankGiven = rank.nextLine();
					int rankValue = Integer.parseInt(rankGiven);
                                        
                                        String key = ipGiven+":"+idGiven;
                                                
			
					dcCommandHandling.rank(key,rankValue, 2);
					break;
				case 7:
					System.out.println("7: Rank a File is selected");
					System.out.println("Enter File Name to Rank: ");
					Scanner fileNameGivenSc = new Scanner(System.in);
					String fileNameGiven = fileNameGivenSc.nextLine();
					System.out.println("Enter Rank : ");
					Scanner rankGivenSc = new Scanner(System.in);
					String fileRankGiven = rankGivenSc.nextLine();
					int filRankValue = Integer.parseInt(fileRankGiven);
					dcCommandHandling.rank(fileNameGiven,filRankValue, 1);
					break;
				case 8:
					System.out.println("8: Display Forum is selected");
					dcCommandHandling.displayForum();
					break;
				case 9:
					System.out.println("9: Leave is selected");
					dcCommandHandling.nodeLeave();
					break;
				default:
					System.out.println("Invalid input. Please select again.");
					break;
			}
			
			System.out.println("");
			System.out.println("FROM SERVER:" + modifiedSentence);
		}
    }
}
