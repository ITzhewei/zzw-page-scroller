package zzw;

import static java.util.function.Function.identity;

import java.util.List;
import java.util.stream.Collectors;

import zzw.pagescroller.impl.PageScroller;

/**
 * @author zhangzhewei
 * Created on 2019-06-06
 */
public class PageScrollerTest {

    public static void main(String[] args) {

        PageScroller<Integer, Integer> pageScroller = PageScroller.<Integer, Integer> newBuilder() //
                .start(0) //
                .indexFunction(identity()) //
                .bufferSize(3) //
                .build((c, l) -> new UserDAO().getByCursor(c, l));

        List<Integer> collect = pageScroller.stream()
                .filter(id -> id % 11 == 0)
                .limit(1)
                .collect(Collectors.toList());

        System.out.println(collect);
    }

}
