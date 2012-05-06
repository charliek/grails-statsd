package grails.plugin.statsd

import java.lang.annotation.Retention
import java.lang.annotation.Target
import org.codehaus.groovy.transform.GroovyASTTransformationClass
import java.lang.annotation.ElementType
import java.lang.annotation.RetentionPolicy

@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.METHOD])
@GroovyASTTransformationClass(['grails.plugin.statsd.ast.TimerASTTransformation'])
public @interface StatsdTimer {
    String key() default '';
    double sampleRate() default 1.0d;
}
