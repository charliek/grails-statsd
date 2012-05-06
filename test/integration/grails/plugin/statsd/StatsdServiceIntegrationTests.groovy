package grails.plugin.statsd

import org.junit.After
import org.junit.Before
import org.junit.Test

class StatsdServiceIntegrationTests extends GroovyTestCase {

    def statsdService
    SimpleUDPServer server

    @Before
    public void setup() {
        server = new SimpleUDPServer()
        Thread.start {
            server.start()
        }
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
