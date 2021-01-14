

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.crypto.Cipher;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author Mengchuan Lin
 */
public class ChatClient extends JFrame implements Runnable {
    private JTextArea jta;
    private JTextField jtfName, jtfMsg, jtfKey;
    private JPanel jpl1, jpl2, jpl3;

    private Socket socket;
    private DataOutputStream dout;
    private DataInputStream din;
    private String name, message, key;
    private final initVactor = "Iamhereandfucked"

    public ChatClient(String hostName, int portNum) {
    	 String initVector = "encryptionIntVec";

        initComponents();
        jtfMsg.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!jtfName.getText().isEmpty() &&
                    !jtfMsg.getText().isEmpty()  &&
                    !jtfKey.getText().isEmpty()) {
                    name = jtfName.getText().trim();
                    message = jtfMsg.getText().trim();
                    key = jtfKey.getText().trim();
                    try {
            	        IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            	        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            	 
            	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            	        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            	 
            	        byte[] encrypted = cipher.doFinal(message.getBytes());
            	        message Base64.encodeBase64String(encrypted);
            	    } catch (Exception ex) {
            	        ex.printStackTrace();
            	    }
                    processMessage(name);
             
                    processMessage(message);
                }
                else if (jtfName.getText().isEmpty())
                    JOptionPane.showMessageDialog(null, "Enter name.",
                                "No name detected",
                                JOptionPane.ERROR_MESSAGE);
                else if (jtfMsg.getText().isEmpty())
                    JOptionPane.showMessageDialog(null, "Enter chat message.",
                                "No message detected",
                                JOptionPane.ERROR_MESSAGE);
                else 
                	JOptionPane.showMessageDialog(null, "Enter chat key.",
                            "No key found",
                            JOptionPane.ERROR_MESSAGE);
            }
        });
        connectToServer(hostName, portNum);
    }

    private void initComponents() {
        //populate label panel
        jpl1 = new JPanel();
        jpl1.setLayout(new GridLayout(3, 1));
        jpl1.add(new JLabel("Name"));
        jpl1.add(new JLabel("Enter text"));
        jpl1.add(new JLabel("Room key"));

        //populate textfield panel
        jpl2 = new JPanel();
        jpl2.setLayout(new GridLayout(3, 1));
        jtfName = new JTextField();
        jtfName.setHorizontalAlignment(JTextField.RIGHT);
        jpl2.add(jtfName);
        jtfMsg = new JTextField();
        jtfMsg.setHorizontalAlignment(JTextField.RIGHT);
        jpl2.add(jtfMsg);
        jtfKey = new JTextField();
        jtfKey.setHorizontalAlignment(JTextField.RIGHT);
        jpl2.add(jtfKey);

        //put label and button panel on jpl3
        jpl3 = new JPanel();
        jpl3.setLayout(new BorderLayout());
        jpl3.add(jpl1, BorderLayout.WEST);
        jpl3.add(jpl2, BorderLayout.CENTER);

        //align panels on frame
        setLayout(new BorderLayout());
        add(jpl3, BorderLayout.NORTH);
        jta = new JTextArea();
        jta.setEditable(false);
        add(new JScrollPane(jta), BorderLayout.CENTER);

        setTitle("Chat Client");
        Dimension d = new Dimension(400, 300);
        setSize(d);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocation(700, 300);
        setVisible(true);
    }

    private void processMessage(String msg) {
        try {
            dout.writeUTF(msg); //send text to server
            jtfMsg.setText(""); //clear out text input field
        }
        catch (IOException ioe) {
            System.err.println(ioe);
        }
    }

    @Override
    public void run() {
        try {
            while(true) {
                String rName = din.readUTF();
                String rMessage = din.readUTF();
                
        	    try {
        	        IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
        	        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
        	 
        	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        	        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
        	        byte[] original = cipher.doFinal( Base64.encodeBase64(encrypted));
        	 
        	        return new String(original);
        	    } catch (Exception ex) {
        	        ex.printStackTrace();
        	    }
        	    
        	    
                jta.append(rName + ": " + rMessage + '\n');
            }
        }
        catch(IOException ioe) {
            System.err.println(ioe);
        }
    }

    private void connectToServer(String hostName, int portNum) {
        try {
            //initiate connection to server
            socket = new Socket(hostName, portNum);

            jta.append("Connected to " + socket + '\n');

            din = new DataInputStream(socket.getInputStream());
            dout = new DataOutputStream(socket.getOutputStream());

            //start background thread to receive messages
            new Thread(this).start();
        }
        catch(IOException ioe) {
            System.err.println(ioe);
        }
    }

    public static void main(String[] args) {
        String hostName =  "localhost";
        int portNum = 9999;
        new ChatClient(hostName, portNum);
    }
}
