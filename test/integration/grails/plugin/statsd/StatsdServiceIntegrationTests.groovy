package grails.plugin.statsd

import org.junit.After
import org.junit.Before
import org.junit.Test

import grails.plugin.statsd.test.IntegrationTestUDPServer

class StatsdServiceIntegrationTests extends GroovyTestCase {

    def statsdService
    IntegrationTestUDPServer server

    @Before
    public void setup() {
        server = new IntegrationTestUDPServer()
        server.start()
    }

    @After
    public void tearDown() {
        server.stop()
    }

    @Test
    public void testIncrement() {
        statsdService.increment('key', 5, 1.0)
        Thread.sleep(5)
        assert server.messages.size() == 1
        assert server.messages[0] == "key:5|c"
    }

    @Test
    public void testTiming() {
        statsdService.timing('key2', 42, 1.0)
        Thread.sleep(5)
        assert server.messages.size() == 1
        assert server.messages[0] == "key2:42|ms"
    }
}
