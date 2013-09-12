package grails.plugin.statsd

class StatsdTimingService {

    static transactional = false

    public long currentTimeMillis() {
        System.currentTimeMillis()
    }

}
