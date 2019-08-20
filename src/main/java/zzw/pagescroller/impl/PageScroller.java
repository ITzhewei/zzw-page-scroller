package zzw.pagescroller.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;

import zzw.pagescroller.GetByCursor;

/**
 * @author zhangzhewei
 */
public class PageScroller<Index, Entity> implements Iterable<List<Entity>> {

    private final GetByCursor<Index, Entity> getFunction;
    private final Index initIndex;
    private final int limit;
    private final Function<Entity, Index> indexFunction;
    private int maxNumberOfPages = Integer.MAX_VALUE;
    private static final int DEFAULT_LIMIT_SIZE = 30;

    public PageScroller(GetByCursor<Index, Entity> getFunction, Index initIndex, int limit,
            Function<Entity, Index> indexFunction) {
        this.getFunction = getFunction;
        this.initIndex = initIndex;
        this.limit = limit;
        this.indexFunction = indexFunction;
    }

    public static <Index, Entity> Builder<Index, Entity> newBuilder() {
        return new Builder<>();
    }

    private static <Index, Entity> List<Entity>
            fetchOnePageExcludeStart(GetByCursor<Index, Entity> dao, Index start, int limit) {
        return dao.getByCursor(start, limit);
    }

    @Nonnull
    @Override
    public Iterator<List<Entity>> iterator() {
        return new AbstractIterator<List<Entity>>() {

            private List<Entity> previousPage;
            private boolean firstTime = true;
            private int pageIndex = 0;

            @Override
            protected List<Entity> computeNext() {
                List<Entity> page;
                if (firstTime) {
                    firstTime = false;
                    page = getFunction.getByCursor(initIndex, limit);
                } else {
                    if (pageIndex >= maxNumberOfPages) {
                        page = Collections.emptyList();
                    } else if (previousPage.size() < limit) {
                        page = Collections.emptyList();
                    } else {
                        Index start = indexFunction
                                .apply(previousPage.get(previousPage.size() - 1));
                        page = fetchOnePageExcludeStart(getFunction, start, limit);
                    }
                }

                previousPage = page;
                pageIndex++;
                return page.isEmpty() ? endOfData() : page;
            }
        };
    }

    public void setMaxNumberOfPages(int maxNumberOfPages) {
        this.maxNumberOfPages = maxNumberOfPages;
    }

    public Stream<List<Entity>> streamList() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this.iterator(), 1296),
                false);
    }

    public Stream<Entity> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this.iterator(), 1296),
                false).flatMap(Collection::stream).collect(Collectors.toList()).stream();
    }

    public static class Builder<Index, Entity> {

        private GetByCursor<Index, Entity> getFunction;
        private Index initIndex;
        private int limit;
        private Function<Entity, Index> indexFunction;
        private int maxNumberOfPages = 0;

        public Builder() {
        }

        @CheckReturnValue
        public PageScroller.Builder<Index, Entity> bufferSize(int limit) {
            Preconditions.checkArgument(limit > 0);
            this.limit = limit;
            return this;
        }

        @CheckReturnValue
        public PageScroller.Builder<Index, Entity> indexFunction(Function<Entity, Index> function) {
            this.indexFunction = function;
            return this;
        }

        @CheckReturnValue
        public PageScroller.Builder<Index, Entity> start(Index init) {
            this.initIndex = init;
            return this;
        }

        @CheckReturnValue
        public PageScroller.Builder<Index, Entity> maxNumberOfPages(int maxNumberOfPages) {
            this.maxNumberOfPages = maxNumberOfPages;
            return this;
        }

        public PageScroller<Index, Entity> build(GetByCursor<Index, Entity> getFunction) {
            this.getFunction = getFunction;
            ensure();
            PageScroller<Index, Entity> pageScroller = new PageScroller<>(getFunction, initIndex,
                    limit, indexFunction);
            if (this.maxNumberOfPages > 0) {
                pageScroller.setMaxNumberOfPages(this.maxNumberOfPages);
            }
            return pageScroller;
        }

        private void ensure() {
            Preconditions.checkNotNull(this.getFunction);
            Preconditions.checkNotNull(this.indexFunction);
            if (this.limit == 0) {
                this.limit = DEFAULT_LIMIT_SIZE;
            }
        }

    }

}
