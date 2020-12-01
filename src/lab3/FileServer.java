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

//Importing Required JAVA libraries
import java.awt.FlowLayout;
//Importing the required packages 
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;


import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;

import org.apache.commons.io.FileUtils;


public class FileServer extends Thread implements ActionListener {


	private ServerSocket ss; // Declaring socket variable
	
	// Declaring static variables
	
	static int i=0;
	static String[] ids = {"A","B","C"}; // For assigning local directories to client
	static int incr = -1; // incrementor which usd to select element from ids to select local directory
	static private final String newline = "\n"; // For newline on log
	static TextArea area; // For log area on GUI
	static JButton closeBtn,undoBtn; // For exit button on GUI
	static JList clist; //UI components which maintains the list of the connected clients
	static JList loglist;
	static ArrayList<String> clientList = new ArrayList<String>(); // List to maintain connected client to server
	static DefaultListModel listModel; // ListModels to manipulate clist i.e. client list
	static DefaultListModel logModel;
	static List<String> logs;
	File file = new File("E:\\Lab1Folders\\logs.txt"); // Object of log file path 
	
	// Declaring default constructor which is called in main method to construct the GUI
	FileServer(){  
		JFrame f = new JFrame(); // Declaring the frame object and designing the GUI
	    area = new TextArea();  // Instantiating TextArea for log 

	    closeBtn = new JButton(); // Instantiating exit button
	    closeBtn.setText("Close"); // Setting text for the exit button
	    closeBtn.addActionListener(this); // adding actionlistener to exit button

	    undoBtn = new JButton(); // Instantiating Undo button
	    undoBtn.setText("Undo"); // Setting text for the Undo button
	    undoBtn.addActionListener(this); // adding actionlistener to Undo button
	    
	    
	    listModel = new DefaultListModel(); // // Instantiating list model
	    logModel = new DefaultListModel(); // // Instantiating list model
	    
	    clist = new JList(listModel); // // Instantiating clist (client list UI Component)
	    loglist = new JList(logModel);
	    area.setBounds(20,100,300,300);  // Setting position and dimensions for log on GUI
	    loglist.setBounds(20, 450, 350, 300);
	    closeBtn.setBounds(20,5,80,30); // Setting position and dimensions for exit button on GUI
	    undoBtn.setBounds(120, 5, 80, 30);
	    clist.setBounds(40,40,50,50); // Setting position and dimensions for the client list on GUI
	    
	    
	    f.add(area); // Adding log area to GUI
	    f.add(loglist); // Adding log for file operations to GUI
	    f.add(closeBtn); // Adding exit button to GUI
	    f.add(undoBtn); // Adding Undo Button to GUI
	    f.add(clist); // Adding connected client list UI Component to GUI
	    
	    try {
			logs = Files.readAllLines(Paths.get("E:\\Lab1Folders\\logs.txt"));
			for(int i = 0; i< logs.size();i++) {
				logModel.addElement(logs.get(i));
			}
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    
	    f.setSize(400,850);  // Setting size(Height and Width) of the GUI
	    f.setLayout(null);  // Setting Layout to null
	    f.setVisible(true); //Setting visibility to true	
	}
	
	public FileServer(int port) {
		try {
			ss = new ServerSocket(port); // Defining the port on which the file server will run
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == closeBtn) {
			clientList.clear(); // emptying clientlist of connected clients before closing the server
			System.exit(1); // killing the server process 
		}
		else if(e.getSource() == undoBtn) {
			//function to undo ops
			undoOps();
		}
	}

	// run method to establish connection with the client and calling saveFile method to upload the files to the server
	public void run() {
		while (true) {
			try {
				
				i++; 
				runCommands(); // Calling function which handle the commands sent by client
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	//Function to handle commands sent by client
	// https://stackoverflow.com/questions/36849197/send-message-to-specific-clients-using-java/49846551
	public void runCommands() throws IOException {
		
		Socket clientSock = ss.accept(); // Accepting connection from client
		if(!file.exists()) // checking if log file exists
			file.createNewFile(); // Creating log file if does not exists
		DataOutputStream oos = new DataOutputStream(clientSock.getOutputStream()); // Initializing outputstream to send back messages to client
		InputStream in = clientSock.getInputStream(); // Intializing inputstream to read messages sent by client
		DataInputStream ip = new DataInputStream(in); // Instantiating data input stream
		String cmd = ip.readUTF(); // Reading command sent by client
			if(cmd.equals("Connect")) {  // Checking if command is to connect by client
				int i = connectClient(clientSock);  // Calling connectClient function which has all routiones to connect client to server and returns the error or success code
				if(i == 0) { // If Code is 0 then it is success
					oos.writeUTF("You are connected."); // Sending message to client that he is successfully connected to server
					//To give client local directory id incr is used on the server to keep local dirctory id
					// Rotating Among A,B,B and send it bak to client
					if(incr <= 3) {
						incr++;
					}
					else
						incr = 0;
					oos.writeUTF(ids[incr]);

				}
				else if(i == 1){ // If Code is 1 then Username formate given by client is not proper
					oos.writeUTF("Username is not valid"); // Sending client message that username formate is not valid 
				}
				else if(i == 2){ // If Code is 2 then it suggests that client with the username is already is connected
					oos.writeUTF("Username is alredy taken."); // // Sending client message that username is alredy in use so that he chooses different username
				}
				else if(i == 3){ // If Code is 3 then it suggests that no more than 3 client can connect to server 
					oos.writeUTF("Cannot have more than 3 clients."); // Telling client that he can't connect becuase server already has 3 client connected.
				}
				
			}
			else if(cmd.equals("createDir")) { // Checking if command is to create a directory in clients home directory
				int i = createDirectory(clientSock); //Calling function which creates direcory for client and returns error or success code
				if( i == 0) { // Code 0 suggest that directory is create successfully
					oos.writeUTF("Directory created."); // Sending client message that directory is created successfully.
				}
				else if(i == 1) { // Code 1 suggests that there was some error during the creation of directory
					oos.writeUTF("Failed to create directory."); // Sending client message the server was unable to create directory
				}
				else if(i == 2) { // Code 2 suggests that directory with the same name alread exists
					oos.writeUTF("Directory already exits."); // Sending client message that directory  with the same directory exists
				}
			}
			else if(cmd.equals("DeleteDir")) { // Checking is command is to delete directory by client
				int i = deleteDir(clientSock); // Calling function which deletes the directory for client and returning error or success code
				if(i == 0) { // Code 0 suggest that directory deletion was successfull
					oos.writeUTF("Directory deleted successfully."); // Sending client message that directory deleted successfully
				}
				else if(i == 1) { // Code 1 suggests that directory requested by client to delete does not exist 
					oos.writeUTF("Directory does not exist."); // Sending client message that directory with the name does not exist
				}
			}
			else if(cmd.equals("RenameDir")) { // Check is command is to move or rename directory
				int i = renameDir(clientSock); // Calling function which renames or moves direcory to desired location and returning error or success code
				if(i == 0) { // Code 0 suggests that directory moved or renamed successfully
					oos.writeUTF("Directory renamed or moved successfully."); // Sending client message that directory successfully moved or renamed
				}
				else if(i == 1) { // Code 1 suggests that there was some error in moving or renaming directory
					oos.writeUTF("Failed to rename or move directory."); // Sending client message that unable to do some operation.
				}
				else { // Code 2 suggests that directory does not exist
					oos.writeUTF("Directory does not exist."); // Sending client message that direcory with provided name does not exist.
				}
			}
			else if(cmd.equals("ListContentsDir")) { // Checking if command is to list the directory contents.
				int i = listFiles(clientSock); // Calling function which lists the files and returning the error or success code
				if(i == 0) { // Code 0 suggests that File listing is successfull
					oos.writeUTF("End of list"); 
				}
				else { // Code 1 suggests that directory with provided path does not exist
					oos.writeUTF("Directory does not exist."); //Sending client message that directory given by him does not exist.
				}
			}
			else if(cmd.equals("GetHomeDirs")) {
				getHome(clientSock);
			}
			else { // Following code disconnects client from the server if client command is to disconnect 
				int i = disconnect(clientSock); // Calling functions which disconnects client from the server and returns error and success code
				if(i == 0) { // Code 0 suggests that disconnection was successfull.
					oos.writeUTF("You are disconnected."); // Sending client message that he has been disconnected.
				}
				else { // Following code suggest that there was some error in disconnection
					oos.writeUTF("Unable to disconnect."); // Sending client message that there was some in disconnection process 
				}
			}
		
		clientSock.close(); // Closing the client socket

	}
	
	//Method to connect client and create his/her home directory
	public int connectClient(Socket clientSock) throws IOException {
		int connected = 0; // for error or success code purposes
		InputStream in = clientSock.getInputStream(); // Creating inputstream from client
		DataInputStream ip = new DataInputStream(in); // Creating datainputstearm to read data from client
		String username = ip.readUTF(); // Reading client username

		//checking if no. of clients are less than 3
		if(clientList.size() < 3) {
			//checking if username already exists
			if(!clientList.contains(username)) {
				//checking validity(format) of the username.
				if(username.matches("[a-zA-Z]{3}\\d{3}")) {
					clientList.add(username); // If all is well than adding client username to connected client list
					connected = 0; // Setting code to 0 which suggests that all operations were successful.
					area.append("Client: "+ username + "  Connected"+ newline); // Writing to server log that client with this username has connected.
					String path = "E:\\Lab1Folders\\"+username; // Creating path for client home directory 
					File dir = new File(path); // Initializing file object with the path to create client home directory
					if(!dir.exists()) { // Checking if home directory for the client already exists
						dir.mkdir(); // Creating client home directory
					}
					//http://www.seasite.niu.edu/cs580java/JList_Basics.htm
					listModel.addElement(username); // Adding newly connected client on UI client list

				}
				else {
					connected = 1; // This code suggests that client username formate is not proper.
				}
			}
			else {
				connected = 2; // This code suggests that client with the username already exists 
			}
		}
		else {
			connected = 3; // This code suggests that there are already 3 clients connected.
		}
		return connected; // Returning error or success codes
		
	}
	
	//Method to disconnect the client from the server
	public int disconnect(Socket clientSock) throws IOException {
		int result = 0; // Error code
		InputStream in = clientSock.getInputStream(); // Creating client inputstream
		DataInputStream ip = new DataInputStream(in); // Creating client datainputstream to read message from the client
		String uname = ip.readUTF(); // Reading the client username
		if(clientList.remove(uname)) { // Removing client username from connected client list and if successful setting error code to 0;
			result = 0; // Setting error code to 0;
			area.append(uname + " disconnected" + newline); // Log on server GUI that user is disconnected
			//http://www.seasite.niu.edu/cs580java/JList_Basics.htm
			listModel.removeElement(uname); // Removing disconnected client from UI client list
		}
		else {
			result = 1; // Setting error code to 1 if unsuccessful in removig client from the list.
		}
		
		return result; // Returning the error code
	}

	// Method to create directory for client
	public int createDirectory(Socket clientSock) throws IOException {
		int result = 0; // Error code initialization
		InputStream in = clientSock.getInputStream(); // Getting inputstream from client
		DataInputStream ip = new DataInputStream(in); // Creating datatinputstream to read data from client
		String uname = ip.readUTF(); // Reading client username
		String dirname = ip.readUTF(); // Reading dirpath to be created
		String path = "E:\\Lab1Folders\\"+ uname + "\\" + dirname; // Setting path to create directory
		File dir = new File(path); // Creating file object to create directory
		if(!dir.exists()) { // checking is directory already exists
			if(dir.mkdir()) { // Creating the new directory requested by client
				result = 0; // Setting error code 0 suggesting success.
				FileWriter writer = new FileWriter("E:\\Lab1Folders\\logs.txt",true);
				writer.append("CreateDir : " + dirname + " at " + path);
				writer.append(System.getProperty( "line.separator" ));
				writer.flush();
				writer.close();
			}
			else { 
				result = 1; // Setting error code 1 suggesting there was some error in creating the directory
			}
		}
		else {
			result = 2; // Setting error code 2 suggesting directory already exists
		}
		return result; // Returning error code

	}
	
	//Method to Delete Directory for the client
	public int deleteDir(Socket clientSock) throws IOException {
		int result = 0; // Error code initialization
		InputStream in = clientSock.getInputStream(); // Getting inputstream from client
		DataInputStream ip = new DataInputStream(in); // Creating datatinputstream to read data from client
		String uname = ip.readUTF(); // Reading client username
		String dirname = ip.readUTF(); // Reading dirpath to be created
		String path = "E:\\Lab1Folders\\"+ uname + "\\" + dirname; // Setting path for directory to delete
		File dir = new File(path); // Creating file object to delete directory
		if(dir.exists()) { // checking is directory already exists
			org.apache.commons.io.FileUtils.deleteDirectory(dir); // Method which deletes the subdirectory in given directory
			FileWriter writer = new FileWriter("E:\\Lab1Folders\\logs.txt",true);
			writer.append("DeleteDir : " + path);
			writer.append(System.getProperty( "line.separator" ));
			writer.flush();
			writer.close();

		}
		else
			result = 1; // // Setting error code 2 suggesting directory does not exists
		return result;
		
	}

	
	//Method to rename or move the directory
	public int renameDir(Socket clientSock) throws IOException {
		int result = 0; // Error code initialization
		InputStream in = clientSock.getInputStream(); // Getting inputstream from client
		DataInputStream ip = new DataInputStream(in); // Creating datatinputstream to read data from client
		String uname = ip.readUTF(); // Reading client username
		String dirpath = ip.readUTF(); // Reading source directory(or origin) path to be rename or moved
		String newDirPath = ip.readUTF(); // Reading destination directory(or new name) path to be moved or rename
		String path = "E:\\Lab1Folders\\"+ uname + "\\" + dirpath; // Setting source path(or origin name) for directory to move or rename
		String newPath = "E:\\Lab1Folders\\"+ uname + "\\" + newDirPath; // Setting destination path(or new name) for directory to be moved or renamed
		// https://www.codejava.net/java-se/file-io/how-to-rename-move-file-or-directory-in-java
		File dir = new File(path); // Creating file object with origin path or name  to rename or move directory
		File dir1 = new File(newPath); // Creating file object with new path or name  to rename or move directory
		if(dir.exists()) { // checking is directory already exists
			if(dir.renameTo(dir1)) { // Renaming directory to new name or moving it to new destination
				result = 0; // Setting error code to 0 suggesting that renaming or moving was successful
				FileWriter writer = new FileWriter("E:\\Lab1Folders\\logs.txt",true);
				writer.append("RenameMoveDir : " + "from " + path + " to " + newPath);
				writer.append(System.getProperty( "line.separator" ));
				writer.flush();
				writer.close();
			}
			else
				result = 1; // Setting error code to 1 suggesting directory does not exist 
		}
		else
			result = 2; // Setting error code to 1 suggesting error occured during renaming or moving operation
		return result; // Returning error code
	}
	
	// List Contents 
	// https://stackoverflow.com/questions/5694385/getting-the-filenames-of-all-files-in-a-folder
	// https://stackoverflow.com/questions/36849197/send-message-to-specific-clients-using-java/49846551
	public int listFiles(Socket clientSock) throws IOException {
		int result = 0; // Error code initialization
		InputStream in = clientSock.getInputStream(); // Getting inputstream from client
		DataInputStream ip = new DataInputStream(in); // Creating datatinputstream to read data from client
		OutputStream os = clientSock.getOutputStream(); // Creating outputstream to send filelist data to client
		DataOutputStream op = new DataOutputStream(os); // Creating datatoutputstream to send filelist data to client
		String uname = ip.readUTF(); // Reading client username
		String dirpath = ip.readUTF(); // Reading directory path for listing content
		String path = "";
		if(dirpath.equals(null)) { 
			path = "E:\\Lab1Folders\\"+ uname;
		}
		else {
			path = "E:\\Lab1Folders\\"+ uname + "\\" + dirpath; // Setting path for directory for which content to be listed 
		}
		File dir = new File(path); // Creating file object for listing purpose
		
		if(dir.exists()) { // checking is directory exists
			File[] listofFiles = dir.listFiles(); // Getting list of files or directories for the requested directory
			op.writeUTF("Files in " +path);
			for(int j = 0;j<listofFiles.length;j++) { // loop which sends client list one by one
				op.writeUTF(listofFiles[j].getName()); // sending client filenames
			}
		}
		else
			result = 1; // Setting error code to 1 suggesting that directory does not exist
		return result; // Returning error code
	}
	
	//Get all home directories on server
	public void getHome(Socket clientSock) throws IOException {
		OutputStream os = clientSock.getOutputStream(); 
		DataOutputStream op = new DataOutputStream(os); 
		String path = "E:\\Lab1Folders\\";
		File dir = new File(path);
		if(dir.exists()) { // checking is directory exists
			File[] listofFiles = dir.listFiles(); // Getting list home directories for the requested directory
			for(int j = 0;j<listofFiles.length;j++) { // loop which sends client list one by one
				op.writeUTF(listofFiles[j].getName()); // sending client filenames
			}
		}

	}
	
	// Recursively deleting the files
	// https://stackoverflow.com/questions/20281835/how-to-delete-a-folder-with-files-using-java
	public void delDir(File file) {
		File[] files = file.listFiles(); // Getting list of files to be deleted
		if(files != null) { // checking if there are any files to be deleted
			for(File f : files) { // Iterating over the list of files
				delDir(f); // Recursively calling the function
			}
		}
		file.delete(); // Deleting the file
		
	}
	
	//Method to undo operation
	public void undoOps() {
		String undoS = (String)loglist.getSelectedValue();
		int index = loglist.getSelectedIndex();
		logModel.remove(index);
		Path p = Paths.get("E:\\Lab1Folders\\logs.txt");
		try {
			Files.newBufferedWriter(p, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		logs.remove(undoS);
		for(int i = 0; i< logs.size();i++) {
			try {
				FileWriter fw = new FileWriter("E:\\Lab1Folders\\logs.txt",true);
				fw.append(logs.get(i));
				fw.append(System.getProperty( "line.separator" ));
				fw.flush();
				fw.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		String cmd = undoS.substring(0,undoS.indexOf(" "));
		if(cmd.equals("CreateDir")) {
			String path = undoS.substring(undoS.lastIndexOf(" ") + 1,undoS.length());
			File dir = new File(path);
			try {
				org.apache.commons.io.FileUtils.deleteDirectory(dir);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(cmd.equals("DeleteDir")) {
			String path = undoS.substring(undoS.lastIndexOf(" ") + 1,undoS.length());
			File dir = new File(path);
			dir.mkdir();
		}
		else if(cmd.equals("RenameMoveDir")) {
			String[] temp = undoS.split(" ");
			File src = new File(temp[5]);
			File dest = new File(temp[3]);
			src.renameTo(dest);
		}
	}
	

	//  Main method declares the port number on which the connection will be made and calls a thread using .start() method and also calls default constructor for making the GUI
	public static void main(String[] args) {
		FileServer fs = new FileServer(1998); // Initializing the object of fileserver
		fs.start(); // Creating GUI for the server
		new FileServer(); // Staring the server
		
	}

}
