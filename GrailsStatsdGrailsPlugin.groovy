import grails.plugin.statsd.StatsdPoolFactory
import org.apache.commons.pool.impl.GenericObjectPool

class GrailsStatsdGrailsPlugin {
    // the plugin version
    def version = "0.2"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def title = "Grails Statsd Plugin"
    def author = "Charlie Knudsen"
    def authorEmail = "charlie.knudsen@gmail.com"
    def description = '''\
The statsd plugin provides integration with the statsd server and can be used to publish application metrics and activity.
'''

    def license = "APACHE"

//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/charliek/grails-statsd" ]
    def documentation = "https://github.com/charliek/grails-statsd"

    def doWithWebDescriptor = { xml ->
        // Additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {

        def statsdConfigMap = application.config.grails.statsd ?: [:]
        statsdPoolConfig(GenericObjectPool.Config) {
            // used to set arbitrary config values without calling all of them out here or requiring any of them
            // any property that can be set on GenericObjectPool.Config can be set here
            statsdConfigMap.poolConfig.each { key, value ->
                delegate.setProperty(key, value)
            }
        }

        def host = statsdConfigMap.host ?: 'localhost'
        def port = statsdConfigMap.port ?: 8125

        log.info("Setting up statsd for ${host}:${port}")
        println "Setting up statsd for ${host}:${port}"

        statsdPoolFactory(StatsdPoolFactory, host, port)
        statsdPool(GenericObjectPool, ref('statsdPoolFactory'), ref('statsdPoolConfig')) {
            //bean.destroyMethod = 'close'
        }

    }

    def doWithDynamicMethods = { ctx ->
        // Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // Implement code that is executed when the application shuts down (optional)
    }
}
