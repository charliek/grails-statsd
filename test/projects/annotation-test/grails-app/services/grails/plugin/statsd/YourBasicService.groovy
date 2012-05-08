package grails.plugin.statsd

import java.util.concurrent.TimeUnit
import grails.plugin.statsd.StatsdTimer
import grails.plugin.statsd.StatsdCounter

class YourBasicService {

    private int x = 42

    public int methodWithCounterAndAnnotationParamsProxy(int y, int z) {
        return methodWithCounterAndAnnotationParams(y, z)
    }

    public int methodWithTimerAndDefaultsProxy() {
        return methodWithTimerAndDefaults()
    }

    @StatsdCounter(key='counterAndDefaults')
    public int methodWithCounterAndDefaults() {
        return 6 + 7
    }

    @StatsdCounter(key='counterAndMagnitude', magnitude=5)
    public int methodWithCounterAndMagnitude(int x) {
        return x + 4
    }

    @StatsdCounter(key='counterAndZero', magnitude=0, sampleRate=0.0d)
    public int methodWithCounterAndZeroSampling(int z) {
        return z
    }

    @StatsdCounter(key='#{x}.#{y}.#{z}', magnitude=3, sampleRate=2.0d)
    private int methodWithCounterAndAnnotationParams(int y, int z) {
        return y + z
    }

    @StatsdCounter(key='test#{x}', magnitude=42, sampleRate=2.0d)
    public void methodWithCounterAndVoidReturn(int x) {
        // Void functions can be handled differently so we have a test
        1 + 1
    }


    @StatsdTimer(key='timerAndDefaults')
    private int methodWithTimerAndDefaults() {
        return 6 + 7
    }

    @StatsdTimer(key='timerAndZero', sampleRate=0.0d)
    public int methodWithTimerAndZeroSampling(int z) {
        return z
    }

    @StatsdTimer(key='#{z}.#{x}.#{y}', sampleRate=2.0d)
    public int methodWithTimerAndAnnotationParams(int y, int z) {
        return y + z
    }

    @StatsdTimer(key='test#{x}', sampleRate=2.0d)
    public void methodWithTimerAndVoidReturn(int x) {
        // Void functions can be handled differently so we have a test
        1 + 1
    }
}
