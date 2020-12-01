/* Name : Bhumit Pratishkmar Shah
 * ID: 1001765834 */

/* References
 * https://github.com/tejaswisingh/Multi-Client-File-Server/tree/master/src 
 * https://hub.packtpub.com/java-7-managing-files-and-directories/
 * https://stackoverflow.com/questions/36849197/send-message-to-specific-clients-using-java/49846551
 * https://stackoverflow.com/questions/20281835/how-to-delete-a-folder-with-files-using-java/29175213
 * https://www.codejava.net/java-se/file-io/how-to-rename-move-file-or-directory-in-java
 * https://stackoverflow.com/questions/5694385/getting-the-filenames-of-all-files-in-a-folder
 * https://stackoverflow.com/questions/19839172/how-to-read-all-of-inputstream-in-server-socket-java
 * https://docs.oracle.com/javase/tutorial/uiswing/components/list.html
 * https://www.journaldev.com/709/java-read-file-line-by-line#java-read-file-line-by-line-using-files
 * https://stackoverflow.com/questions/15568646/getting-back-data-from-jlist
 * https://www.techiedelight.com/periodically-execute-task-java/
 * http://www.seasite.niu.edu/cs580java/JList_Basics.htm
 * */

package lab3;

//Importing the required packages 
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.JList;

import org.apache.commons.io.FileUtils;

