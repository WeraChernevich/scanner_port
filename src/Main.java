public class Main {


    public static void main(String[] args) {
        String host = "catmolly.com";
        int minPort = 1;
        int maxPort = 1024;
        PortScanner scanner = new PortScanner();
        scanner.scannerPort(host, minPort, maxPort);
        System.out.println("Finish!");
    }


}