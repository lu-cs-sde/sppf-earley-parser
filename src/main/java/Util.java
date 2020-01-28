import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;

public class Util {
	public static void dumpParseResult(String dotFileName, SPPFNode node, Grammar grammar) {
		try {
			PrintStream out = new PrintStream(new File(dotFileName));
			DotVisitor visitor = new DotVisitor(out, grammar);
			visitor.prologue();
			node.accept(visitor);
			visitor.epilogue();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}

class DotVisitor implements SPPFNodeVisitor {
	private PrintStream ps;
	private int nodeID = 0;
	private HashMap<SPPFNode, Integer> visitedNodes = new HashMap<>();
	private HashMap<SPPFNode.FamilyNode, Integer> visitedFamilies = new HashMap<>();
	private Grammar info;

	public DotVisitor(PrintStream ps, Grammar info) {
		this.info = info;
		this.ps = ps;
	}

	public void prologue() {
		ps.println("digraph G {");
	}

	public void epilogue() {
		ps.println("}");
	}

	@Override
	public void visit(SPPFNode.FamilyNode f) {
		if (visitedFamilies.containsKey(f))
			return;

		int currentID = nodeID++;
		visitedFamilies.put(f, currentID);

		if (f.child1 == null) {
			// epsilon
			ps.print(currentID + " [label=\u03b5];\n");
		} else {
			ps.print(currentID + " [shape=circle];\n");

			f.child1.accept(this);
			if (f.child2 != null) {
				f.child2.accept(this);
			}
		}

		if (f.child1 != null)
			ps.print(currentID + " -> " + visitedNodes.get(f.child1) + ";\n");
		if (f.child2 != null)
			ps.print(currentID + " -> " + visitedNodes.get(f.child2) + ";\n");
	}

	@Override
	public void visit(SPPFNode n) {
		if (visitedNodes.containsKey(n))
			return;

		int currentID = nodeID++;
		visitedNodes.put(n, currentID);

		if (n.getLabel() instanceof ItemLabel) {
			ps.print(currentID + " [shape=box,color=red,label=\"" + n.prettyPrint(info) + "\"];\n");
		} else {
			ps.print(currentID + " [shape=box,label=\"" + n.prettyPrint(info) + "\"];\n");
		}

		for (SPPFNode.FamilyNode f : n.children) {
			f.accept(this);

			ps.print(currentID + " -> " + visitedFamilies.get(f) + ";\n");
		}
	}
}
