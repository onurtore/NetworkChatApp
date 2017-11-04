import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class Server {
	//A unique ID for each connection
	private static int uniqueId;
	
	//An ArrayList to keep the list of the Client
	private ArrayList<ClientThread> al;
	
	private SimpleDateFormat sdf;
	
	//the port number to listen for connection
	private int port;

	// if I am in a GUI
	private ServerGUI sg;

	Timer timer;
	
	//I need to sleep so this isnt gonna be private
	public int PMCanSee;
	
	//The boolen that will be turned of to stop the server
	private boolean keepGoing;

	
	public Server(int port){
		this(port,null);
	}

	public Server(int port,ServerGUI sg){
		
		this.sg = sg;
		this.port = port;
		
		//to display hh:mm:ss
		sdf = new SimpleDateFormat("HH:mm:ss");
		
		al = new ArrayList<ClientThread>();
	}
	
	public void start(){
		
		keepGoing = true;
		timer = new Timer(true);
		timer.schedule(new TimeMessages(),0,30000);
		try{
			ServerSocket serverSocket = new ServerSocket(port);
			
			while(keepGoing){
				//Her saniye print yapmýyor.
				display("Server waiting for Clients on port " + port + ".");
				
				Socket socket = serverSocket.accept();
				
				if(!keepGoing)
					break;
				
				ClientThread t = new ClientThread(socket);
				al.add(t);
				t.start();
			}
			
			//Stop it 
			try{
				serverSocket.close();
				for(int i = 0; i < al.size(); i++){
					ClientThread tc = al.get(i);
					try{
						tc.sInput.close();
						tc.sOutput.close();
						tc.socket.close();
					}
					catch(IOException ioE){
						System.out.println("Thread " + i + " can not closed " + ioE  );
					}
				}
			}
			catch(Exception e){
				display("Exception closing the server and clients: " + e );
			}
		}
		catch(IOException e){
			String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}
	
	protected void stop(){
		keepGoing = false;
		
		//Connect to myself as Client to exit statement //Onur Berk Töre : I don't know why so i dont include it here
	}
	
	
	private void display(String msg){
		String time = sdf.format(new Date()) + " " + msg;
		if(sg == null)
			System.out.println(time);
		else
			sg.appendEvent(time + "\n");
	}
//Harry Potter ve ölüm yadigarlarý bölüm 2 
	
	private synchronized void PM(String username, String message){
		
		String _sender;
		String _receiver;
		
		
		//Hoca dediki usernameler space içermemesini kabul edebilirmiþiz.
		
		String[] tokens = message.split(" ");
		if(tokens.length!=2){
			throw new IllegalArgumentException();
		}

		_receiver = tokens[0];
		
		
		
		
		
		
		message = tokens[1];
			
		
		String time = sdf.format(new Date());
	
		String sg_message = message;
		
		if(this.PMCanSee == 0 ){
			sg_message = "";
		}
		
		String messageLf = time + " User " + "\"" + username + "\" send a PM to user \"" + _receiver + "\": " + sg_message + "\n";
		
		//Hardcore Debug
		//System.out.println(messageLf);
	
		
		//For server log
		if(sg == null)
			System.out.print(messageLf);
		else
			sg.appendRoom(messageLf);     // append in the room window
					
		
		for ( int i = al.size(); --i >= 0 ; ){
			ClientThread ct = al.get(i);
			System.out.println("ct.username is  "  + ct.username);
			System.out.println("receiver username is : " + _receiver);

			if(ct.username.equals(_receiver)){
				messageLf = time + "!!! "  + username + " send a PM to you "  +  message + "\n";
				if(!ct.writeMsg(messageLf)){
					al.remove(i);
					display("Disconnected Client " + ct.username + " removed from list.");
				}
				break;
			}
		}
	}
		
	private synchronized void broadcast(String username, String message){
		
		String _sender;
		String time = sdf.format(new Date());
		String messageLf = time + " " + username + ": " + message + "\n";

		//For server log
		if(sg == null)
			System.out.print(messageLf);
		else
			sg.appendRoom(messageLf);     // append in the room window

		
		//Loop in reverse order in case we would have to remove a Client//Onur Berk Töre : Çokda fifi
		
		/*
		 * Hocanýn verdiði kodda, YOU yazýlmasý istenmiþ,gerekli yeri bulup düzeltmek
		 * kodu anladýðýnýn bir göstergesidir
		 */
		
		for ( int i = al.size(); --i >= 0 ; ){
			_sender = username;
			ClientThread ct = al.get(i);
			if(ct.username == username){
				_sender = "You";
			}
			messageLf = time + " " + _sender + ": " + message + "\n";
			
			
			
			
			if(!ct.writeMsg(messageLf)){
				al.remove(i);
				display("Disconnected Client " + ct.username + " removed from list.");
			}

		}
	}
	
	synchronized void remove(int id){
		for(int i = 0; i < al.size(); i++){
			ClientThread ct = al.get(i);
			if(ct.id == id){
				al.remove(i);
				return;
			}
		}
	}
	
	public static void main(String[] args){
		int portNumber = 1500;
		System.out.println("Port number is : " + portNumber);
		
		Server server = new Server(portNumber);
		server.start();
		
	}
	
	//One instance of this thread will run for each client
	
	//Read client output,act for them 
	class ClientThread extends Thread {
		//The socket where to listen/talk
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		
		int id;
		
		String username;
		ChatMessage cm;
		String date;
		
		ClientThread(Socket socket){
			
			id = ++uniqueId;
			this.socket = socket;
			
			System.out.println("Thread trying to create Object Input/Output Streams");
			try{
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				
				username = (String) sInput.readObject();
				display(username + " just connected");
			}
			
			catch(IOException e){
				display("Exception creating new Input/Output Streams: " + e );
				return;
			}
			catch (ClassNotFoundException e) {
			}
            date = new Date().toString() + "\n";
		}
		
		
		public void run(){
			boolean keepGoing = true;
			while(keepGoing){
				try{
					cm = (ChatMessage) sInput.readObject();
				}
				catch(IOException e ){
					display(username + " Exception reading Streams: " + e);
					break;
				}
				catch(ClassNotFoundException e2){
					break;
				}
				
				String message = cm.getMessage();
				
				
				//This part is addition of me, i dont want to mix it with the original variables
				String _sender;
				
				switch(cm.getType()){
					
				case ChatMessage.MESSAGE:
					
					broadcast(username, message);
					break;
				case ChatMessage.LOGOUT:
					display(username + " disconnected with a  LOGOUT message.");
					keepGoing = false;
					break;
				case ChatMessage.WHOISON:
					writeMsg("List  of the users connected at " + sdf.format(new Date()) + "\n");
					
					
					for(int i = 0; i < al.size(); i++){
						ClientThread ct = al.get(i);
						if(ct.username == username){
							_sender = "You";
						}
						else{
							_sender = ct.username;
						}
						
						writeMsg((i+1) + ") " +"\""+  _sender +"\"" + " since " + ct.date);
 					}
					break;
					
				//Added bu Onur Berk Töre
				//Personal message coming,we are respect this and not gonna put into sg kayýt
				case ChatMessage.PM:
					
					//My mysterious ways of debugging. 
					//System.out.println("PM received");
					PM(username,message);
					break;
				}
			}
			remove(id);
			close();
		}
		
		private void close(){
			try{
				if(sOutput != null) 
					sOutput.close();
			}
			catch(Exception e){
				
			}
			
			try{
				if(sInput != null)
					sInput.close();
			}
			catch(Exception e){
				
			}
			try{
				if(socket != null)
					socket.close();
			}
			catch(Exception e){
				
			}
			
		}
		
		private boolean writeMsg(String msg){
			if(! socket.isConnected()){
				close();
				return false;
			}
			
			try{
				sOutput.writeObject(msg);
			}
			catch(IOException e){
				display("Error sending to message to " + username);
				display(e.toString());
			}
			return true;
		}
		
	}
	//Onur Berk Töre
	class TimeMessages extends TimerTask {

		@Override
		public void run() {
			String msg = "Currently connected users:\n";
			for(int j= 0; j < al.size(); j++){
				msg += "-";
				ClientThread ct = al.get(j);
				msg += ct.username;
			}
			msg +="\n";
			for(int j= 0; j < al.size(); j++){
				ClientThread ct = al.get(j);
				ct.writeMsg(msg);
			}
		}
	}
}

