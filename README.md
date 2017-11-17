1. Compile the code using the following command.
	jjavac ClientNode.java ClientNode_ThreadMain.java ClientNode_ThreadNodeOperations.java Neighbour.java

2. Run the node using the following command.
	java ClientNode_ThreadMain

3. Enter the bootstrap server's IP address.

4. Enter the node's port number.

5. In the options displayed, select '1: Node Registration' by inputting 1.

6. Enter a node name.
	Here, when Enter button is pressed, following things will happen internally.
	1. Joining with the nodes sent by bootstrap server.
	2. Assigning random 3~5 files from the file name list.

7. Then, following options in the options list displayed.
   When input the number relevant to each option is input and Enter button is pressed, following things will happen.
	2: Display Connected Nodes
	- Connected nodes' (neighbors') IP addresses and port numbers will be displayed.

	3: Display Assigned Files
	- Assigned files' names will be displayed as a numbered list.

	4: Search a File
	- Enter the search query and press Enter.
	- If the searched file has been found, SEROK message will be displayed (with IP addresses and port numbers) from the nodes where the searched file exists.

	5: Leave
	- Graceful departure of the node happens.

	0: Exit
	- Exit from the application.
