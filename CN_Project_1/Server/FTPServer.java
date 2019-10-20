import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.Files;
import java.util.*;
public class FTPServer{
    private static final int serverPortNo = 8000;   //The server will be listening on this port number
    private static final String user = "user";
    private static final String password = "password";
    public static void main(String[] args) throws Exception{
        System.out.println("FTP Server running on port: "+serverPortNo);
        System.out.println("Ready to accept requests");
        ServerSocket listener = new ServerSocket(serverPortNo);
        int clientNum = 1;
        	try {
            		while(true) {
                        new ClientHandler(listener.accept(),clientNum).start();
				            System.out.println("Client "  + clientNum + " is connected!");
                            clientNum++;
            			}
            } 
            catch (Exception e){
                //System.out.println(e.stackTrace());
                e.printStackTrace();
            }
            finally {
            		listener.close();
            }
        
    }
    private static class ClientHandler extends Thread {
        private String message;    //message received from the client
    private Socket connection;
        private ObjectInputStream in;	//stream read from the socket
        private ObjectOutputStream out;    //stream write to the socket
    private int no;		//The index number of the client

        public ClientHandler(Socket connection, int no) {
                this.connection = connection;
            this.no = no;
        }

    public void run() {
     try{
        //initialize Input and Output streams
        out = new ObjectOutputStream(connection.getOutputStream());
        out.flush();
        in = new ObjectInputStream(connection.getInputStream());
        try{
            while(true)
            {
                //receive the credentials sent from the client
                message = (String)in.readObject();
                String[] credentials = message.split(" ");
                if(credentials.length!=2){
                    out.writeObject("false");
                    out.flush();
                    continue;
                }
                else {
                    if(credentials[0].equals(user)&&credentials[1].equals(password)){
                        System.out.println("Authenticated Client"+no);
                        out.writeObject("true");
                        out.flush();
                        break;
                    }
                    else{
                        System.out.println("Authentication failed for Client"+no);
                        out.writeObject("false");
                        out.flush();
                        continue;
                    }
                }   
            }
            while(true){
                message=(String)in.readObject();
                if(message.equals("dir")){
                    //List all available files to the client
                    System.out.println("List of Files available sent to Client"+no);
                    out.writeObject(listFiles());
                    out.flush();
                }
                else if(message.equals("get")){
                    //Send file to client
                    byte [] result = sendFile((String)in.readObject());
                    System.out.println("File sent to Client"+no);
                    out.writeObject(result);
                    out.flush();
                }
                else if(message.startsWith("upload")){
                    //Receive file from client
                    String filename = message.split(",")[1];
                    System.out.println("File "+filename+" received from Client"+no);
                    out.writeObject((String)receiveFile(filename,(byte[])in.readObject()));
                    out.flush();
                }
            }
        }
        catch(ClassNotFoundException classnot){
                System.err.println("Data received in unknown format");
            }
    }
    catch(IOException ioException){
        System.out.println("Disconnect with Client " + no);
    }
    finally{
        //Close connections
        try{
            in.close();
            out.close();
            connection.close();
        }
        catch(IOException ioException){
            System.out.println("Disconnect with Client " + no);
        }
    }
}
public String listFiles(){
    File pwd = new File(System.getProperty("user.dir"));
    String files = "";
    for(File f: pwd.listFiles()){
        files=files+(f.getName()+"\n");
    }
    return files;
}
public byte[] sendFile(String filename){
    try{
        File f = new File(filename);
        if(f.exists()){
            byte[] result = Files.readAllBytes(f.toPath());
            return result;
        }
        else {
            return new byte[0];
        }
    }
    catch(Exception ex){
        ex.printStackTrace();
        return new byte[0];
    }
     
}
public String receiveFile(String filename,byte[] fileContent){
    try{
        File f = new File(filename);
        if(fileContent.length==0){
            return "Empty";
        }
        Files.write(f.toPath(), fileContent);
        return "Success";
    }
    catch(Exception e){
        e.printStackTrace();
        return "Failure";
    }
    
}

}
}