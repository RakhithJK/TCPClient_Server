import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.Files;
import java.util.*;

import javax.lang.model.util.ElementScanner6;

//import jdk.internal.jline.internal.InputStreamReader;

public class FTPClient {
	Socket requestSocket;           //socket connect to the server
	ObjectOutputStream out;         //stream write to the socket
	ObjectInputStream in;			//stream read from the socket
	static String serverIP="localhost";
	static int serverPort=8000;           
	String user;                //message send to the server
	String password;                //capitalized message read from the server
	String RESPONSE;
	String command;
	
	public FTPClient(String ip, int port) {
		this.serverIP=ip;
		this.serverPort=port;
	}
	public static void main(String args[])
	{
		try{
			while(true){
				System.out.println("Client started");
				System.out.println("Enter command:");
				BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
				String[] commandInput = input.readLine().split(" ");
				if(commandInput.length!=3||!(commandInput[0].equals("ftpclient"))){
					System.out.println("Invalid command signature. Enter correct command.");
					continue;
				}
				else if(commandInput[0].equals("ftpclient")){
					if(commandInput[1].equals(serverIP)&&Integer.parseInt(commandInput[2])==serverPort){
						FTPClient client = new FTPClient(commandInput[1],Integer.parseInt(commandInput[2]));
						client.run();
						break;		
					}
				}
				System.out.println("Server details incorrect or command invalid. Please enter correct value");
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		
	}

	
	/*public void ftpClient(){

	}*/

	void run()
	{
		try{
			//create a socket to connect to the server
			requestSocket = new Socket(serverIP, serverPort);
			System.out.println("Connected to localhost on port 8000");
			//initialize inputStream and outputStream
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			
			//get Input from standard input
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			while(true)
			{
				//Login loop
				System.out.print("Hello, please input username: ");
				//read a sentence from the standard input
				user = bufferedReader.readLine();
				System.out.print("Please input password: ");
				//read a sentence from the standard input
				password = bufferedReader.readLine();
				//Send the sentence to the server
				login(user,password);
				//Receive the upperCase sentence from the server
				RESPONSE = (String)in.readObject();
				if(RESPONSE.equals("true")) {
					System.out.println("Login Succesful. Connected to server: "+serverIP+": "+serverPort);
					break;
				}
				else{
					System.out.println("Incorrect credentials");
					continue;
				}
				//show the message to the user
				//System.out.println("Receive message: " + MESSAGE);
			}
			while(true){
				System.out.println("Enter Command");
				command = bufferedReader.readLine();
				if(command.toLowerCase().equals("dir")){
					if(listFiles()){
						System.out.println((String)in.readObject());
						continue;
					}
				}
				if(command.toLowerCase().equals("exit")){
					break;
				}
				String[] commandArr = command.split(" ");
				int n = commandArr.length;

				if(n==2){
					if(commandArr[0].toLowerCase().equals("get")){
						String filename = commandArr[1];
						if(get(filename)){
							System.out.println("File "+filename+"' downloaded succesfully.");
						}
						else System.out.println(filename+" is unavailable on the Server.");
						continue;
					}
					if(commandArr[0].toLowerCase().equals("upload")){
						String filename = commandArr[1];
						if(upload(filename)){
							System.out.println("File "+filename+"' uploaded succesfully.");
						}
						else System.out.println("Client doesn't contain "+filename);
						continue;
					}
				}
				System.out.println("Invalid command entered.\nSee below list for valid commands-\n1)dir\t-\tList all files present on the server.\n2)get <filename>\t-\tDownload a file from the server.\n3)upload <filename>\t-\tTo upload a file to the server.\n4)exit\t-\tTo close the client.");
				
			}
			
		}
		catch (ConnectException e) {
    			System.err.println("Connection refused. You need to initiate a server first.");
		} 
		catch ( ClassNotFoundException e ) {
            		System.err.println("Class not found");
        	} 
		catch(UnknownHostException unknownHost){
			System.err.println("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}
	boolean listFiles(){
		try{
			out.writeObject("dir");
			out.flush();
			return true;
		}
		catch(IOException ioException){
			ioException.printStackTrace();
			return false;
		}
	}
	boolean upload(String filename){
		try{
			File f = new File(filename);
			if(!f.exists()){
				System.out.println("File does not exist");
				return false;
			}
			else {
				out.writeObject("upload,"+filename);
				out.flush();
				byte[] fileContent = Files.readAllBytes(f.toPath());
				out.writeObject(fileContent);
				out.flush();
				System.out.println("File "+ filename+" sent to server.");
				RESPONSE =(String)in.readObject();
				if(RESPONSE.equals("Success")){
					System.out.println("File received by server.");
				}
				else{
					System.out.println("Server could not process file. Please send again");
					return false;
				}
				return true;
			}
		}
		catch(Exception e){
			
			e.printStackTrace();
			return false;
		}
	}
	boolean get(String filename){
		try{
			out.writeObject("get");
			out.flush();
			out.writeObject(filename);
			out.flush();
			byte[] downloadedFile = (byte[])in.readObject();
			if(downloadedFile.length!=0){
				File f = new File(filename);
				Files.write(f.toPath(), downloadedFile);
				return true;
			}
			else{
				return false;
			}
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	void login(String username, String password){
		try{
			//stream write the message
			out.writeObject(username+" "+password);
			out.flush();
			System.out.println("Attempting to login for client user: " + username);

		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
}
