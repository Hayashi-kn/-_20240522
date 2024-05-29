import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class Record {

    private JFrame frame;
    private String username;
    private Client client;
    private RecordTable recordTable;

    public Record(Client client, String username) {
        this.username = username;
        this.client = client;
        client.setMessageHandler(this::handleServerMessage);
        fetchUserId(username);
    }

    private void initializeRecordUI() {
        frame = new JFrame();
        frame.setTitle(username + "のマイページ");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());

        JButton matchButton = new JButton("対戦する");
        matchButton.setPreferredSize(new Dimension(100, 50));
        matchButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        matchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startBattle(); // 対戦するボタンが押されたら対戦を開始
            }
        });
        topPanel.add(matchButton, BorderLayout.EAST);

        JLabel titleLabel = new JLabel(username + "の対戦成績", JLabel.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        topPanel.add(titleLabel, BorderLayout.CENTER);

        frame.add(topPanel, BorderLayout.NORTH);
        ;
        JScrollPane tableScrollPane = recordTable.getTableScrollPane();
        frame.getContentPane().add(tableScrollPane, BorderLayout.CENTER);

        JLabel totalStatsLabel = new JLabel("Total " + getTotal(), JLabel.CENTER);
        totalStatsLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        frame.add(totalStatsLabel, BorderLayout.SOUTH);

        frame.setSize(800, 600);
        frame.setVisible(true);
    }

    // public void show() {
    // SwingUtilities.invokeLater(() -> frame.setVisible(true));
    // }

    private void handleServerMessage(String message) {
        System.out.println("Received message: " + message);
        if (message.startsWith("userId")) { // fetchUserIdコマンドのレスポンス
            String[] parts = message.split(" ");
            String userId = parts[1];
            System.out.println("User ID: " + userId);
            fetchRecords(userId);
        } else if (message.startsWith("records")) { // fetchRecordsコマンドのレスポンス
            String[] parts = message.split(" ", 2);
            String recordMessage = parts[1];
            recordTable = new RecordTable(recordMessage);
            initializeRecordUI();

        }
    }

    private void fetchUserId(String username) {
        client.sendMessage("fetchUserId " + username);
    }

    private void fetchRecords(String userId) {
        client.sendMessage("fetchRecords " + userId);
    }

    private String getTotal() {

        String win = String.valueOf(recordTable.getWinRecords());
        String loss = String.valueOf(recordTable.getLossRecords());
        String givingUp = String.valueOf(recordTable.getGivingUpRecords());
        String draw = String.valueOf(recordTable.getDrawRecords());

        String returnMessage = win + "勝" + loss + "負" + givingUp + "投了" + draw + "引き分け";

        return returnMessage;
    }

    // 対戦を開始するメソッド
    private void startBattle() {
        // ここに対戦を開始する処理を書く
        System.out.println("対戦を開始します");
        // 対戦が開始されたことをユーザーに通知
        JOptionPane.showMessageDialog(null, "対戦を開始します", "対戦開始", JOptionPane.INFORMATION_MESSAGE);
    }
}