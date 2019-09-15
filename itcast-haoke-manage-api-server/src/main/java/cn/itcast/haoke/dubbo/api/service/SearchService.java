package cn.itcast.haoke.dubbo.api.service;

import cn.itcast.haoke.dubbo.api.vo.HouseData;
import cn.itcast.haoke.dubbo.api.vo.SearchResult;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    public static final Integer ROWS = 10;

    public SearchResult search(String keyWord, Integer page) {
        PageRequest pageRequest = PageRequest.of(page - 1, ROWS); //设置分页参数

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyWord, "title", "title.pinyin"
                ).operator(Operator.AND)) // match查询
                .withPageable(pageRequest)
                .withHighlightFields(new HighlightBuilder.Field("title")) // 设置高亮
                .build();

        AggregatedPage<HouseData> housePage =
                this.elasticsearchTemplate.queryForPage(searchQuery,
                        HouseData.class, new SearchResultMapper() {
                            @Override
                            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {

                                if (response.getHits().totalHits == 0) {
                                    return new AggregatedPageImpl<>(Collections.emptyList(), pageable, 0L);
                                }

                                List<T> list = new ArrayList<>();
                                for (SearchHit searchHit : response.getHits().getHits()) {
                                    T obj = (T) ReflectUtils.newInstance(clazz);

                                    try {
                                        FieldUtils.writeField(obj, "id", searchHit.getId(), true);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }

                                    // 非高亮字段的数据写入
                                    for (Map.Entry<String, Object> entry : searchHit.getSourceAsMap().entrySet()) {

                                        Field field = FieldUtils.getField(clazz, entry.getKey(), true);
                                        if (null == field) {
                                            continue;
                                        }

                                        try {
                                            FieldUtils.writeField(obj, entry.getKey(), entry.getValue(), true);
                                        } catch (IllegalAccessException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    for (Map.Entry<String, HighlightField> entry : searchHit.getHighlightFields().entrySet()) {
                                        StringBuilder sb = new StringBuilder();
                                        Text[] fragments = entry.getValue().getFragments();
                                        for (Text fragment : fragments) {
                                            sb.append(fragment.toString());
                                        }

                                        // 写入高亮的内容
                                        try {
                                            FieldUtils.writeField(obj, entry.getKey(), sb.toString(), true);
                                        } catch (IllegalAccessException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    list.add(obj);
                                }

                                return new AggregatedPageImpl<>(list, pageable, response.getHits().totalHits);
                            }
                        });

        return new SearchResult(housePage.getTotalPages(), housePage.getContent(), null);

    }


}
