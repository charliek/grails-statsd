package grails.plugin.statsd.ast

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit

import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression

import org.codehaus.groovy.ast.MethodNode

import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.classgen.VariableScopeVisitor
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.stmt.ExpressionStatement

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class TimerASTTransformation extends AbstractStatsdASTTransformation {

    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        if(! validateNodes(astNodes, sourceUnit)) {
            return
        }
        try {
            injectStatsdService(sourceUnit)
            if(! addTimingStatements((AnnotationNode) astNodes[0], (MethodNode) astNodes[1], sourceUnit)) {
                return
            }
            visitVariableScopes(sourceUnit)
        } catch (Exception e) {
            addError("Error during Memoize AST Transformation: ${e}", astNodes[0], sourceUnit)
            throw e
        }
    }

    /**
     * Fix the variable scopes for closures.  Without this closures will be missing the input params being passed from the parent scope.
     * @param sourceUnit The SourceUnit to visit and add the variable scopes.
     */
    private void visitVariableScopes(SourceUnit sourceUnit) {
        VariableScopeVisitor scopeVisitor = new VariableScopeVisitor(sourceUnit);
        sourceUnit.AST.classes.each {
            scopeVisitor.visitClass(it)
        }
    }

    /**
     * Create the statements for the memoized method, clear the node and then readd the memoized code back to the method.
     * @param methodNode The MethodNode we will be clearing and replacing with the redisService.memoize[?] method call with.
     * @param memoizeProperties The map of properties to use for the service invocation
     */
    private boolean addTimingStatements(AnnotationNode annotationNode, MethodNode methodNode, SourceUnit sourceUnit) {

        String key = annotationNode.getMember('key')?.text
        double sampleRate = annotationNode.getMember('sampleRate')?.value ?: 1.0

        if(key == null) {
            addError('Key is required in StatsTimer annotation', annotationNode, sourceUnit)
            return false
        }

        def stmt = timingMethod(key, sampleRate, methodNode)

        // This cast is currently enforced in the 'validateNodes' method
        BlockStatement bodyStatement = (BlockStatement) methodNode.code
        bodyStatement.statements.clear()
        bodyStatement.statements.addAll(stmt)
        return true
    }

    protected List<Statement> timingMethod(String key, double sampleRate, MethodNode methodNode) {
        BlockStatement body = new BlockStatement()
        addStatsdServiceTimerInvocation(key, sampleRate, body, methodNode)
        body.statements
    }

    protected void addStatsdServiceTimerInvocation(String key, double sampleRate, BlockStatement body, MethodNode methodNode) {
        ArgumentListExpression argumentListExpression = new ArgumentListExpression(
                buildKeyExpression(key),
                new ConstantExpression(sampleRate)
        )
        argumentListExpression.addExpression(makeClosureExpression(methodNode))

        MethodCallExpression methodCallExpression = new MethodCallExpression(
                new VariableExpression(STATSD_SERVICE),
                'withTimer',
                argumentListExpression
        )

        Statement methodStatement
        if (methodNode.returnType.toString() == 'void') {
            // If we have a void return type and use a return statement things will blow up on compile
            methodStatement = new ExpressionStatement(methodCallExpression)
        } else {
            methodStatement = new ReturnStatement(methodCallExpression)
        }
        body.addStatement(
                methodStatement
        )
    }

    protected ClosureExpression makeClosureExpression(MethodNode methodNode) {
        ClosureExpression closureExpression = new ClosureExpression(
                [] as Parameter[],
                new BlockStatement(methodNode.code.statements as Statement[], new VariableScope())
        )
        closureExpression.variableScope = methodNode.variableScope.copy()
        closureExpression
    }

}
