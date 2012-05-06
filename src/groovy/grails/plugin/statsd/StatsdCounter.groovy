package grails.plugin.statsd

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.lang.annotation.ElementType
import org.codehaus.groovy.transform.GroovyASTTransformationClass

@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.METHOD])
@GroovyASTTransformationClass(['grails.plugin.statsd.ast.CounterASTTransformation'])
public @interface StatsdCounter {
    String key() default '';
    int magnitude() default 1;
    double sampleRate() default 1.0d;
}