import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PortScanner {
    private static final Logger LOGGER = Logger.getLogger(PortScanner.class.getName());
    private static final int MIN_PORT_NUMBER = 0;
    private static final int MAX_PORT_NUMBER = 1_024;
    private static final int TIMEOUT = 500;
    private static final int THREADS = 100;

    public static void scannerPort(String host) {
        System.out.println("Scanning ports: ");
        ExecutorService executorService = Executors.newFixedThreadPool(THREADS);

        for (int i = MIN_PORT_NUMBER; i <= MAX_PORT_NUMBER; i++) {
            final int port = i;
            executorService.execute(() -> {
                var inetSocketAddress = new InetSocketAddress(host, port);

                try (var socket = new Socket()) {
                    socket.connect(inetSocketAddress, TIMEOUT);
                    LOGGER.log(Level.INFO, "Host: {0}, port {1} is opened", new Object[]{host, port});
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Host: {0}, port {1} is closed or unreachable. Reason: {2}", new Object[]{host, port, ex.getMessage()});
                }
            });

        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Finish!");
    }
}
