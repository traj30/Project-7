/* ClientMain.java
 * EE422C Project 7 submission by
 * Replace <...> with your actual data.
 * Jake Klovenski
 * jdk2595
 * 16445
 * Tiraj Parikh
 * trp589
 * 16460
 * Slip days used: <0>
 * Fall 2016
 */

package assignment7;

import javafx.application.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextAreaBuilder;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.awt.event.ActionListener;
import java.io.*; 
import java.net.*;
import java.util.ArrayList;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientMain extends Application { 
	//initialize variables
	private AtomicBoolean initial = new AtomicBoolean(true);
	private String user = "";
	private String sent = "";
	private String ip = "";
	private Label ipLabel;
	private Button sendIP;
	private TextField enterIP;
	private TextArea incoming;
	private TextField outgoing;
	private BufferedReader reader;
	private PrintWriter writer;
	private ArrayList<String> groups = new ArrayList<String>();
	//run program
	public static void main(String[] args) {
		launch(args);
	}
   /**
	* initialize GUI for chat room
	* @return scene of chatroom
	*/
	private Scene initView() {
		//create layouts
		incoming = new TextArea("Please enter a username below \n"); 
		outgoing = new TextField(); 
		outgoing.setAlignment(Pos.BASELINE_LEFT); 
		//create borderpane for outgoing text
		BorderPane outgoingBPane = new BorderPane(); 
		outgoingBPane.setPadding(new Insets(10, 6, 6, 6)); 
		outgoingBPane.setLeft(new Label("Message: ")); 
		outgoingBPane.setCenter(outgoing); 
		//create main border pane
		BorderPane mainBPane = new BorderPane(); 
		mainBPane.setCenter(incoming); 
		mainBPane.setBottom(outgoingBPane); 
		//create scene
		Scene scene = new Scene(mainBPane, 500, 200); 
		incoming.resize(scene.getWidth(), scene.getHeight());
		return scene;
	}
	/**
	 * set up socket, input stream reader, buffered reader, and print writer
	 * print statement to indicate network connected
	 * @throws Exception
	 */
	public void setUpNetworking() throws Exception {
		Socket sock = new Socket(ip, 4242);			//"192.168.2.7"
		InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
		reader = new BufferedReader(streamReader);
		writer = new PrintWriter(sock.getOutputStream());
		System.out.println("networking established");
	}
	/**
	 * when enter is pressed on textfield, output text to text area, then reset writer and textfield
	 */
	public void actionPerformed(){
		outgoing.setOnAction(e -> { 
			if (initial.getAndSet(false)) {
				user = outgoing.getText();
				sent = user + " joined.";
			} 
			else {
				sent = user + ":"+ outgoing.getText();
			}
			writer.println(sent);
			writer.flush();
			outgoing.setText("");
			outgoing.requestFocus();
		}); 
	}
	/**
	 * Display stage to prompt user to enter IP address
	 */
	class FirstStage {
				
		FirstStage(Stage stage)
		{
			Stage substage = new Stage();
			substage.setTitle("Prompt"); 
			
			enterIP = new TextField(); 
			BorderPane IPBPane = new BorderPane(); 
			IPBPane.setPadding(new Insets(6, 6, 6, 6));  
			enterIP.setAlignment(Pos.TOP_LEFT);
			IPBPane.setCenter(enterIP); 
			ipLabel = new Label("Enter IP Address: ");
			IPBPane.setLeft(ipLabel);
			Scene scene = new Scene(IPBPane, 350, 150); 
			substage.setScene(scene); 
			substage.show();
			while(enterIP.isPressed()){}
			//set ip to IP address once enter is pressed on text field
		    enterIP.setOnAction(new EventHandler<ActionEvent>() {
		        @Override
		        public void handle(ActionEvent t) {
		        	ip = enterIP.getText();
					try {
						mainStage(stage);
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
		        	substage.close();
		        }
		    });
			
		} 
			
	}
	
	/**
	 * Main runner for chat room GUI
	 */
	@Override  
	public void start(Stage stage) throws Exception { 
		
		FirstStage ipStage = new FirstStage(stage);

	}

	public void mainStage(Stage stage) throws Exception
	{
		setUpNetworking();
		Scene scene = initView();

		stage.setTitle("Chat Room");
		stage.setScene(scene);
		stage.show();

		Thread reading = new Thread(new IncomingReader());
		reading.start();

		actionPerformed();
	}
	/**
	 * threaded input analyzer of text field
	 * if @user is type, will send private message to user
	 */
	class IncomingReader implements Runnable {
		public void run() {
			
			String msg;
			try {
				while ((msg = reader.readLine()) != null) {
					char metaChar = msg.charAt(msg.indexOf(':') + 1);
					System.out.println(metaChar);
					if (metaChar == '@') {
						synchronized(this) {
							try {
								String toUser = msg.substring(msg.indexOf(':') + 2, msg.indexOf(' '));
								if (toUser.equals(user)) {
									String msgUser = msg.substring(0, msg.indexOf(':'));
									String sentMsg = msg.substring(msg.indexOf(' '), msg.length());
									incoming.appendText(msgUser + " sent you: " + sentMsg + "\n");
								} else if (msg.equals(sent)) {
									incoming.appendText(msg + "\n");
								}
							}
							catch(ArrayIndexOutOfBoundsException e1)
							{
								System.out.println("Please send a message");
							}
						}
					}
					else if(metaChar == '#'){
						synchronized (this) {
							try {
								String group = msg.substring(msg.indexOf(':') + 2, msg.indexOf(' '));
								if (groups.contains(group)) {
									String msgUser = msg.substring(0, msg.indexOf(':'));
									String sentMsg = msg.substring(msg.indexOf(' '), msg.length());
									incoming.appendText((msgUser + " said to " + group + ": " + sentMsg + "\n"));
								}
							}
							catch(ArrayIndexOutOfBoundsException e1)
							{
								System.out.println("Please send a message.");
							}
						}
					}
					else if(metaChar == '$'){
						synchronized (this) {
							String group = msg.substring(msg.indexOf(':') + 2);
							System.out.println(group);
							if(!groups.contains(group) && msg.substring(0,msg.indexOf(':')).equals(user)){
								System.out.println("group");
								groups.add(group);
							}
						}
					}
					else {
						synchronized(this){
							incoming.appendText(msg + "\n");
						}
					}
					sent = "";
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}
