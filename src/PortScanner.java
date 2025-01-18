import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class PortScanner {

    private static final Logger LOGGER = Logger.getLogger(PortScanner.class.getName());
    private static final int TIMEOUT = 100;
    private static final Map<Integer, String> KNOWN_PORTS = new HashMap<>();

    static {
        KNOWN_PORTS.put(21, "FTP");
        KNOWN_PORTS.put(22, "SSH");
        KNOWN_PORTS.put(23, "Telnet");
        KNOWN_PORTS.put(25, "SMTP");
        KNOWN_PORTS.put(53, "DNS");
        KNOWN_PORTS.put(80, "HTTP");
        KNOWN_PORTS.put(110, "POP3");
        KNOWN_PORTS.put(143, "IMAP");
        KNOWN_PORTS.put(443, "HTTPS");
        KNOWN_PORTS.put(3306, "MySQL");
        KNOWN_PORTS.put(3389, "RDP");
        KNOWN_PORTS.put(5432, "PostgreSQL");
    }

    public PortScanner() {
        setupLogger();
    }

    private void setupLogger() {
        try {
            FileHandler fileHandler = new FileHandler("port_scanner.log", true);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.ALL);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error during logger setup", e);
        }
    }

    public void scannerPort(String host, int minPort, int maxPort) {
        try {
            // Проверка на корректность ввода
            if (host.startsWith("http://") || host.startsWith("https://")) {
                host = host.substring(host.indexOf("//") + 2);
            }
            InetAddress address = InetAddress.getByName(host);


            int numThreads = Runtime.getRuntime().availableProcessors();
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);

            for (int port = minPort; port <= maxPort; port++) {
                int finalPort = port;
                executor.submit(() -> {
                    try {
                        InetSocketAddress inetSocketAddress = new InetSocketAddress(address, finalPort);
                        try (Socket socket = new Socket()) {
                            socket.connect(inetSocketAddress, TIMEOUT);
                            String service = KNOWN_PORTS.getOrDefault(finalPort, "Unknown");
                            String banner = getBanner(socket, 100);
                            LOGGER.log(Level.INFO, "Port {0} is open. Service: {1}. Banner: {2}", new Object[]{finalPort, service, banner});

                        } catch (IOException e) {
                            LOGGER.log(Level.FINE, "Error connecting to port {0}: {1}", new Object[]{finalPort, e.getMessage()});
                        }
                    }  catch (IllegalArgumentException e) {
                        LOGGER.log(Level.WARNING, "Error creating InetSocketAddress. Host: {0}, Port: {1}: {2}", new Object[]{address,finalPort, e.getMessage()});
                    }
                });
            }
            executor.shutdown();
            while (!executor.isTerminated()){
                //ждем завершения потоков
            }
        } catch (UnknownHostException ex) {
            LOGGER.log(Level.SEVERE, "Invalid host: {0} - {1}", new Object[]{host, ex.getMessage()});
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error during port scan: {0}", e.getMessage());
        }

    }

    private String getBanner(Socket socket, int readTimeout) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            socket.setSoTimeout(readTimeout);
            writer.println("\r\n");
            if (reader.ready()) {
                StringBuilder banner = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null){
                    banner.append(line).append("\n");
                }
                return banner.toString().trim();
            }
            return "No banner received";
        } catch (IOException e) {
            return "Error getting banner: " + e.getMessage();
        }
    }

}
