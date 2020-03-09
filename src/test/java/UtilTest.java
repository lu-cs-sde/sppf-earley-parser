import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import se.lth.sep.*;

import org.junit.Test;

public class UtilTest {
	@Test public void test1() {
		LinkedList<Integer> l1 = new LinkedList<>(Arrays.asList(1, 2, 3));
		LinkedList<Integer> l2 = new LinkedList<>(Arrays.asList(4, 5, 6));
		LinkedList<Integer> l3 = new LinkedList<>(Arrays.asList(7, 8));

		LinkedList<LinkedList<Integer>> ll = new LinkedList<>(Arrays.asList(l1, l2, l3));
		LinkedList<LinkedList<Integer>> r = Util.<Integer>product(ll.iterator());

		assertEquals(18, r.size());

		// for (LinkedList<Integer> li : r) {
		// 	for (int i : li) {
		// 		System.out.print(i + ", ");
		// 	}
		// 	System.out.println();
		// }
	}
}
