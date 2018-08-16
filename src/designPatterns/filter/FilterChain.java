package designPatterns.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author he_guitang
 * @version [1.0 , 2018/5/21]
 * 过滤器管理器
 */
public class FilterChain implements Filter {
    private List<Filter> filters = new ArrayList<>();
    private int index = 0;

    FilterChain addFilter(Filter filter){
        this.filters.add(filter);
        return this;
    }

    @Override
    public void doFilter(Request request, Response response, FilterChain filterChain) {
        if (index == filters.size()) return;
        Filter f = filters.get(index);
        index ++;
        f.doFilter(request, response, filterChain);
    }
}


