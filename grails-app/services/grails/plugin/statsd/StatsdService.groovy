package grails.plugin.statsd

import statsd.StatsdClient
import org.apache.commons.pool.impl.GenericObjectPool

class StatsdService {

    GenericObjectPool statsdPool

    private void withClient(Closure closure) {
        StatsdClient client = (StatsdClient) statsdPool.borrowObject()
        try {
            closure.call(client)
        } finally {
            statsdPool.returnObject(client)
        }
    }

    public void increment(String metric) {
        log.debug("counting ${metric}")
        withClient { client ->
            client.increment(metric)
        }
    }

    public def withTimer(String key, Closure closure) {
        return withTimer(key, 1.0, closure)
    }

    public def withTimer(String key, double sampleRate, Closure closure) {
        log.debug("Start timer : ${key}")
        long startTime = System.currentTimeMillis()
        def result = closure()
        long finishTime = System.currentTimeMillis()
        long runTime = finishTime - startTime
        withClient { client ->
            client.timing(key, runTime.toInteger(), sampleRate)
        }
        log.debug("End timer : ${key} : ${runTime}ms")
        return result
    }

    def methodMissing(String name, args) {
        log.debug "methodMissing $name"
        withClient { client ->
            client.invokeMethod(name, args)
        }
    }

}
