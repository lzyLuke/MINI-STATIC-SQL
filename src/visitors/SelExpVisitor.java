package visitors;

import java.util.List;

import net.sf.jsqlparser.schema.Column;
import util.Helpers;
import util.Tuple;

/**
 * Select expression visitor which takes one child 
 * and one schema.
 *
 */
public class SelExpVisitor extends ConcreteExpVisitor {

	private Tuple tuple = null;
	private List<String> schema = null;
	

	public SelExpVisitor(List<String> schema) {
		this.schema = schema;
	}
	

	public void setTuple(Tuple tuple) {
		this.tuple = tuple;
	}
	

	@Override
	public void visit(Column arg0) {
		currNumericValue = Helpers.getAttrVal(tuple, arg0.toString(), schema);
	}
	
}
