import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    static Map<String, ServerCommand> commandMap = new HashMap<>();
    private static Deque<Socket> waitingClients = new ArrayDeque<>();
    static ConcurrentHashMap<Socket, PrintWriter> clientWriters = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Socket, BufferedReader> clientReaders = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Socket, Boolean> clientMatching = new ConcurrentHashMap<>();

    static {
        commandMap.put("login", new LoginCommand());
        commandMap.put("signup", new SignupCommand());
        commandMap.put("fetchUserId", new FetchUserIdCommand());
        commandMap.put("fetchRecords", new FetchRecordsCommand());
        commandMap.put("startMatching", new StartMatchingCommand(waitingClients, clientWriters, clientReaders, clientMatching));
        commandMap.put("fetchOpponentUsername", new FetchOpponentnameCommand());
        commandMap.put("endMatching", new EndMatchingCommand(waitingClients, clientMatching));
        commandMap.put("fetchResultRecords", new FetchResultRecordsCommand());
        commandMap.put("sendChatMessage", new SendChatMessageCommand());
        commandMap.put("sendGrid", new SendGridCommand());
        // TODO 他のコマンドを追加
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        System.out.println("Server is running...");

        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                clientWriters.put(clientSocket, writer);
                clientReaders.put(clientSocket, reader);
                clientMatching.put(clientSocket, false);

                new Thread(() -> handleClient(clientSocket, writer, reader)).start();
            }
        } finally {
            serverSocket.close();
        }
    }

    private static void handleClient(Socket clientSocket, PrintWriter out, BufferedReader in) {
        try {
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                if (clientMatching.get(clientSocket)) {
                    System.out.println("ignore command: " + inputLine + " because client is in matching mode");
                    synchronized (clientSocket) {
                        if (clientMatching.get(clientSocket)) {
                            try {
                                // モニターを取得してから待機
                                clientSocket.wait();
                            } catch (InterruptedException e) {
                                System.out.println("Thread interrupted: " + e.getMessage());
                            }
                        }
                    }
                    continue; // Ignore other commands when matching
                }

                System.out.println("Received message: " + inputLine);
                String[] parts = inputLine.split(" ", 2);
                String command = parts[0];
                String args = parts.length > 1 ? parts[1] : "";

                ServerCommand cmd = commandMap.get(command);
                if (cmd != null) {
                    cmd.execute(out, args);
                } else {
                    out.println("Unknown command: " + command);
                }
            }
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientReaders.remove(clientSocket);
                clientWriters.remove(clientSocket);
                clientMatching.remove(clientSocket);
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Failed to close client socket");
            }
        }
    }
}