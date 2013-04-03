package grails.plugin.statsd

class StatsdService {

    static transactional = false

    def statsdPool
    def statsdTimingService

    private void withClient(Closure closure) {
        StatsdClient client = (StatsdClient) statsdPool.borrowObject()
        try {
            closure.call(client)
        } finally {
            statsdPool.returnObject(client)
        }
    }

    public def withTimer(String key, Closure closure) {
        return withTimer(key, 1.0, closure)
    }

    public def withTimer(String key, double sampleRate, Closure closure) {
        log.debug("Start timer : ${key}")
        long startTime = statsdTimingService.currentTimeMillis()
        def result = closure()
        long finishTime = statsdTimingService.currentTimeMillis()
        long runTime = finishTime - startTime
        timing(key, runTime.toInteger(), sampleRate)
        log.debug("End timer : ${key} : ${runTime}ms")
        return result
    }

    public void timing(String key, int value) {
        timing(key, value, 1.0)
    }

    public void timing(String key, int value, double sampleRate) {
        withClient { client ->
            client.send(sampleRate, String.format("%s:%d|ms", key, value));
        }
    }

    public void decrement(String key) {
        increment(key, -1, 1.0)
    }

    public void decrement(String key, int magnitude) {
        decrement(key, magnitude, 1.0)
    }

    public void decrement(String key, int magnitude, double sampleRate) {
        magnitude = magnitude < 0 ? magnitude : -magnitude
        increment(key, magnitude, sampleRate)
    }

    public void increment(String key) {
        increment(key, 1, 1.0);
    }

    public void increment(String key, int magnitude) {
        increment(key, magnitude, 1.0)
    }

    public void increment(String key, int magnitude, double sampleRate) {
        String stat = String.format("%s:%s|c", key, magnitude);
        withClient { client ->
            client.send(sampleRate, stat);
        }
    }

    public void gauge(String key, double magnitude){
        gauge(key, magnitude, 1.0);
    }

    public void gauge(String key, double magnitude, double sampleRate){
        String stat = String.format("%s:%s|g", key, magnitude);
        withClient { client ->
            client.send(sampleRate, stat);
        }
    }

}
