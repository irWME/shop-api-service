package nl.simpliphi.shopapiservice;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public abstract class AbstractRepository<T> {
  private final SearchHits<T> emptySearchHits = new SearchHitsImpl<>(0, TotalHitsRelation.EQUAL_TO, 0f, null, Collections.emptyList(), null);
  private final ElasticsearchRestTemplate elasticsearchRestTemplate;
  private final Class<T> entityClazz;

  public AbstractRepository(ElasticsearchRestTemplate elasticsearchRestTemplate) {
    this.elasticsearchRestTemplate = elasticsearchRestTemplate;
    this.entityClazz = (Class) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
  }

  public void save(T entity) {
    elasticsearchRestTemplate.save(entity);
  }

  public void save(List<T> list) {
    elasticsearchRestTemplate.save(list);
  }

  public void delete(String id) {
    elasticsearchRestTemplate.delete(id, entityClazz);
  }

  public void delete(List<String> ids) {
    Query query = new NativeSearchQueryBuilder()
        .withQuery(QueryBuilders.idsQuery()
            .addIds(ids.toArray(new String[0])))
        .build();
    elasticsearchRestTemplate.delete(query, entityClazz);
  }

  public T getById(String id) {
    if (!elasticsearchRestTemplate.indexOps(entityClazz).exists()) {
      return null;
    }
    return elasticsearchRestTemplate.get(id, entityClazz);
  }

  public void searchForStream(MultiValueMap<String, String> params, Consumer<List<T>> function) {
    if (!elasticsearchRestTemplate.indexOps(entityClazz).exists()) {
      return;
    }

    BoolQueryBuilder query = buildQuery(params);
    BoolQueryBuilder filter = buildFilter(params);

    Query q = new NativeSearchQueryBuilder()
        .withQuery(query)
        .withFilter(filter)
        .withPageable(PageRequest.of(0, 1000))
        .build();

    SearchHitsIterator<T> stream = elasticsearchRestTemplate.searchForStream(q, entityClazz);
    List<T> buffer = new ArrayList<>();

    while (stream.hasNext()) {
      buffer.add(stream.next().getContent());
      if (buffer.size() % 1000 == 0) {
        function.accept(buffer);
        buffer = new ArrayList<>(); // clear buffer
      }
    }
    if (!buffer.isEmpty()) {
      function.accept(buffer);
    }
    stream.close();
  }

  public SearchHits<T> search(MultiValueMap<String, String> params, Pageable pageable) {
    if (!elasticsearchRestTemplate.indexOps(entityClazz).exists()) {
      return emptySearchHits;
    }

    Query query = buildSearchQuery(params, pageable);
    return elasticsearchRestTemplate.search(query, entityClazz);
  }

  public abstract BoolQueryBuilder buildQuery(MultiValueMap<String, String> params);

  public abstract BoolQueryBuilder buildFilter(MultiValueMap<String, String> params, String facet);

  public BoolQueryBuilder buildFilter(MultiValueMap<String, String> params) {
    return buildFilter(params, null);
  }

  public NativeSearchQuery buildSearchQuery(MultiValueMap<String, String> params, Pageable pageable) {
    BoolQueryBuilder query = buildQuery(params);
    BoolQueryBuilder filter = buildFilter(params);

    return new NativeSearchQueryBuilder()
        .withQuery(query)
        .withFilter(filter)
        .withPageable(pageable)
        .build();
  }
}
