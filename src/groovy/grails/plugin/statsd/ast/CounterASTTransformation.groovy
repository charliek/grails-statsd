package grails.plugin.statsd.ast

import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.stmt.BlockStatement

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class CounterASTTransformation extends AbstractStatsdASTTransformation {

    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        if(! validateNodes(astNodes, sourceUnit)) {
            return
        }
        try {
            injectStatsdService(sourceUnit)

            AnnotationNode annotationNode = (AnnotationNode) astNodes[0]
            MethodNode methodNode = (MethodNode) astNodes[1]
            addCountStatement(annotationNode, methodNode, sourceUnit)
        } catch (Exception e) {
            addError("Error during Memoize AST Transformation: ${e}", astNodes[0], sourceUnit)
            throw e
        }
    }

    protected boolean addCountStatement(AnnotationNode annotationNode, MethodNode methodNode, SourceUnit sourceUnit) {
        String key = annotationNode.getMember('key')?.text
        int magnitude = annotationNode.getMember('magnitude')?.value ?: 1
        double sampleRate = annotationNode.getMember('sampleRate')?.value ?: 1.0

        if(key == null) {
            addError('Key is required in StatsCounter annotation', annotationNode, sourceUnit)
            return false
        }

        // This cast is currently enforced in the 'validateNodes' method
        BlockStatement bodyStatement = (BlockStatement) methodNode.code
        List<Statement> existingStatements = bodyStatement.statements
        existingStatements.add(0, createCountAst(key, magnitude, sampleRate))
        return true
    }

    private Statement createCountAst(String key, int magnitude, double sampleRate) {
        return new ExpressionStatement(
            new MethodCallExpression(
                new VariableExpression(STATSD_SERVICE),
                new ConstantExpression('increment'),
                new ArgumentListExpression(
                    buildKeyExpression(key),
                    new ConstantExpression(magnitude),
                    new ConstantExpression(sampleRate)
                )
            )
        )
    }

}
