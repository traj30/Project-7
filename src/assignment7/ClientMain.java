/* ClientMain.java
 * EE422C Project 4b submission by
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
import javafx.geometry.*;
import javafx.scene.*;
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
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientMain extends Application { 
	
	private AtomicBoolean initial = new AtomicBoolean(true);
	private String user = "";
	private String sent = "";
	private TextArea incoming;
	private TextField outgoing;
	private BufferedReader reader;
	private PrintWriter writer;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	private Scene initView() {
		incoming = new TextArea("Please enter a username below \n"); 
		outgoing = new TextField(); 
		outgoing.setAlignment(Pos.BASELINE_LEFT); 
		
		BorderPane outgoingBPane = new BorderPane(); 
		outgoingBPane.setPadding(new Insets(10, 6, 6, 6)); 
		outgoingBPane.setLeft(new Label("Message: ")); 
		outgoingBPane.setCenter(outgoing); 
		
		BorderPane mainBPane = new BorderPane(); 
		mainBPane.setCenter(incoming); 
		mainBPane.setBottom(outgoingBPane); 
		
		Scene scene = new Scene(mainBPane, 500, 200); 
		incoming.resize(scene.getWidth(), scene.getHeight());
		return scene;
	}

	public void setUpNetworking() throws Exception {
		Socket sock = new Socket("127.0.0.1", 4242);
		InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
		reader = new BufferedReader(streamReader);
		writer = new PrintWriter(sock.getOutputStream());
		System.out.println("networking established");
	}
	
	public void actionPerformed(){
		outgoing.setOnAction(e -> { 
			if (initial.getAndSet(false)) {
				user = outgoing.getText();
				sent = user + " joined.";
			} 
			else {
				sent = user + ":  "+ outgoing.getText();
			}
			writer.println(sent);
			writer.flush();
			outgoing.setText("");
			outgoing.requestFocus();
		}); 
	}
	
	@Override  
	public void start(Stage stage) throws Exception { 
		
		setUpNetworking();
		Scene scene = initView(); 
		
		stage.setTitle("Chat Room");  
		stage.setScene(scene); 
		stage.show();
		
		Thread reading = new Thread(new IncomingReader());
		reading.start();
		
		actionPerformed();					
	}
		
	class IncomingReader implements Runnable {
		public void run() {
			String msg;
			try {
				while ((msg = reader.readLine()) != null) {
					if (msg.charAt(msg.indexOf(':') + 3) == '@') {
						synchronized(this) {
							if (msg.substring(msg.indexOf(':') + 4, msg.indexOf(':') + 4 + user.length()).equals(user)) {
								String msgUser = msg.substring(0, msg.indexOf(':'));
								String sentMsg = msg.substring(msg.indexOf(':') + 4 + msgUser.length(), msg.length());
								incoming.appendText(msgUser + " sent you: " +  sentMsg + "\n");
							} else if (msg.equals(sent)) {
								incoming.appendText(msg + "\n");
							}
						}
					} else {
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
