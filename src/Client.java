import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_ADDRESS = "localhost"; // Change to server IP if needed
    private static final int PORT = 12345;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private JTextArea textArea;
    private JTextField textField;
    private String username;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Client().createGUI();
        });
    }

    public Client() {
        try {
            socket = new Socket(SERVER_ADDRESS, PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createGUI() {
        JFrame frame = new JFrame("TCP Chat Client");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Prompt for username
        username = JOptionPane.showInputDialog("Enter your username:");

        // Chat area
        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Text field for sending messages
        textField = new JTextField();
        frame.add(textField, BorderLayout.SOUTH);

        // Action listener for sending messages
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = textField.getText();
                if (!message.trim().isEmpty()) {
                    out.println(username + ": " + message); // Send message with username
                    textField.setText(""); // Clear the text field
                }
            }
        });

        frame.setVisible(true);

        // Start thread to read incoming messages
        new Thread(new IncomingReader()).start();
    }

    private class IncomingReader implements Runnable {
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    textArea.append(message + "\n"); // Display the message in the chat area
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
