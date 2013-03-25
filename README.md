[![Build Status](https://drone.io/github.com/charliek/grails-statsd/status.png)](https://drone.io/github.com/charliek/grails-statsd/latest)

Grails Statsd Plugin
=============

This project allows for easy publication of metrics to StatsD from a grails application.

What is StatsD
--------------
[StatsD][statsd_repo] is a NodeJS daemon that listens for messages on a UDP port. It parses the messages, extracts metrics data, and periodically flushes the data to graphite. While it is not that complex it allows for powerful visualization of metrics over time. For more information about StatsD see this [blog post][statsd_post] written by [Etsy][etsy] its creator.

Installation
------------

For now you will need to make a local plugin, but eventually it should be as easy as:

    grails install-plugin statsd


Out of the box, the plugin expects that StatsD is running on `localhost:8125`.  You can modify this (as well as any other pool config options) by adding a stanza like this to your `grails-app/conf/Config.groovy` file:

    grails {
        statsd {
            poolConfig {
                // apache pool specific tweaks here. 
                // See [the javadoc][genericobjectpool] for details.
            }
            port = 8125
            host = "localhost"
        }
    }

The poolConfig section will let you tweak any of the setter values made available by Apache Commons [GenericObjectPool][genericobjectpool].

Plugin Usage
------------

### StatsdService Bean ###

    def statsdService

The `statdService` bean is the only service exposed by this plugin that you should need, and it contains the methods that are supported by the StatsD server. Currently this includes counters and timers.

Counters are used to count the number of times something happens and report on it. This is done through the increment and decrement methods within the service. These methods take the metric to count as the first parameter, an optional magnitude as a second parameter, and an optional sample rate as a third parameter.

    def statsdService

    statsdService.increment('this.happened')
    statsdService.increment('this.happened twice', 2)
    statsdService.increment('this.happened tres times', 3, 0.8)

    statsdService.decrement('this.happened')
    statsdService.decrement('this.happened twice', 2)
    statsdService.decrement('this.happened tres times', 3, 0.8)

Timers are used to record how long something took. Generally you will probably want to use the closure method to call timers, but there is also functions to specify times directly if desired. Timers also take in a metric name, and can have a second sampling rate parameter.

    def allBooks = service.withTimer('dbquery.books') {
        Books.list()
    }

There is not very much code in the service so if you have questions please dig in.

### Statsd Annotations ###

In addition to the service you can also use annotations to push out metrics.

The following are available as annotations:

<table width="100%">
    <tr><td><b>Annotation</b></td><td><b>Description</b></td></tr>
    <tr><td>@StatsdCounter</td><td>Used to add to a counter when a method is called</td></tr>
    <tr><td>@StatsdTimer</td><td>Used to add a timer to the method that is called</td></tr>
</table>

[statsd_repo]: https://github.com/etsy/statsd
[statsd_post]: http://codeascraft.etsy.com/2011/02/15/measure-anything-measure-everything/
[etsy]: http://www.etsy.com/
[genericobjectpool]:http://commons.apache.org/pool/apidocs/org/apache/commons/pool/impl/GenericObjectPool.html