public class MultipleClients extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static private final String newline = "\n";

	boolean connected = false; // Variable to hold information if client is connected or not
	String uname; // For storing the user name
	static String id; //local directory id
	JButton connectButton, dirButton, deleteDirButton,renameMoveDirButton, listButton, disconnectButton,syncDirs; // Declaring Button variables
	JTextField username,dirName,delDirName,renameDirName,renameDirPath, listContent; // Declaring the Input Fields
	DataInputStream inFromServer; // Declaring Data I/O Stream variables
	DataOutputStream outToServer; // Declaring Data I/O Stream variables
	JTextArea log; // Declaring textarea variable
	Socket clSocket; // Declaring socket variable
	JFileChooser fc; // Declaring filechooser variable
	JComboBox<String> cb; // Declaring combobox variable
	FileInputStream in;  // Declaring File I/O Stream variables
	JList list; // UI Component to show home directories on server
	ArrayList<String> dirList = new ArrayList<String>(); // List to store home directories on server
	static ArrayList<String> curdirList = new ArrayList<String>(); // List of currently synchronized server home directories by client

	int i;
	
	//date format for http
 static String date=java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.systemDefault())).toString();

	@SuppressWarnings("unchecked")
	public MultipleClients() throws IOException {
		super(new BorderLayout()); // Setting layout for client GUI
		
		//Create the log object 

		log = new JTextArea(5, 20);
		log.setMargin(new Insets(5, 5, 5, 5));
		log.setEditable(false);
		JScrollPane logScrollPane = new JScrollPane(log);
		
		//Create a file chooser
		//fc = new JFileChooser();

		// Declaring buttons and adding listeners to them
		connectButton = new JButton("Connect");
		connectButton.addActionListener(this);
		
		username = new JTextField("Username");

		dirButton = new JButton("Create Directory");
		dirButton.addActionListener(this);
		
		dirName = new JTextField("Directory Name");

		listButton = new JButton("List");
		listButton.addActionListener(this);
		
		
		deleteDirButton = new JButton("Delete Directory");
		deleteDirButton.addActionListener(this);
		
		delDirName = new JTextField("Ente Directory Path for Deletion");
		
		renameMoveDirButton = new JButton("Rename Directory");
		renameMoveDirButton.addActionListener(this);
		
		renameDirName = new JTextField("Enter new name of Directory");
		renameDirPath = new JTextField("Enter path of Directory you eont to rename");
		
		listButton = new JButton("List Content");
		listButton.addActionListener(this);
		
		listContent  = new JTextField("Directory Path");
		
		disconnectButton = new JButton("Disconnect");
		disconnectButton.addActionListener(this);
		
		cb = new JComboBox<String>(); // Declaring object of combobox class
		
		//For layout purposes, put the buttons in a separate panel
		JPanel buttonPanel = new JPanel(); //use FlowLayout
		buttonPanel.add(username);
		buttonPanel.add(connectButton);
		buttonPanel.add(dirName);
		buttonPanel.add(dirButton);
		buttonPanel.add(delDirName);
		buttonPanel.add(deleteDirButton);
		buttonPanel.add(renameDirPath);
		buttonPanel.add(renameDirName);
		buttonPanel.add(renameMoveDirButton);
		buttonPanel.add(listContent);
		buttonPanel.add(listButton);
		buttonPanel.add(disconnectButton);
		
		//Showing server home directories to client on client GUI
		JPanel dirsPanel = new JPanel();
		homeDirs(); // Method to get list of server home directoires from server
		//Operations to manage directory listing on client GUI
		// https://stackoverflow.com/questions/15568646/getting-back-data-from-jlist
		// https://docs.oracle.com/javase/tutorial/uiswing/components/list.html
		String l[] = new String[dirList.size()];
		for(int j =0;j<dirList.size();j++){
			  l[j] = dirList.get(j);
		}
		list = new JList(l);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		//Sync button which on press maintains the currently sync directories by client
		syncDirs = new JButton("Sync Selected Directories");
		syncDirs.addActionListener(this);
		
		dirsPanel.add(list);
		dirsPanel.add(syncDirs);
		
		//Add the buttons and the log(textarea) to this panel.
		add(buttonPanel, BorderLayout.PAGE_START);
		add(logScrollPane, BorderLayout.CENTER);
		add(dirsPanel, BorderLayout.PAGE_END);
	}

	// Defining methods for the actions of the button declared above
	
	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource() == connectButton) { // If button pressed in connect
			try {
				connect(username.getText()); //calling function send connect request to server
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else if(e.getSource() == disconnectButton) { // If button pressed us disconnect
				try {
				exitClient(); //calling function send disconnect command to the server
			} catch (Exception e2) {
				// TODO: handle exception
			}
			
		}
		else if(e.getSource() == dirButton) { // If button pressed is create directory
			try {
				if(connected) { // Checking if client is connected
					createDir(dirName.getText(),"createDir"); //calling function to send create directory command and related data to server
				}
				else { // If not connected then telling to client to connect to server
					log.append("Please connect with the server.");
				}
				
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else if(e.getSource() == deleteDirButton) { // If button pressed in delete directory
			if(connected) { // Checking if client is connected
				try {
					deleteDir(delDirName.getText()); //calling function to send delete directory command and related data to server
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else { // If not connected then telling to client to connect to server
				log.append("Please connect with the server.");
			}
		}
		else if(e.getSource() == renameMoveDirButton) { // If button pressed is rename or move directory
			if(connected) { // Checking if client is connected
				try {
					renameDir(renameDirPath.getText(),renameDirName.getText()); //calling function to send rename or move directory command and related data to server
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else { // If not connected then telling to client to connect to server
				log.append("Please connect with the server.");
			}
		}
		else if(e.getSource() == listButton) { // If button pressed is list content of directory
			if(connected) { // Checking if client is connected
				try {
					listContentofDir(listContent.getText()); //calling function to send list content directory command and related data to server
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else { // If not connected then telling to client to connect to server
				log.append("Please connect with the server.");
			}
		}
		else if(e.getSource() == syncDirs) { // Button is pressed to maintain currently synchronized directories
			if(connected) {
				try {
					storeFile(); // Method which stores cuurently synchronized local directories information to file for persistence
					selectedDirs(); // Method which helps updating file of synchronized directories
					copy(); // Method to update content of local directory with remote directory
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else {
				log.append("Please connect with the server.");
			}
		}
	}
	
	//Method to get list of home directories from server
	public void homeDirs() throws IOException {
		Socket socket = new Socket("localhost", 1998); // Initializing Socket
		DataOutputStream op = new DataOutputStream(socket.getOutputStream()); // Opening dataoutputstream to server
		op.writeUTF("GetHomeDirs"); // Sending list contents of directory command to server
		DataInputStream in = new DataInputStream(socket.getInputStream()); // getting inputstream from server
		//reading directory listing sent by server
		boolean end = false; 
		while(!end) { 
			try {
				String inp = in.readUTF(); 
				if(inp != null) {
					dirList.add(inp); //adding directory name sent by server to list server home directories
				}
				else
					end = true;  

			}catch(Exception e) {
				break;
			}
		}
		in.close(); // closing inputstream
		op.close(); // closing outputstream
		socket.close(); // closing socket
	}
	
	//Method to store selected home directories in file
	public void storeFile() throws IOException {
		File file = new File("E:\\Lab2Folders\\"+uname+".txt"); // File object to store synchronized directories information
		if(!file.exists()) { // checking if file exists
			if(file.createNewFile()) { // If not then creating a new file
				FileWriter writer = new FileWriter("E:\\Lab2Folders\\"+uname+".txt"); // Writer object to write to file
				List<String> list1= new ArrayList<String>(); // list that stores directories selected on UI
				list1 = list.getSelectedValuesList(); // storing selected values from UI
				// Processing string for appropriate formate to store in the file
				String tempList = list1.toString();
				String tempList1 = tempList.replace("[","");
				String tempList2 = tempList1.replace("]", "");
				String tempList3 = tempList2.replaceAll("\\s", "");
				//Writing selected dirctories information to file 
				writer.append(id + "," + tempList3);
				writer.append(System.getProperty( "line.separator" ));
				writer.flush();
				writer.close();
			}
		}
		else {
			FileWriter writer = new FileWriter("E:\\Lab2Folders\\"+uname+".txt"); // Writer object to write to file
			List<String> list1= new ArrayList<String>(); // list that stores directories selected on UI
			list1 = list.getSelectedValuesList(); // storing selected values from UI
			// Processing string for appropriate formate to store in the file
			String tempList = list1.toString();
			String tempList1 = tempList.replace("[","");
			String tempList2 = tempList1.replace("]", "");
			String tempList3 = tempList2.replaceAll("\\s", "");
			//Writing selected dirctories information to file 
			writer.append(id + "," + tempList3);
			writer.append(System.getProperty( "line.separator" ));
			writer.flush();
			writer.close();

		}
	}
	
	// method to exit the server operation
	public void exitClient()throws Exception{				
		// --	
		Socket socket = new Socket("localhost", 1998); // Initializing Socket
		DataOutputStream op = new DataOutputStream(socket.getOutputStream()); // Opening dataoutputstream to server
		op.writeUTF("Disconnect"); // Sending disconnect command to server
		op.writeUTF(uname); // Sending username to server
		DataInputStream in = new DataInputStream(socket.getInputStream()); // getting inputstream from server
		String r = in.readUTF(); // reading message from server
		log.append(r+newline); // writing message to client log 
		int i = 0;
		while(i<1000) { // loop to delay the exit so that client can see the message.
			i++;
		}
		System.exit(0);										// close the connection to the server
	}
	
	//Method to Connect Client
	public void connect(String uname) throws IOException{
		
		Socket socket = new Socket("localhost", 1998); // Initializing Socket
		DataOutputStream op = new DataOutputStream(socket.getOutputStream()); // Opening dataoutputstream to server
		op.writeUTF("Connect"); // Sending connect command to server
		op.writeUTF(uname); //Sending username to server
		DataInputStream in = new DataInputStream(socket.getInputStream());  // getting inputstream from server
		String r = in.readUTF(); // reading message from server
		if(r.equals("You are connected.")) { // Checking if connection is succcessfull
			connected = true; // setting connection to true suggesting client is connected
			
			this.uname  = uname; // setting object's username to input username 
		}
		String ld = in.readUTF();
		id = ld;
		selectedDirs();
		copy();
		log.append(r+newline); // writing message to client log
		in.close(); //closing inputstream
		op.close(); //closing outputstream
		socket.close(); // closing socket
		
	}
	
	//selected Directories in the list
	public void selectedDirs() throws IOException {
		File file = new File("E:\\Lab2Folders\\"+uname+".txt"); // File object of client locad directory information
		List<String> allLines; // Object to read lines of file
		if(file.exists()) { // If file exists
			// https://www.journaldev.com/709/java-read-file-line-by-line#java-read-file-line-by-line-using-files
			allLines = Files.readAllLines(Paths.get("E:\\Lab2Folders\\"+uname+".txt")); // Reading lines from file
			String[] a = allLines.get(0).split(",");
			id = a[0]; //Assigning Local Directory ID to current client retrieved from file
			//Deleting Files old sync directries in local directories
			deleteDirs();
		
			//Creating latest list of sync directories
			curdirList.clear();
			for(int i=1;i<a.length;i++) {
				curdirList.add(a[i]);
			}
			
			//Updating UI selection to latest information from file
			list.clearSelection();
			ListModel model = list.getModel();
			List<Integer> b = new ArrayList<Integer>(); 
			for(int i=0;i<model.getSize();i++) {
				if(curdirList.contains(model.getElementAt(i))) {
					b.add(i);
				}
			}
			int c[] = new int[b.size()];
			for(int i=0;i<b.size();i++) {
				c[i] = b.get(i); 
			}
			list.setSelectedIndices(c);
		}

	}
	
	//delete directories for update purpose local directories
	public void deleteDirs() throws IOException {
		for(int i=0;i<curdirList.size();i++) {
			File file2 = new File("E:\\Lab2Folders\\"+id+"\\"+curdirList.get(i));
			FileUtils.deleteDirectory(file2); // Deleting local directories
		}
		
	}
	
	//Copy latest copies of selected directories to local directories
	public void copy() throws IOException {

		//delete directories for update purpose local directories
		for(int i=0;i<curdirList.size();i++) {
			File file2 = new File("E:\\Lab2Folders\\"+id+"\\"+curdirList.get(i));
			FileUtils.deleteDirectory(file2); // Deleting local directories
		}
		
		for(int i=0;i<curdirList.size();i++) {
			File file2 = new File("E:\\Lab2Folders\\"+id+"\\"+curdirList.get(i)); //File object to create latest copy of directory
			File file3 = new File("E:\\Lab1Folders\\"+curdirList.get(i)); // File object Address of remote directory on server
			file2.mkdir(); // creating to new directory
			FileUtils.copyDirectory(file3, file2); // Copying contents from server directory to client local directory
		}

	}
	//Method to send create directory command
	public void createDir(String dirName,String cmd) throws UnknownHostException, IOException {
		Socket socket = new Socket("localhost", 1998); // Initializing Socket
		DataOutputStream op = new DataOutputStream(socket.getOutputStream()); // Opening dataoutputstream to server
		op.writeUTF(cmd); // Sending create directory command to server
		op.writeUTF(this.uname); // Sending username to server
		op.writeUTF(dirName); // Sending directory name to be created to server
		DataInputStream in = new DataInputStream(socket.getInputStream()); // getting inputstream from server
		String r = in.readUTF(); // reading message from server
		log.append(r+newline); // writing message to client log
		in.close(); //closing inputstream
		op.close(); //closing outputstream
		socket.close(); // closing socket
	}
	
	//Method to send delete Directory command
	public void deleteDir(String delDirName) throws UnknownHostException, IOException {
		Socket socket = new Socket("localhost", 1998); // Initializing Socket
		DataOutputStream op = new DataOutputStream(socket.getOutputStream()); // Opening dataoutputstream to server
		op.writeUTF("DeleteDir"); // Sending delete directory command to server
		op.writeUTF(this.uname); // Sending username to server
		op.writeUTF(delDirName); // Sending directory name to be deleted to server
		DataInputStream in = new DataInputStream(socket.getInputStream()); // getting inputstream from server
		String r = in.readUTF(); // reading message from server
		log.append(r+newline); // writing message to client log
		in.close(); //closing inputstream
		op.close(); //closing outputstream
		socket.close(); // closing socket
		
	}
	
	//Method to send rename and move  directory command
	public void renameDir(String dirPath,String newDirName) throws UnknownHostException, IOException {
		Socket socket = new Socket("localhost", 1998); // Initializing Socket
		DataOutputStream op = new DataOutputStream(socket.getOutputStream()); // Opening dataoutputstream to server
		op.writeUTF("RenameDir"); // Sending rename directory command to server
		op.writeUTF(this.uname); // Sending username to server
		op.writeUTF(dirPath); // Sending source directory name to be moved or renamed to server
		op.writeUTF(newDirName); // Sending destination directory name to server
		DataInputStream in = new DataInputStream(socket.getInputStream()); // getting inputstream from server
		String r = in.readUTF(); // reading message from server
		log.append(r + newline); // writing message to client log
		in.close(); //closing inputstream
		op.close(); //closing outputstream
		socket.close(); // closing socket
	}
	
	public void listContentofDir(String dirPath) throws UnknownHostException, IOException {
		Socket socket = new Socket("localhost", 1998); // Initializing Socket
		DataOutputStream op = new DataOutputStream(socket.getOutputStream()); // Opening dataoutputstream to server
		op.writeUTF("ListContentsDir"); // Sending list contents of directory command to server
		op.writeUTF(this.uname); // Sending username to server
		op.writeUTF(dirPath); // Sending source directory name to server
		DataInputStream in = new DataInputStream(socket.getInputStream()); // getting inputstream from server
		// https://stackoverflow.com/questions/19839172/how-to-read-all-of-inputstream-in-server-socket-java
		boolean end = false; // for reading listing of files from server
		while(!end) { // loop to read until there are no more messages from server
			String inp = in.readUTF(); // Reading message from server
			if(inp != null) {
				log.append(inp + newline); // writing filename to client log
			}
			else
				end = true; // setting to true if there no more messages from server 
		}
		in.close(); // closing inputstream
		op.close(); // closing outputstream
		socket.close(); // closing socket
		
	}
	
	
//Function to upload files to the server
//	public void send() throws IOException {
//		File file = fc.getSelectedFile();  // getting the selected file to upload to server
//		Socket socket = new Socket("localhost", 1988); // socket connection
//
//		DataOutputStream dos = new DataOutputStream(socket.getOutputStream()); // Initializing data I/O streams
//		FileInputStream fis = new FileInputStream(file);
//		byte[] buffer = new byte[4096]; // Defining the size of the buffer 
//		dos.writeUTF(file.getName());
//		while (fis.read(buffer) > 0) {
//			dos.write(buffer);
//		}
//		log.append("File " +file.getName()+" uploaded successfully"+ newline); // Message to client that the  file is uploaded successfully
//		String msg= "File " +file.getName()+" uploaded successfully"+ newline; // Taking message into a variable to calculate length of the message
//		int res=msg.length(); // Calculating length of the message
//		// http string passed to the client
//		String httpMsg="\nPOST HTTP/1.1\n"+"Host: http://127.0.0.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
//				"Content-Length:"+res+"\nUser-Agent: Multi-Client File Server Application\n";
//		log.append(httpMsg + newline);
//		dos.flush();
//	}
//
////Function to list files uploaded to the server
//	public void listOfFiles() throws IOException {
//		File folder = new File("C:/Users/tejas/Desktop/Server/"); // Static directory where the files are  uploaded
//		File[] listOfFiles = folder.listFiles();
//		cb.removeAllItems();
//		// loop over all the files uploaded to the server
//		for (int i = 0; i < listOfFiles.length; i++) { 
//			if (listOfFiles[i].isFile()) {
//				log.append(listOfFiles[i].getName()+ newline); // List of files displayed to client
//				String msg = listOfFiles[i].getName()+ newline; // Taking message into a variable to calculate length of the message
//				int res=msg.length(); // Calculating length of the message
//				// http string passed to the client
//		    	String httpMsg="\nPOST HTTP/1.1\n"+"Host: http://127.0.0.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
//						"Content-Length:"+res+"\nUser-Agent: Multi-Client File Server Application\n";
//				log.append(httpMsg + newline);
//				cb.addItem(listOfFiles[i].getName());
//			} 
//		}
//		
//	}
	
//Function to download files to the server	
//	public void downloadFile() throws IOException {
//	    String selectedBook = (String) cb.getSelectedItem();
//	    
//	    InputStream is = null;
//	    OutputStream os = null;
//	    try {
//	    	File source = new File("C:/Users/tejas/Desktop/Server/"+selectedBook); // Static directory where the files are  uploaded
//	    	File dest = new File("C:/Users/tejas/Desktop/Download/"+selectedBook); // Static directory where the files are  downloaded
//	    	FileUtils.copyFile(source, dest);
//	    	log.append("File " +selectedBook+" downloaded successfully"+ newline); // Message to client that the  file is downloaded successfully
//	    	String msg= "File " +selectedBook+" downloaded successfully"+ newline; // Taking message into a variable to calculate length of the message
//	    	int res=msg.length(); // Calculating length of the message
//			// http string passed to the client
//	    	String httpMsg="\nPOST HTTP/1.1\n"+"Host: http://127.0.0.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
//					"Content-Length:"+res+"\nUser-Agent: Multi-Client File Server Application\n";
//			log.append(httpMsg + newline);
//	    	
//	    }catch(Exception e){
//	    	
//	    } finally {
//			
//			// closing I/O streams
//	        is.close(); 
//	        os.close();
//	    }		
//	}
	    
	    
	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 * @throws IOException 
	 */
	private static void createAndShowGUI() throws IOException {
		//Make sure we have nice window decorations.
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);

		//Create and set up the window.
		JFrame frame = new JFrame("MultipleClient");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Create and set up the content pane.
		JComponent newContentPane = new MultipleClients();
		newContentPane.setOpaque(true); //content panes must be opaque
		frame.setContentPane(newContentPane);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
		//Code to get any update from server directory to update local directory periodically
		// https://www.techiedelight.com/periodically-execute-task-java/
		Timer timer = new Timer(); // Timer object
		timer.schedule(new TimerTask() { // Scheduling task for getting regular updates
            @Override
            public void run() {
            	//Code to manage updates on local directory same as copy method above(Refer to copy method)
        		for(int i=0;i<curdirList.size();i++) {
        			File file2 = new File("E:\\Lab2Folders\\"+id+"\\"+curdirList.get(i));
        			try {
						FileUtils.deleteDirectory(file2);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		}
        		
        		for(int i=0;i<curdirList.size();i++) {
        			File file2 = new File("E:\\Lab2Folders\\"+id+"\\"+curdirList.get(i));
        			File file3 = new File("E:\\Lab1Folders\\"+curdirList.get(i));
        			file2.mkdir();
        			try {
						FileUtils.copyDirectory(file3, file2);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		}
                
            }
        }, 0, 5000);
	}

	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					createAndShowGUI();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
}
