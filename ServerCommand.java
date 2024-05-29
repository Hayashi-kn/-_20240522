import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Deque;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import io.github.cdimascio.dotenv.Dotenv;

interface ServerCommand {
    void execute(PrintWriter out, String args);
}

class SignupCommand implements ServerCommand {
    @Override
    public void execute(PrintWriter out, String args) {
        String name = args.split(" ")[0];
        String password = args.split(" ")[1];

        System.out.println("username: " + name);
        System.out.println("password: " + password);

        // TODO mysqlが無い人（ゆうき以外）以下はコメントアウトしてください
        Dotenv dotenv = Dotenv.load();
        String url = "jdbc:mysql://localhost:3306/othello?serverTimezone=Asia/Tokyo"; // データベースのURL
        String dbUsername = dotenv.get("DB_USER"); // データベースのユーザー名
        String dbPassword = dotenv.get("DB_PASS"); // データベースのパスワード


        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            System.out.println("MySQLに接続しました。");

            // 同じユーザー名が存在しないか確認
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM users WHERE username = '" + name + "'");
            if (resultSet.next()) {
                out.println("signupFailed : User " + name + " already exists.");
                return;
            }

            // ユーザーを登録
            statement.executeUpdate(
                    "INSERT INTO users (username, password) VALUES ('" + name + "', '" + password + "')");
            out.println("signupSuccess");

            statement.close();

        } catch (SQLException e) {
            out.println("signupFailed : Failed to register user " + name);
            System.out.println("MySQLへの接続に失敗しました。");
            e.printStackTrace();
        }

        // TODO mysqlが無い人以下をコメントアウトから外してください
        // ユーザ登録ロジック（ダミー）
        // out.println("signup success : User " + args + " registered successfully.");
    }
}

class LoginCommand implements ServerCommand {
    @Override
    public void execute(PrintWriter out, String args) {
        System.out.println("login " + args);

        String username = args.split(" ")[0];
        String password = args.split(" ")[1];

        Dotenv dotenv = Dotenv.load();
        String url = "jdbc:mysql://localhost:3306/othello?serverTimezone=Asia/Tokyo"; // データベースのURL
        String dbUsername = dotenv.get("DB_USER"); // データベースのユーザー名
        String dbPassword = dotenv.get("DB_PASS"); // データベースのパスワード

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            System.out.println("MySQLに接続しました。");

            // ユーザー名とパスワードが一致するか確認
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                    "SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'");
            if (resultSet.next()) {
                out.println("loginSuccess");
            } else {
                out.println("loginFailed : Invalid username or password.");
            }

            statement.close();

        } catch (SQLException e) {
            out.println("loginFailed : Failed to login user " + username);
            System.out.println("MySQLへの接続に失敗しました。");
            e.printStackTrace();
        }

        // ユーザ認証ロジック（ダミー）
        // out.println("login success : User " + args + " logged in successfully.");
    }
}

