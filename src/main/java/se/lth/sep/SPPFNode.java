package se.lth.sep;

import java.util.HashSet;

public class SPPFNode {
	static class FamilyNode {
		private SPPFNode[] child;

		FamilyNode(SPPFNode child0, SPPFNode child1) {
			assert child0 != null || child1 == null;
			child = new SPPFNode[2];
			child[0] = child0;
			child[1] = child1;
		}

		public SPPFNode getChild(int i) {
			return child[i];
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((child[0] == null) ? 0 : child[0].hashCode());
			result = prime * result + ((child[1] == null) ? 0 : child[1].hashCode());
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
			if (child[0] == null) {
				if (other.child[0] != null)
					return false;
			} else if (!child[0].equals(other.child[0]))
				return false;
			if (child[1] == null) {
				if (other.child[1] != null)
					return false;
			} else if (!child[1].equals(other.child[1]))
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

	public void addChildren(SPPFNode child0, SPPFNode child1) {
		children.add(new FamilyNode(child0, child1));
	}

	public void addEpsilon() {
		children.add(new FamilyNode(null, null));
	}

	public String prettyPrint(Grammar info) {
		return label.prettyPrint(info);
	}

	public void accept(SPPFNodeVisitor visitor) {
		visitor.visit(this);
	}

	public NodeLabel getLabel() {
		return label;
	}
}
