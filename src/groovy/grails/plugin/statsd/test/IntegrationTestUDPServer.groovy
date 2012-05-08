package grails.plugin.statsd.test

/**
 * Do not use this class for production in any way. It is exposed as an easy way to verify functionality in
 * integration tests.
 */
class IntegrationTestUDPServer {

    def messages = []
    DatagramSocket serverSocket
    volatile boolean stop = false

    def start() {
        Thread.start {
            serverSocket = new DatagramSocket(8125);
            byte[] receiveData = new byte[1024];
            while (!stop) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String sentence = new String(receivePacket.getData(), receivePacket.offset, receivePacket.length, "utf-8");
                messages << sentence
            }
        }
    }

    def stop() {
        stop = true
        serverSocket.close()
    }
}