grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsCentral()
        //mavenCentral()
        //mavenLocal()
        //mavenRepo "http://repository.codehaus.org"
    }
    dependencies {
        // runtime 'mysql:mysql-connector-java:5.1.5'
    }

    plugins {
        build(":tomcat:$grailsVersion", ":release:1.0.0") {
            export = false
        }
    }
}
