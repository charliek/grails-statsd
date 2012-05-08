package grails.plugin.statsd

import grails.plugin.statsd.StatsdTimer
import grails.plugin.statsd.StatsdCounter

class TestController {

    def yourBasicService
    def statsdService

    def x = 'BLAH'

    def index() {
        statsdService.increment('homepage.visit')
        render 'hello'
    }

    def test2() {
        statsdService.withTimer('homepage.test2') {
            yourBasicService.methodWithTimerAndDefaultsProxy()
        }
        render 'annotation test'
    }

    @StatsdTimer(key='#{x}.method.timer')
    private void testMethod() {
        println 'method called2'
    }

    @StatsdCounter(key='#{x}.method.counter1')
    def test3() {
        render 'called annotation maybe?'
    }

    @StatsdCounter(key='test.method.counter2', magnitude=4)
    def test4() {
        testMethod()
        render 'called annotation maybe?'
    }

    @StatsdCounter(key='test.method.counter3', magnitude=6, sampleRate=4.4d)
    def test5() {
        testMethod()
        render 'called annotation maybe?'
    }

    @StatsdCounter(key='#{x}.test34')
    def testMethod2(int x){
        return x + 1
    }


    def test6() {
        testMethod2(42)
        render 'test'
    }

}
