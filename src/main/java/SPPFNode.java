import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class SPPFNode {
	static class FamilyNode {
		SPPFNode child1;
		SPPFNode child2;

		FamilyNode(SPPFNode child1, SPPFNode child2) {
			this.child1 = child1;
			this.child2 = child2;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((child1 == null) ? 0 : child1.hashCode());
			result = prime * result + ((child2 == null) ? 0 : child2.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FamilyNode other = (FamilyNode) obj;
			if (child1 == null) {
				if (other.child1 != null)
					return false;
			} else if (!child1.equals(other.child1))
				return false;
			if (child2 == null) {
				if (other.child2 != null)
					return false;
			} else if (!child2.equals(other.child2))
				return false;
			return true;
		}

		public void accept(SPPFNodeVisitor visitor) {
			visitor.visit(this);
		}
	}

	HashSet<FamilyNode> children = new HashSet<>();
	private NodeLabel label;

	public SPPFNode() {
		label = null;
	}

	public SPPFNode(NodeLabel label) {
		this.label = label;
	}

	public void addChild(SPPFNode child) {
		children.add(new FamilyNode(child, null));
	}

	public void addChildren(SPPFNode child1, SPPFNode child2) {
		children.add(new FamilyNode(child1, child2));
	}

	public void addEpsilon() {
		children.add(new FamilyNode(null, null));
	}

	public String prettyPrint(PrettyPrintingInfo info) {
		return label.prettyPrint(info);
	}

	public void accept(SPPFNodeVisitor visitor) {
		visitor.visit(this);
	}
}

interface SPPFNodeVisitor {
	public void visit(SPPFNode.FamilyNode familyNode);
	public void visit(SPPFNode n);
}

class DotVisitor implements SPPFNodeVisitor {
	private PrintStream ps;
	private int nodeID = 0;
	private HashMap<SPPFNode, Integer> visitedNodes = new HashMap<>();
	private HashMap<SPPFNode.FamilyNode, Integer> visitedFamilies = new HashMap<>();
	private PrettyPrintingInfo info;

	public DotVisitor(PrintStream ps, PrettyPrintingInfo info) {
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

		ps.print(currentID + " [shape=box,label=\"" + n.prettyPrint(info) + "\"];\n");

		for (SPPFNode.FamilyNode f : n.children) {
			f.accept(this);

			ps.print(currentID + " -> " + visitedFamilies.get(f) + ";\n");
		}
	}
}
