package chatapp;



	import java.io.*;
	import java.net.Socket;
	import java.util.Scanner;

	/**
	 * ChatClient: a console client that connects to ChatServer.
	 * Usage: run ChatClient.main, enter nickname when prompted.
	 */
	public class ChatClient {
	    public static void main(String[] args) {
	        String host = "localhost";
	        int port = 6002;
	        try (Socket socket = new Socket(host, port)) {
	            System.out.println("Connected to server " + host + ":" + port);
	            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
	            Scanner sc = new Scanner(System.in);

	            // Wait for server prompt
	            String serverLine = in.readLine();
	            if ("ENTER_NICK".equals(serverLine)) {
	                System.out.print("Enter your nickname: ");
	                String nick = sc.nextLine().trim();
	                out.println("NICK:" + nick);
	                // server may accept or reject; if reject, server will close
	            } else {
	                System.out.println("Unexpected server response: " + serverLine);
	                return;
	            }

	            // Start a thread to read messages from server
	            Thread reader = new Thread(() -> {
	                try {
	                    String s;
	                    while ((s = in.readLine()) != null) {
	                        System.out.println(s);
	                    }
	                } catch (IOException e) {
	                    System.out.println("Connection closed.");
	                }
	            });
	            reader.setDaemon(true);
	            reader.start();

	            // Main loop: read user input and send to server
	            System.out.println("You can now type messages. Use @nickname to PM. Type /quit to exit.");
	            while (true) {
	                String msg = sc.nextLine();
	                if (msg == null) break;
	                out.println(msg);
	                if (msg.equalsIgnoreCase("/quit")) break;
	            }
	            System.out.println("Disconnected.");
	        } catch (IOException e) {
	            System.err.println("Unable to connect to server: " + e.getMessage());
	        }
	    }
	}
