import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class T1 {

	private static List<String> rst = new ArrayList<String>();
	
	private static void add(String item) {
		System.out.println(item);
		rst.add(item);
	}
	public static void aa(String str, int num, List<List<String>> list) {
		LinkedList<List<String>> link = new LinkedList<List<String>>(list);
		while(link.size() > 0) {
			List<String> remove = link.remove(0);
			for(int j=0; j<remove.size(); j++) {
				String item = str+remove.get(j);
				if(num == 1) {
					add(item);
				}else {
					aa(item,num-1,link);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> l1 = new ArrayList<String>();
		l1.add("a1");
		l1.add("a2");
		List<String> l2 = new ArrayList<String>();
		l2.add("b1");
		l2.add("b2");
		List<String> l3 = new ArrayList<String>();
		l3.add("c1");
		l3.add("c2");
		list.add(l1);
		list.add(l2);
		list.add(l3);
		List<String> l4 = new ArrayList<String>();
		l4.add("d1");
		l4.add("d2");
		List<String> l5 = new ArrayList<String>();
		l5.add("e1");
		l5.add("e2");
		List<String> l6 = new ArrayList<String>();
		l6.add("f1");
		list.add(l4);
		list.add(l5);
		list.add(l6);
		aa("", 5, list);
		System.out.println(rst.size());
	}
}
