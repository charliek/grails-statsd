package grails.plugin.statsd.annotation

import org.junit.Before
import grails.plugin.statsd.test.IntegrationTestUDPServer
import org.junit.After
import org.junit.Test
import grails.plugin.statsd.StatsdTimingService

class AnnotationIntegrationTests extends GroovyTestCase {

    private static final long SLEEP_TIME = 25L
    def statsdService
    IntegrationTestUDPServer server
    def yourBasicService
    def statsdTimingService

    @Before
    public void setup() {
        // Don't like to stub out within integration tests, but the time dependency
        // causes non-deterministic tests
        long currentMs = 0
        statsdService.statsdTimingService = [currentTimeMillis: { ->
            currentMs += 2
            return currentMs
        }] as StatsdTimingService

        server = new IntegrationTestUDPServer()
        server.start()
    }

    @After
    public void tearDown() {
        statsdService.statsdTimingService = statsdTimingService
        server.stop()
    }

    @Test
    public void testCountAnnotationWithDefaults() {
        assert yourBasicService.methodWithCounterAndDefaults() == 13
        Thread.sleep(SLEEP_TIME)
        assert server.messages.size() == 1
        assert server.messages[0] == "counterAndDefaults:1|c"
    }

    @Test
    public void testCountAnnotationWithMagnitude() {
        assert yourBasicService.methodWithCounterAndMagnitude(9) == 13
        Thread.sleep(SLEEP_TIME)
        assert server.messages.size() == 1
        assert server.messages[0] == "counterAndMagnitude:5|c"
    }

    @Test
    public void testCountAnnotationWithZeroSamplingRate() {
        assert yourBasicService.methodWithCounterAndZeroSampling(13) == 13
        Thread.sleep(SLEEP_TIME)
        assert server.messages.size() == 1
        // TODO this test is verifying that zero sampling rates and magnitudes are not supported
        assert server.messages[0] == "counterAndZero:1|c"
    }

    @Test
    public void testCountAnnotationWithParameters() {
        assert yourBasicService.methodWithCounterAndAnnotationParamsProxy(4, 2) == 6
        Thread.sleep(SLEEP_TIME)
        assert server.messages.size() == 1
        assert server.messages[0] == "42.4.2:3|c"
    }

    @Test
    public void testCountAnnotationWithVoidReturn() {
        assert yourBasicService.methodWithCounterAndVoidReturn(2) == null
        Thread.sleep(SLEEP_TIME)
        assert server.messages.size() == 1
        assert server.messages[0] == "test2:42|c"
    }

    @Test
    public void testTimerAnnotationWithDefaults() {
        assert yourBasicService.methodWithTimerAndDefaultsProxy() == 13
        Thread.sleep(SLEEP_TIME)
        assert server.messages.size() == 1
        assert server.messages[0] == "timerAndDefaults:2|ms"
    }

    @Test
    public void testTimerAnnotationWithZeroSamplingRate() {
        assert yourBasicService.methodWithTimerAndZeroSampling(13) == 13
        Thread.sleep(SLEEP_TIME)
        assert server.messages.size() == 1
        // TODO this test is verifying that zero sampling rates are not supported
        assert server.messages[0] == "timerAndZero:2|ms"
    }

    @Test
    public void testTimerAnnotationWithParameters() {
        assert yourBasicService.methodWithTimerAndAnnotationParams(4, 2) == 6
        Thread.sleep(SLEEP_TIME)
        assert server.messages.size() == 1
        assert server.messages[0] == "2.42.4:2|ms"
    }

    @Test
    public void testTimerAnnotationWithVoidReturn() {
        assert yourBasicService.methodWithTimerAndVoidReturn(2) == null
        Thread.sleep(SLEEP_TIME)
        assert server.messages.size() == 1
        assert server.messages[0] == "test2:2|ms"
    }
}
