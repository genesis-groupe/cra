import java.math.BigDecimal;
import java.util.*;

/**
 * @author f.patin
 */
public class Scrapbook {

	public static void main(String[] args) {

		System.out.println("true || false=" + (true));
		System.out.println("true && false=" + (false));
		System.out.println("true || true=" + true);
		System.out.println("true && true=" + (true));
		System.out.println("false || false=" + (false));
		System.out.println("false && false=" + (false));
		List<String> list = new ArrayList<>();
		list.add("un");
		list.add("deux");
		list.add("trois");
		list.add("un");
		System.out.println("List");
		for (String s : list) {
			System.out.println("s = " + s);
		}
		Set<String> set = new HashSet<>(list);
		System.out.println("Set");
		for (String s : set) {
			System.out.println("s = " + s);
		}
		System.out.println("After");
		for (String s : list) {
			System.out.println("s = " + s);
		}

		System.out.println("5%2:" + 5 % 2);
		System.out.println("4%2:" + 4 % 2);
		System.out.println("1%2:" + 1 % 2);

		BigDecimal bd = new BigDecimal("10.1");
		System.out.println(String.format("%.2f", bd));

		Map<String, StringBuilder> m = new HashMap<>();

	}
}