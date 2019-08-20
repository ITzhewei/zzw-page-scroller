package zzw;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UserDAO {

    public List<Integer> getByCursor(int cursor, int limit) {
        System.out.println("run once");
        return IntStream.rangeClosed(cursor, 1000).boxed().limit(limit).collect(Collectors.toList());
    }
}
