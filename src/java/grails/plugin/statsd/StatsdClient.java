package grails.plugin.statsd;

/**
 * StatsdClient.java
 *
 * (C) 2011 Meetup, Inc.
 * Author: Andrew Gwozdziewycz <andrew@meetup.com>, @apgwoz
 *
 *
 *
 * Example usage:
 *
 *    StatsdClient client = new StatsdClient("statsd.example.com", 8125);
 *    // increment by 1
 *    client.increment("foo.bar.baz");
 *    // increment by 10
 *    client.increment("foo.bar.baz", 10);
 *    // sample rate
 *    client.increment("foo.bar.baz", 10, .1);
 *    // increment multiple keys by 1
 *    client.increment("foo.bar.baz", "foo.bar.boo", "foo.baz.bar");
 *    // increment multiple keys by 10 -- yeah, it's "backwards"
 *    client.increment(10, "foo.bar.baz", "foo.bar.boo", "foo.baz.bar");
 *    // multiple keys with a sample rate
 *    client.increment(10, .1, "foo.bar.baz", "foo.bar.boo", "foo.baz.bar");
 *
 * Note: For best results, and greater availability, you'll probably want to
 * create a wrapper class which creates a static client and proxies to it.
 *
 * You know... the "Java way."
 */

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Random;

public class StatsdClient {
    private static Random RNG = new Random();
    private static Logger log = Logger.getLogger(StatsdClient.class.getName());

    private InetSocketAddress _address;
    private DatagramChannel _channel;

    public StatsdClient(String host, int port) throws UnknownHostException, IOException {
        this(InetAddress.getByName(host), port);
    }

    public StatsdClient(InetAddress host, int port) throws IOException {
        _address = new InetSocketAddress(host, port);
        _channel = DatagramChannel.open();
    }

    protected boolean isOpen() {
        return _channel.isOpen();
    }

    protected void close() throws IOException {
        _channel.close();
    }

    public boolean send(double sampleRate, String... stats) {

        boolean retval = false; // didn't send anything
        if (sampleRate < 1.0) {
            for (String stat : stats) {
                if (RNG.nextDouble() <= sampleRate) {
                    stat = String.format("%s|@%f", stat, sampleRate);
                    if (doSend(stat)) {
                        retval = true;
                    }
                }
            }
        } else {
            for (String stat : stats) {
                if (doSend(stat)) {
                    retval = true;
                }
            }
        }

        return retval;
    }

    private boolean doSend(final String stat) {
        try {
            final byte[] data = stat.getBytes("utf-8");
            final ByteBuffer buff = ByteBuffer.wrap(data);
            final int nbSentBytes = _channel.send(buff, _address);

            if (data.length == nbSentBytes) {
                return true;
            } else {
                log.error(String.format(
                        "Could not send entirely stat %s to host %s:%d. Only sent %i bytes out of %i bytes", stat,
                        _address.getHostName(), _address.getPort(), nbSentBytes, data.length));
                return false;
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            log.error(
                    String.format("Could not send stat %s to host %s:%d", stat, _address.getHostName(),
                            _address.getPort()), e);
            return false;
        }
    }
}