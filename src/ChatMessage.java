import java.io.*;

/*
 * This class defines the different type of
 * messages that will be exchanged between the
 * clients and the server
 * 
 * When talking from a Java Client to a Java
 * Server a lot easier to pass java objects,
 * no need to count bytes or to wait for a line
 * feed at the end of the frame.
 * 
 * 
 * When talking between two Java applications,
 * if both have access to the same code,personally
 * prefer to send Java Object between the two
 * applications.Java will do the job of serializing
 * and deserializing the the Java objects for you.
 * To do that you have to create an ObjectInputStream
 * and an ObjectOutputStream from the Socket 
 * InputStream and the Socket OutputStream
 * 
 * The objects sent of the sockets have to implements
 * Serializable.In this application,all the messages
 * sent form the Server to the Client are String objects.
 * All the messages sent from Client to the Server
 * (but the first one which is a String) are ChatMessage.
 * 
 * 
 * 
 * 
 */

public class ChatMessage implements Serializable {
	
	protected static final long serialVersionUID = 1112122200L;
	
	//WHOSIN to receive the list of the users connected
	//MESSAGE an ordinary message
	//LOGOUT to disconnected from the server
	
	static final int WHOISON = 0,MESSAGE = 1,LOGOUT = 2;
	
	private int type;
	
	private String message;
	
	ChatMessage(int type,String message){
		this.type = type;
		this.message = message;
	}
	
	
	int getType(){
		return type;
	}
	
	String getMessage(){
		return message;
	}
}
