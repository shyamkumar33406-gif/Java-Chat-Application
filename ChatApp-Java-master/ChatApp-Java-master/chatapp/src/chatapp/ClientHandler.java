package chatapp;


	import java.io.*;
	import java.net.Socket;
	import java.util.Map;

	/**
	 * ClientHandler: handles a single client connection.
	 * Protocol:
	 *  - First message from client should be: NICK:<nickname>
	 *  - Messages starting with @nickname are private messages.
	 *  - Otherwise messages are broadcast to all clients.
	 */
	public class ClientHandler implements Runnable {
	    private Socket socket;
	    private String nickname;
	    private BufferedReader in;
	    private PrintWriter out;
	    private boolean running = true;

	    public ClientHandler(Socket socket) {
	        this.socket = socket;
	    }

	    public void send(String msg) {
	        out.println(msg); // auto flush
	    }

	    @Override
	    public void run() {
	        try {
	            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	            out = new PrintWriter(socket.getOutputStream(), true);

	            // 1) Read nickname
	            out.println("ENTER_NICK"); // prompt client to send nickname
	            String line = in.readLine();
	            if (line == null || !line.startsWith("NICK:")) {
	                socket.close();
	                return;
	            }
	            String proposed = line.substring(5).trim();
	            if (proposed.isEmpty() || ChatServer.clients.containsKey(proposed)) {
	                out.println("NICK_REJECTED");
	                socket.close();
	                return;
	            }

	            nickname = proposed;
	            ChatServer.clients.put(nickname, this);
	            broadcast("SERVER: " + nickname + " joined the chat.");

	            // 2) Read lines from client
	            while (running && (line = in.readLine()) != null) {
	                if (line.equalsIgnoreCase("/quit")) {
	                    break;
	                }
	                handleMessage(line);
	            }
	        } catch (IOException e) {
	            // connection reset or other IO issue
	        } finally {
	            // cleanup
	            if (nickname != null) {
	                ChatServer.clients.remove(nickname);
	                broadcast("SERVER: " + nickname + " left the chat.");
	            }
	            try {
	                socket.close();
	            } catch (IOException ignored) {}
	        }
	    }

	    private void handleMessage(String msg) {
	        // private message format: @nick message...
	        if (msg.startsWith("@")) {
	            int space = msg.indexOf(' ');
	            if (space > 1) {
	                String target = msg.substring(1, space);
	                String body = msg.substring(space + 1);
	                ClientHandler dest = ChatServer.clients.get(target);
	                if (dest != null) {
	                    dest.send("[PM from " + nickname + "]: " + body);
	                    send("[PM to " + target + "]: " + body);
	                } else {
	                    send("SERVER: User '" + target + "' not found.");
	                }
	            } else {
	                send("SERVER: Invalid private message format. Use: @nickname message");
	            }
	        } else {
	            broadcast(nickname + ": " + msg);
	        }
	    }

	    private void broadcast(String message) {
	        for (Map.Entry<String, ClientHandler> e : ChatServer.clients.entrySet()) {
	            e.getValue().send(message);
	        }
	    }
	}