class FetchUserIdCommand implements ServerCommand {
    @Override
    public void execute(PrintWriter out, String args) {
        String userId = args;

        Dotenv dotenv = Dotenv.load();
        String url = "jdbc:mysql://localhost:3306/othello?serverTimezone=Asia/Tokyo"; // データベースのURL
        String dbUsername = dotenv.get("DB_USER"); // データベースのユーザー名
        String dbPassword = dotenv.get("DB_PASS"); // データベースのパスワード

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            System.out.println("MySQLに接続しました。");

            // ユーザー名に対応するユーザーIDを取得
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT id FROM users WHERE username = '" + userId + "'");
            if (resultSet.next()) {
                out.println("fetchUserIdSuccess " + resultSet.getInt("id"));
            } else {
                out.println("fetchUserIdFailed : userId not found");
            }

            statement.close();

        } catch (SQLException e) {
            out.println("fetchUserIdFailed : userId not found");
            System.out.println("MySQLへの接続に失敗しました。");
            e.printStackTrace();
        }
    }
}
class ChangeUsernameCommand implements ServerCommand {
    @Override
    public void execute(PrintWriter out, String args) {
        String username = args.split(" ")[0];
        String changename = args.split(" ")[1];
        
        Dotenv dotenv = Dotenv.load();
        String url = "jdbc:mysql://localhost:3306/othello?serverTimezone=Asia/Tokyo"; // データベースのURL
        String dbUsername = dotenv.get("DB_USER"); // データベースのユーザー名
        String dbPassword = dotenv.get("DB_PASS"); // データベースのパスワード

        Connection connection = null;
        PreparedStatement preparedStatement1 = null;
        PreparedStatement preparedStatement2 = null;
        PreparedStatement preparedStatement3 = null;

        try {
            // MySQLデータベースに接続
            connection = DriverManager.getConnection(url, dbUsername, dbPassword);

            // changenameが既に使用されているかどうかを確認
            String query = "SELECT COUNT(*) FROM users WHERE username = ?";
            preparedStatement1 = connection.prepareStatement(query);
            preparedStatement1.setString(1, changename);
            ResultSet resultSet = preparedStatement1.executeQuery();
            resultSet.next();
            int count = resultSet.getInt(1);
            if (count > 0) {
                out.println("changenameFailed : The specified changename is already in use.");
                return;
            }

            // usernameが存在するかどうかを確認
            query = "SELECT COUNT(*) FROM users WHERE username = ?";
            preparedStatement2 = connection.prepareStatement(query);
            preparedStatement2.setString(1, username);
            resultSet = preparedStatement2.executeQuery();
            resultSet.next();
            count = resultSet.getInt(1);
            if (count == 0) {
                out.println("changenameFailed : The specified username does not exist.");
                return;
            }

            // usernameを更新
            query = "UPDATE users SET username = ? WHERE username = ?";
            preparedStatement3 = connection.prepareStatement(query);
            preparedStatement3.setString(1, changename);
            preparedStatement3.setString(2, username);
            preparedStatement3.executeUpdate();
            out.println("changenameSuccess");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // JDBCリソースをクローズ
            try {
                if (preparedStatement1 != null) {
                    preparedStatement1.close();
                }
                if (preparedStatement2 != null) {
                    preparedStatement2.close();
                }
                if (preparedStatement3 != null) {
                    preparedStatement3.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

class FetchRecordsCommand implements ServerCommand {
    @Override
    public void execute(PrintWriter out, String args) {
        String userId = args;
        int myId = Integer.parseInt(userId);

        Dotenv dotenv = Dotenv.load();
        String url = "jdbc:mysql://localhost:3306/othello?serverTimezone=Asia/Tokyo"; // データベースのURL
        String dbUsername = dotenv.get("DB_USER"); // データベースのユーザー名
        String dbPassword = dotenv.get("DB_PASS"); // データベースのパスワード


        StringBuilder outputBuilder = new StringBuilder(); // StringBuilderを使って文字列を追加する

        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            System.out.println("MySQLに接続しました。");

            // SQLクエリを実行してデータを取得
            String query = "SELECT p.id, u.username AS opponent_name, p.result, p.date " +
                           "FROM records p " +
                           "JOIN users u ON p.opponent_id = u.id " +
                           "WHERE p.my_id = ? " +
                           "ORDER BY p.date ASC"; // new_dateの古い順にソート
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, myId);
                ResultSet resultSet = preparedStatement.executeQuery();

                boolean foundData = false; // データが見つかったかどうかのフラグを追加
                // 結果を処理して出力
                while (resultSet.next()) {
                    foundData = true; // データが見つかったことをフラグで示す
                    int id = resultSet.getInt("id");
                    String opponentName = resultSet.getString("opponent_name");
                    String result = resultSet.getString("result");
                    String date = resultSet.getString("date");
                    outputBuilder.append(id).append(" ").append(opponentName).append(" ").append(result).append(" ").append(date).append(" ");
                }

                if (foundData) {
                	String output = outputBuilder.toString(); // 最終的な文字列を取得
                	out.println("fetchRecordsSuccess " + output);
                } else {
                	out.println("fetchRecordsFailed : userId not found");
                }

            }

        } catch (SQLException e) {
            out.println("fetchRecordsFailed : userId not found");
            System.out.println("MySQLへの接続に失敗しました。");
            e.printStackTrace();
        }
    }
    
}

class FetchDetaleRecordsCommand implements ServerCommand {
    @Override
    public void execute(PrintWriter out, String args) {
    	int id = Integer.parseInt(args.split(" ")[0]);
    	
    	Dotenv dotenv = Dotenv.load();
        String url = "jdbc:mysql://localhost:3306/othello?serverTimezone=Asia/Tokyo"; // データベースのURL
        String dbUsername = dotenv.get("DB_USER"); // データベースのユーザー名
        String dbPassword = dotenv.get("DB_PASS"); // データベースのパスワード
        
        try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            // SQLクエリの準備
            String sql = "SELECT grids FROM records WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                // クエリの実行
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // グリッドを取得してコンソールに出力
                        String grids = rs.getString("grids");
                        System.out.println("fetchDetaleRecordsSuccess " + grids);
                        out.println("fetchDetaleRecordsSuccess " + grids);
                    } else {
                        System.out.println("fetchDetaleRecordsFailed : No record found for ID " + id);
                        out.println("fetchDetaleRecordsFailed : No record found for ID " + id);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
}
class InsertRecordsCommand implements ServerCommand {
    @Override
    public void execute(PrintWriter out, String args) {
    	int myId = Integer.parseInt(args.split(" ")[0]);
    	int opponentId = Integer.parseInt(args.split(" ")[1]);
        String result = args.split(" ")[2];

     // 現在の日時を取得
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String date = now.format(formatter);

        Dotenv dotenv = Dotenv.load();
        String url = "jdbc:mysql://localhost:3306/othello?serverTimezone=Asia/Tokyo"; // データベースのURL
        String dbUsername = dotenv.get("DB_USER"); // データベースのユーザー名
        String dbPassword = dotenv.get("DB_PASS"); // データベースのパスワード
        
        // SQL文の定義
        String sql = "INSERT INTO records (my_id, opponent_id, result, date) VALUES (?, ?, ?, ?)";

        try (
            // JDBC接続の確立
            Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword);
            // PreparedStatementの作成
            PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            // パラメータの設定
            preparedStatement.setInt(1, myId);
            preparedStatement.setInt(2, opponentId);
            preparedStatement.setString(3, result);
            preparedStatement.setString(4, date);

            // SQL文の実行
            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("新しい行が挿入されました。影響を受けた行数: " + rowsAffected);
            out.println("insertRecordsSuccess");
        } catch (SQLException e) {
            System.out.println("データベースエラー: " + e.getMessage());
            out.println("insertRecordsFailed : " + e.getMessage());	
        }
    }
}
class StartMatchingCommand implements ServerCommand {
    private Deque<Socket> waitingClients;
    private ConcurrentHashMap<Socket, PrintWriter> clientWriters;
    private ConcurrentHashMap<Socket, BufferedReader> clientReaders;
    private ConcurrentHashMap<Socket, Boolean> clientMatching;

    public StartMatchingCommand(Deque<Socket> waitingClients,
            ConcurrentHashMap<Socket, PrintWriter> clientWriters,
            ConcurrentHashMap<Socket, BufferedReader> clientReaders,
            ConcurrentHashMap<Socket, Boolean> clientMatching) {
        this.waitingClients = waitingClients;
        this.clientWriters = clientWriters;
        this.clientReaders = clientReaders;
        this.clientMatching = clientMatching;
    }

    @Override
    public void execute(PrintWriter out, String args) {
        Socket clientSocket = findSocketByOutputWriter(out);

        if (clientSocket == null) {
            out.println("Your connection is not recognized.");
            return;
        }

        synchronized (waitingClients) {
            if (!waitingClients.isEmpty()) {
                Socket otherClient = waitingClients.pop();
                if (otherClient != null && otherClient != clientSocket) {
                    PrintWriter outOther = clientWriters.get(otherClient);
                    BufferedReader inCurrent = clientReaders.get(clientSocket);
                    BufferedReader inOther = clientReaders.get(otherClient);

                    if (outOther != null && inOther != null && inCurrent != null) {
                    	clientMatching.put(clientSocket, true);
                        clientMatching.put(otherClient, true);
                        new Thread(new ClientPairHandler(clientSocket, otherClient)).start();
                    } else {
                        out.println("Failed to start matching due to an internal error.");
                        waitingClients.push(clientSocket); // 再び待機リストに戻す
                    }
                } else {
                    waitingClients.push(clientSocket);
                    out.println("Waiting for another client to start matching...");
                }
            } else {
                waitingClients.push(clientSocket);
                out.println("Waiting for another client to start matching...");
            }
        }
    }

    private Socket findSocketByOutputWriter(PrintWriter out) {
        for (Entry<Socket, PrintWriter> entry : clientWriters.entrySet()) {
            if (entry.getValue().equals(out)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    private static class ClientPairHandler implements Runnable {
            private Socket client1;
            private Socket client2;

            public ClientPairHandler(Socket clientOne, Socket clientTwo) {
                this.client1 = clientOne;
                this.client2 = clientTwo;
            }

            public void run() {
                try {
                    PrintWriter out1 = new PrintWriter(client1.getOutputStream(), true);
                    PrintWriter out2 = new PrintWriter(client2.getOutputStream(), true);
                    BufferedReader in1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));
                    BufferedReader in2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));

                    // 各クライアント用のスレッドを作成
                    Thread client1Thread = new Thread(() -> handleClient(in1, out2, out1, true));
                    Thread client2Thread = new Thread(() -> handleClient(in2, out1, out2, false));

                    // スレッドを開始
                    client1Thread.start();
                    client2Thread.start();

                    // 両スレッドの終了を待つ
                    client1Thread.join();
                    client2Thread.join();
                } catch (IOException | InterruptedException e) {
                    System.out.println("Error handling client pair: " + e.getMessage());
                } finally {
                    try {
                        client1.close();
                        client2.close();
                    } catch (IOException e) {
                        System.out.println("Failed to close client sockets");
                    }
                }
            }

            private void handleClient(BufferedReader in, PrintWriter otherOut, PrintWriter out, boolean isFirst) {
                try {
                    out.println("matchSuccess");
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {

                        out.println("Message from client: " + inputLine);
                        String[] parts = inputLine.split(" ", 2);
                        String command = parts[0];
                        boolean isToMe = command.equals("fetchResultRecords");
                        String args = parts.length > 1 ? parts[1] : "";

                        ServerCommand cmd = Server.commandMap.get(command);
                        if (cmd != null) {
                            if (isToMe) {
                                cmd.execute(out, args);
                            } else {
                                cmd.execute(otherOut, args);
                            }
                        } else {
                            out.println("Unknown command: " + command);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Error in client communication: " + e.getMessage());
                }
            }
        }

}

class FetchOpponentnameCommand implements ServerCommand {
    @Override
    public void execute(PrintWriter out, String args) {
        out.println("opponentname " + args);
    }
}

class EndMatchingCommand implements ServerCommand {
    private Deque<Socket> waitingClients;
    private ConcurrentHashMap<Socket, Boolean> clientMatching;

    public EndMatchingCommand(Deque<Socket> waitingClients, ConcurrentHashMap<Socket, Boolean> clientMatching) {
        this.waitingClients = waitingClients;
        this.clientMatching = clientMatching;
    }

    @Override
    public void execute(PrintWriter out, String args) {
        Socket clientSocket = findSocketByOutputWriter(out);

        if (clientSocket == null) {
            out.println("Your connection is not recognized.");
            return;
        }

        synchronized (waitingClients) {
            if (clientMatching.get(clientSocket)) {
                clientMatching.put(clientSocket, false);
                waitingClients.push(clientSocket);
                out.println("Matching ended. Waiting for another client to start matching...");
            } else {
                out.println("You are not in matching mode.");
            }
        }
    }

    private Socket findSocketByOutputWriter(PrintWriter out) {
        System.out.println("Looking for socket with given PrintWriter...");
        for (Entry<Socket, PrintWriter> entry : Server.clientWriters.entrySet()) {
            System.out.println("Checking socket: " + entry.getKey() + ", PrintWriter: " + entry.getValue());
            if (entry.getValue().equals(out)) {
                System.out.println("Match found!");
                return entry.getKey();
            }
        }
        System.out.println("No matching socket found.");
        return null;
    }

}

class FetchResultRecordsCommand implements ServerCommand {
    @Override
    public void execute(PrintWriter out, String args) {
    	int myId = Integer.parseInt(args.split(" ")[0]);
    	int opponentId = Integer.parseInt(args.split(" ")[1]);
    	
    	String url = "jdbc:mysql://localhost:3306/othello?serverTimezone=Asia/Tokyo"; // データベースのURL
        String dbUsername = "root"; // データベースのユーザー名
        String dbPassword = "sql0522"; // データベースのパスワード
        
        StringBuilder outputBuilder = new StringBuilder(); // StringBuilderを使って文字列を追加する

        // 接続とクエリの実行
        try (Connection connection = DriverManager.getConnection(url, dbUsername, dbPassword)) {
            // クエリの準備
            String sql = "SELECT result FROM records WHERE my_id = ? AND opponent_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                // パラメータの設定
            	preparedStatement.setInt(1, myId);
            	preparedStatement.setInt(2, opponentId);
                
                ResultSet resultSet = preparedStatement.executeQuery();
                
                boolean foundData = false; // データが見つかったかどうかのフラグを追加

                while (resultSet.next()) {
                	foundData = true; // データが見つかったことをフラグで示す
                	String result = resultSet.getString("result");
                	outputBuilder.append(result).append(" ");
                }
                
                if (foundData) {
                	String output = outputBuilder.toString(); // 最終的な文字列を取得
                	System.out.println("fetchResultRecordsSuccess " + output);
                	out.println("fetchResultRecordsSuccess " + output);
                } else {
                	System.out.println("fetchResultRecordsFailed : not found");
                	out.println("fetchResultRecordsFailed : not found");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
}

class SendChatMessageCommand implements ServerCommand {
    @Override
    public void execute(PrintWriter out, String args) {
        if (args.length() > 0) {
            StringBuilder message = new StringBuilder();
            String[] splitArgs = args.split("\\s+"); // argsを空白で分割
            for (String arg : splitArgs) {
            	message.append(arg).append(" ");
            }
            System.out.println("sendChatMessageSuccess " + message.toString().trim());
            out.println("sendChatMessageSuccess " + message.toString().trim());
        } else {
            out.println("fetchResultRecordsFailed : No argument provided");
        }
    }
}

class SendGridCommand implements ServerCommand {
    @Override
    public void execute(PrintWriter out, String args) {
    	if (args.length() > 0) {
    		String grids = args.split(" ")[0];
            System.out.println("sendGridSuccess " + grids);
            out.println("sendGridSuccess " + grids);
        } else {
            out.println("sendGridFailed : No argument provided");
        }
    }
}