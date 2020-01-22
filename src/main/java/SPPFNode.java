import java.util.ArrayList;
import java.util.List;

public class SPPFNode {
	private List<SPPFNode> children = new ArrayList<SPPFNode>();
	private NodeLabel label;

	public SPPFNode() {
		label = null;
	}

	public SPPFNode(NodeLabel label) {
		this.label = label;
	}
}
