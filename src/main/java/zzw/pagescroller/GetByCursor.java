package zzw.pagescroller;

import java.util.List;

/**
 * @author zhangzhewei
 */
@FunctionalInterface
public interface GetByCursor<Index, Entity> {

    List<Entity> getByCursor(Index index, int limit);

}
