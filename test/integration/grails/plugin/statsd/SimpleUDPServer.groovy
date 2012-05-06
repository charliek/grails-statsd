package grails.plugin.statsd

class SimpleUDPServer {

    def messages = []
    DatagramSocket serverSocket
    volatile boolean stop = false

    def start() {
        serverSocket = new DatagramSocket(8125);
        byte[] receiveData = new byte[1024];
        while (!stop) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            String sentence = new String(receivePacket.getData(), receivePacket.offset, receivePacket.length, "utf-8");
            messages << sentence
        }
    }

    def stop() {
        stop = true
        serverSocket.close()
    }
}