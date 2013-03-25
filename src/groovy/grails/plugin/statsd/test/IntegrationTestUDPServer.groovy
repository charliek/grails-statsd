package grails.plugin.statsd.test

/**
 * Do not use this class for production in any way. It is exposed as an easy way to verify functionality in
 * integration tests.
 */
class IntegrationTestUDPServer {

    private static final long SLEEP_TIME = 200L
    private List<String> _messages = []
    private DatagramSocket serverSocket
    private volatile boolean stop = false

    def start() {
        try {
            InetAddress address = InetAddress.getByName('127.0.0.1')
            serverSocket = new DatagramSocket(8125, address)
        } catch (Exception e) {
            e.printStackTrace()
            throw e
        }

        Thread.sleep(SLEEP_TIME)
        Thread.start {
            byte[] receiveData = new byte[1024]
            while (!stop) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length)
                serverSocket.receive(receivePacket)
                try {
                    String message = new String(receivePacket.getData(), receivePacket.offset, receivePacket.length, "utf-8")
                    _messages << message
                } catch (Exception e) {
                    e.printStackTrace()
                }
            }
        }
    }

    List<String> getMessages() {
        return _messages
    }

    def stop() {
        stop = true
        serverSocket.close()
        Thread.sleep(SLEEP_TIME)
    }
}