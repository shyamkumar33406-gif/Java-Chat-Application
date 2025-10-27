package chatapp;


	import java.io.IOException;
	import java.net.ServerSocket;
	import java.net.Socket;
	import java.util.concurrent.ConcurrentHashMap;

	/**
	 * ChatServer: accepts client connections and manages active clients.
	 */
	public class ChatServer {
	    // nickname -> handler
	    static ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();

	    public static void main(String[] args) {
	        int port = 6002; // you can change port
	        System.out.println("Starting ChatServer on port " + port + "...");
	        try (ServerSocket serverSocket = new ServerSocket(port)) {
	            while (true) {
	                Socket clientSocket = serverSocket.accept();
	                // create and start handler thread
	                ClientHandler handler = new ClientHandler(clientSocket);
	                Thread t = new Thread(handler);
	                t.start();
	            }
	        } catch (IOException e) {
	            System.err.println("Server exception: " + e.getMessage());
	            e.printStackTrace();
	        }
	    }
	}
