package grails.plugin.statsd.ast

import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.FieldNode

import static org.springframework.asm.Opcodes.ACC_PRIVATE
import org.codehaus.groovy.ast.PropertyNode

import static org.springframework.asm.Opcodes.ACC_PUBLIC
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.SyntaxException
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.ast.ClassHelper
import grails.plugin.statsd.StatsdService

import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.stmt.BlockStatement

abstract class AbstractStatsdASTTransformation implements ASTTransformation {

    protected static final String HASH_CODE = '#'
    protected static final String GSTRING = '$'
    protected static final String STATSD_SERVICE = 'statsdService'

    protected boolean validateNodes(ASTNode[] astNodes, SourceUnit sourceUnit) {
        if(astNodes[0] == null || ! astNodes[0].class.isAssignableFrom(AnnotationNode)) {
            addError('Expected annotation node as first argument of statsd ast transformation', astNodes[0], sourceUnit)
            return false
        }
        if(astNodes[1] == null || ! astNodes[1].class.isAssignableFrom(MethodNode)) {
            addError('Statsd annotations can only be placed on methods', astNodes[1], sourceUnit)
            return false
        }
        MethodNode methodNode = (MethodNode) astNodes[1]
        if(! methodNode.getCode().class.isAssignableFrom(BlockStatement)) {
            addError('Statsd annotations can only be placed on methods containing block statements. This indicates a bug in the plugin.', astNodes[1], sourceUnit)
            return false
        }
        return true
    }

    /**
     * This method adds a new property to the class. Groovy automatically handles adding the getters and setters so you
     * don't have to create special methods for those.  This could be reused for other properties.
     * @param cNode Node to inject property onto.  Usually a ClassNode for the current class.
     * @param propertyName The name of the property to inject.
     * @param propertyType The object class of the property. (defaults to Object.class)
     * @param initialValue Initial value of the property. (defaults null)
     */
    private void addStatsdServiceProperty(ClassNode cNode, String propertyName, Class propertyType = java.lang.Object.class, Expression initialValue = null) {
        FieldNode field = new FieldNode(
                propertyName,
                ACC_PRIVATE,
                new ClassNode(propertyType),
                new ClassNode(cNode.class),
                initialValue
        )
        cNode.addProperty(new PropertyNode(field, ACC_PUBLIC, null, null))
    }

    /**
     * Determine if the user missed injecting the statsdService into the class with the method.
     * @param sourceUnit SourceUnit to detect and/or inject service into
     */
    protected void injectStatsdService(SourceUnit sourceUnit) {
        if(!((ClassNode) sourceUnit.AST.classes.toArray()[0]).properties?.any { it?.field?.name == STATSD_SERVICE }) {
            println "Adding statsdService to class ${sourceUnit.AST.classes[0].name}."
            if(!sourceUnit.AST.imports.any {it.className == ClassHelper.make(StatsdService).name}
                    && !sourceUnit.AST.starImports.any {it.packageName == "${ClassHelper.make(StatsdService).packageName}."}) {
                println "Adding namespace ${ClassHelper.make(StatsdService).packageName} to class ${sourceUnit.AST.classes[0].name}."
                sourceUnit.AST.addImport('StatsdService', ClassHelper.make(StatsdService))
            }
            addStatsdServiceProperty((ClassNode) sourceUnit.AST.classes.toArray()[0], STATSD_SERVICE)
        }
    }

    protected void addError(String msg, ASTNode node, SourceUnit source) {
        int line = node.lineNumber
        int col = node.columnNumber
        SyntaxException se = new SyntaxException("${msg}\n", line, col)
        SyntaxErrorMessage sem = new SyntaxErrorMessage(se, source)
        source.errorCollector.addErrorAndContinue(sem)
    }

    protected Expression buildKeyExpression(String key) {
        if(key.contains(HASH_CODE)) {
            def ast = new AstBuilder().buildFromString("""
                "${key.replace(HASH_CODE, GSTRING).toString()}"
           """)
           return ast[0].statements[0].expression
        } else {
           return new ConstantExpression(key)
        }
    }

}
