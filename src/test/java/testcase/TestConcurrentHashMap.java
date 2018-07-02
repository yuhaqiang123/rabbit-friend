package testcase;

import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yuhaiqiang on 2018/6/28.
 *
 * @description
 */
public class TestConcurrentHashMap {

    ConcurrentHashMap<String, String> map = new ConcurrentHashMap<String, String>();

    @Before
    public void setup() {
        Integer count = 100 * 10000;

        for (Integer i = 0; i < count; i++) {
            map.put(i.toString(), i.toString());
        }
    }

    @Test
    public void test() {
        Long start = System.currentTimeMillis();

        for (Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
            iterator.next();
            iterator.remove();
        }
        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    public void test2() {
        Long start = System.currentTimeMillis();
        List<String> list = new LinkedList<>();
        map.forEach((key, value) -> {
            list.add(key);
        });
        list.parallelStream().forEach((key) -> map.remove(key));
        System.out.println(System.currentTimeMillis() - start);
    }
}
