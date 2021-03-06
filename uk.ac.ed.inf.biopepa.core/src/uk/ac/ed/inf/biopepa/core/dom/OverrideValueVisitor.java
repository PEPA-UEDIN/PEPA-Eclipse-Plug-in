package uk.ac.ed.inf.biopepa.core.dom;

import uk.ac.ed.inf.biopepa.core.BioPEPAException;
import uk.ac.ed.inf.biopepa.core.dom.VariableDeclaration.Kind;

/*
 * A visitor of the AST to override a particular value.
 * We make a note of whether or not at least one value was
 * changed/overriden (there should be no more than one).
 * This allows the caller to query whether any overriding was
 * actually performed and thus produce an error if not.
 */

public class OverrideValueVisitor implements ASTVisitor {

	private String nodeName;
	private String nodeValue;
	private boolean valueOverridden = false;
	public OverrideValueVisitor (String name, String value){
		this.nodeName = name;
		this.nodeValue = value;
		this.valueOverridden = false;
	}
	
	public boolean getValueOverridden (){
		return this.valueOverridden;
	}
	
	public boolean visit(NameSet nameSet) throws BioPEPAException {
		return false;
	}

	public boolean visit(Component component) throws BioPEPAException {
		String componentName = component.getName().getIdentifier();
		if (componentName.equals(nodeName)){
			NumberLiteral level = component.ast.newNumberLiteral();
			level.setToken(nodeValue);
			component.setLevel(level);
			this.valueOverridden = true;
		}
		return true;
	}

	public boolean visit(Cooperation cooperation) throws BioPEPAException {
		// For this we do not much care what the cooperation set is
		// since we will not find any overridable values there.
		cooperation.getLeftHandSide().accept(this);
		cooperation.getRightHandSide().accept(this);
		return true;
	}

	public boolean visit(ExpressionStatement statement) throws BioPEPAException {
		// This must be the system equation so we simply visit the
		// sub-expressions
		statement.getExpression().accept(this);
		return true;
	}

	public boolean visit(FunctionCall functionCall) throws BioPEPAException {
		return false;
	}

	public boolean visit(InfixExpression infixExpression) throws BioPEPAException {
		// For this we do not care what the operator is, if we have gotten this
		// far then we are in the main system equation and hence we are looking
		// for component initial concentrations to override
		// NOTE: if we make all of these visit methods return true if they
		// have actually made a substitution or false if the overriding name
		// was not found, then we 
		infixExpression.getLeftHandSide().accept(this);
		infixExpression.getRightHandSide().accept(this);
		return true;
	}

	public boolean visit(Model model) throws BioPEPAException {
		for (Statement s : model.statements()) {
			s.accept(this);
		}
		return true;
	}
	
	public boolean visit(Name name) throws BioPEPAException {
		return false;
	}

	public boolean visit(NumberLiteral numberLiteral) throws BioPEPAException {
		return false;
	}

	public boolean visit(PostfixExpression postfixExpression) throws BioPEPAException {
		return false;
	}

	public boolean visit(Prefix prefix) throws BioPEPAException {
		return false;
	}

	public boolean visit(PropertyInitialiser propertyInitialiser) throws BioPEPAException {
		return false;
	}

	public boolean visit(PropertyLiteral propertyLiteral) throws BioPEPAException {
		return false;
	}

	public boolean visit(VariableDeclaration variableDeclaration) throws BioPEPAException {
		String varName = variableDeclaration.getName().getIdentifier();
		Kind varKind = variableDeclaration.getKind();
		// We can only override variables, not component or function
		// definitions.
		if (varName.equals(nodeName)){
			if (varKind.equals(VariableDeclaration.Kind.VARIABLE)){			
				NumberLiteral value = variableDeclaration.ast.newNumberLiteral();
				value.setToken(nodeValue);
				variableDeclaration.setRightHandSide(value);
				this.valueOverridden = true;
			} else if (varKind.equals(VariableDeclaration.Kind.FUNCTION) &&
					nodeValue.equals("off")){
				NumberLiteral value = variableDeclaration.ast.newNumberLiteral();
				value.setToken("0");
				variableDeclaration.setRightHandSide(value);
				this.valueOverridden = true;
			}
		} else {
			// We may have a variable declaring a portion of the model
			// for example:
			// Module_1 ::= A[10] <*> B[100]
			// And hence we would like to search within it.
			variableDeclaration.getRightHandSide().accept(this);
		}
		return true;
	}

	public boolean visit(Transport transport) throws BioPEPAException {
		return false;
	}

	public boolean visit(SystemVariable variable) throws BioPEPAException {
		return false;
	}

}
