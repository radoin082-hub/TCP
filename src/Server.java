import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static Set<PrintWriter> clientWriters = new HashSet<>();
    private static JTextArea textArea;
    private static JTextField messageField;
    private static BufferedReader consoleReader;
    private static PrintWriter consoleWriter;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Chat Server");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Setup the text area to show messages from clients and server
        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Setup the message input field for server to send messages to clients
        messageField = new JTextField();
        frame.add(messageField, BorderLayout.SOUTH);

        // ActionListener for sending message when Enter is pressed
        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessageToClients();  // Send message when Enter is pressed
            }
        });

        frame.setVisible(true);

        // Start the server and console reader
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            textArea.append("Server is running on port " + PORT + "...\n");

            // Initialize the console reader to handle input from server operator
            consoleReader = new BufferedReader(new InputStreamReader(System.in));
            consoleWriter = new PrintWriter(System.out, true);

            // Start a new thread to handle client connections
            while (true) {
                new ClientHandler(serverSocket.accept()).start();  // Accept incoming client connections
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Sends server message to all connected clients
    private static void sendMessageToClients() {
        String serverMessage = messageField.getText();  // Get message from the text field

        if (serverMessage != null && !serverMessage.trim().isEmpty()) {
            textArea.append("Server: " + serverMessage + "\n");  // Display in the server's text area

            // Broadcast the server's message to all connected clients
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println("Server: " + serverMessage);  // Send to all clients
                }
            }
            messageField.setText("");  // Clear input field after sending the message
        }
    }

    // Handles client communication and broadcasting messages
    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Add the client’s output stream to the list of writers
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                String message;
                while ((message = in.readLine()) != null) {
                    textArea.append("Client: " + message + "\n");

                    // Broadcast the client’s message to all connected clients
                    synchronized (clientWriters) {
                        for (PrintWriter writer : clientWriters) {
                            writer.println(message);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                synchronized (clientWriters) {
                    clientWriters.remove(out); // Remove the client from the list when disconnected
                }
            }
        }
    }
}
