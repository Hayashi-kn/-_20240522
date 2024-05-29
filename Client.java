import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

import io.github.cdimascio.dotenv.Dotenv;

public class Client {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Consumer<String> messageHandler;

    public Client(Consumer<String> messageHandler) {
        try {
            setMessageHandler(messageHandler);
            // TODO ルートディレクトリに「.env」ファイルを作成し、「IPADDR=自分のipアドレス」とかいてください
            Dotenv dotenv = Dotenv.load();
            String ipAddress = dotenv.get("IPADDR");
            socket = new Socket(ipAddress, 1234);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(this::listen).start();
        } catch (IOException e) {
            System.out.println("Unable to connect to server: " + e.getMessage());
        }
    }

    public void setMessageHandler(Consumer<String> handler) {
        this.messageHandler = handler;
    }

    private void listen() {
        System.out.println("Listening for messages from server...");
        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (messageHandler != null) {
                    messageHandler.accept(line);
                } else {
                    System.out.println("Received message: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading from server: " + e.getMessage());
        }
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }
}
