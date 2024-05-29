import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class RecordTable {
    private Object[][] records;
    private String message;
    private int win;
    private int loss;
    private int draw;
    private int givingUp;

    public RecordTable(String message) {
        this.message = message;
        System.out.println("message: " + message);
    }

    private void calculateRecord() {
        calculateTotal();

        String[] parts = message.split(" ");

        if (parts.length > 1) {
            System.out.println("parts: " + parts.length);
            for (int i = 0; i < parts.length; i++) {
                switch (i % 4) {
                    case 0:
                        records[i / 4][0] = parts[i];
                        records[i / 4][4] = "詳細を表示";
                        break;
                    case 1:
                        records[i / 4][1] = parts[i];
                        break;
                    case 2:
                        records[i / 4][2] = parts[i];
                        break;
                    case 3:
                        records[i / 4][3] = parts[i];
                        break;
                }
            }
        }
    }

    private void calculateTotal() {
        // データの初期値を設定
        win = 0;
        loss = 0;
        draw = 0;
        givingUp = 0;

        int winIndex = message.indexOf("win ");// TODO 名前などにwinが含まれている可能性があるのでここは変更
        int lossIndex = message.indexOf("loss ");
        int drawIndex = message.indexOf("draw ");
        int givingUpIndex = message.indexOf("givingUp ");

        while (winIndex != -1) {
            win++;
            winIndex = message.indexOf("win ", winIndex + 1);
        }

        while (lossIndex != -1) {
            loss++;
            lossIndex = message.indexOf("loss ", lossIndex + 1);
        }

        while (drawIndex != -1) {
            draw++;
            drawIndex = message.indexOf("draw ", drawIndex + 1);
        }

        while (givingUpIndex != -1) {
            givingUp++;
            givingUpIndex = message.indexOf("givingUp ", givingUpIndex + 1);
        }

        System.out.println("win: " + win + " loss: " + loss + " draw: " + draw + " givingUp: " + givingUp);

    }

    public JScrollPane getTableScrollPane() {
        // データとカラム名を定義
        String[] columnNames = { "勝敗", "対戦相手", "対戦結果", "日付", "詳細" };
        records = new Object[100][5]; // recordsをフィールドとして定義

        calculateRecord();

        /*
         * // 実際のデータを設定の例
         * records[0][0] = username;
         * records[0][1] = "対戦相手A";
         * records[0][2] = "2-0";
         * records[0][3] = "2024/05/03";
         * 
         * records[1][0] = "負け";
         * records[1][1] = "対戦相手B";
         * records[1][2] = "1-2";
         * records[1][3] = "2024/05/02";
         * 
         * records[2][0] = "勝ち";
         * records[2][1] = "対戦相手C";
         * records[2][2] = "2-1";
         * records[2][3] = "2024/05/01";
         * 
         * records[3][0] = "勝ち";
         * records[3][1] = "対戦相手D";
         * records[3][2] = "2-0";
         * records[3][3] = "2024/04/30";
         * 
         * records[4][0] = "負け";
         * records[4][1] = "対戦相手E";
         * records[4][2] = "0-2";
         * records[4][3] = "2024/04/29";
         * 
         * records[5][0] = "勝ち";
         * records[5][1] = "対戦相手F";
         * records[5][2] = "2-1";
         * records[5][3] = "2024/04/28";
         * 
         * records[6][0] = "負け";
         * records[6][1] = "対戦相手G";
         * records[6][2] = "1-2";
         * records[6][3] = "2024/04/27";
         */

        // モデルとテーブルを作成
        DefaultTableModel model = new DefaultTableModel(records, columnNames);
        JTable table = new JTable(model);

        // ボタンレンダラーとエディターを設定
        table.getColumn("詳細").setCellRenderer(new TableCellRenderer() {
            JButton button = new JButton("詳細を表示");

            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                return button;
            }
        });

        table.getColumn("詳細").setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            protected JButton button;
            private String label;
            private boolean isPushed;

            {
                button = new JButton("詳細を表示");
                button.setOpaque(true);
                button.addActionListener(e -> fireEditingStopped());
            }

            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                    int column) {
                label = "詳細";
                isPushed = true;
                return button;
            }

            public Object getCellEditorValue() {
                if (isPushed) {
                    // ボタンが押されたときのアクション
                    String result = (String) table.getValueAt(table.getSelectedRow(), 0); // 勝敗の値を取得
                    String opponent = (String) table.getValueAt(table.getSelectedRow(), 1); // 対戦相手の値を取得
                    String details = "詳細：" + result + " 対戦相手：" + opponent; // 表示する詳細情報
                    JOptionPane.showMessageDialog(button, details);
                }
                isPushed = false;
                return label;
            }

            public boolean stopCellEditing() {
                isPushed = false;
                return super.stopCellEditing();
            }
        });

        // テーブルを含むJScrollPaneを返す
        JScrollPane scrollPane = new JScrollPane(table);
        return scrollPane;
    }

    public int getWinRecords() {
        return win;
    }

    public int getLossRecords() {
        return loss;
    }

    public int getGivingUpRecords() {
        return givingUp;
    }

    public int getDrawRecords() {
        return draw;
    }
}