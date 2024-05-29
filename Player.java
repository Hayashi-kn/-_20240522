import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class Player {
    private JFrame loginFrame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton signupButton;
    private Client client;

    public Player() {
        this.client = new Client(this::handleServerMessage); // サーバーとの通信を担当するClientインスタンスを作成
        initializeLoginUI();
    }

    private void initializeLoginUI() {
        loginFrame = new JFrame("オセロゲームオンライン");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(300, 120);
        loginFrame.setLayout(new GridLayout(3, 2));

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        loginButton = new JButton("Login");
        signupButton = new JButton("Signup");

        loginFrame.add(new JLabel("Username:"));
        loginFrame.add(usernameField);
        loginFrame.add(new JLabel("Password:"));
        loginFrame.add(passwordField);
        loginFrame.add(loginButton);
        loginFrame.add(signupButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

        signupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                signup();
            }
        });

        loginFrame.setLocationRelativeTo(null);
        loginFrame.setVisible(true);
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        client.sendMessage("login " + username + " " + password);
        System.out.println("Login message sent");
    }

    private void signup() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        client.sendMessage("signup " + username + " " + password);
        System.out.println("Signup message sent");
    }

    // サーバーからのメッセージに基づいて処理を行う
    private void handleServerMessage(String message) {
        System.out.println("Received message: " + message);
        if (message.startsWith("login success")) {
            System.out.println(message);
            openRecordView(usernameField.getText());
        } else if (message.startsWith("login failed")) {
            JOptionPane.showMessageDialog(loginFrame, "Login Failed", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (message.startsWith("signup success")) {
            System.out.println(message);
            openRecordView(usernameField.getText());
        } else if (message.startsWith("signup failed")) {
            JOptionPane.showMessageDialog(loginFrame, "Signup Failed", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openRecordView(String username) {
        loginFrame.setVisible(false); // ログイン画面を非表示にする
        loginFrame.dispose(); // リソースを解放する

        new Record(client, username);

        // recordView.show();
    }

    public static void main(String[] args) {
        new Player(); // Playerのインスタンスを作成してログイン画面を表示
    }
}
