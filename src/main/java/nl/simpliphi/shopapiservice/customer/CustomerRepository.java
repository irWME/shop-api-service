package nl.simpliphi.shopapiservice.customer;

import nl.simpliphi.shopapiservice.AbstractRepository;
import nl.simpliphi.shopprojections.customer.CustomerDto;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CustomerRepository extends AbstractRepository<CustomerDto> {

    private static final String DELETED = "deleted";

    @Autowired
    public CustomerRepository(ElasticsearchRestTemplate elasticsearchRestTemplate) {
        super(elasticsearchRestTemplate);
    }

    @Override
    public BoolQueryBuilder buildQuery(MultiValueMap<String, String> params) {
        BoolQueryBuilder query = new BoolQueryBuilder();

        String terms = params.getFirst("terms");
        if (StringUtils.isNotBlank(terms)) {
            query.must(QueryBuilders
                            .multiMatchQuery(terms, "firstName", "lastName")
                            .operator(Operator.AND)
//          .type(MatchQuery.Type.PHRASE_PREFIX)
            );
        }

        return query;
    }

    @Override
    public BoolQueryBuilder buildFilter(MultiValueMap<String, String> params, String facet) {
        BoolQueryBuilder query = new BoolQueryBuilder();

        List<String> firstNames = params.get("firstName");
        if (CollectionUtils.isNotEmpty(firstNames)) {
            query.must(QueryBuilders.termsQuery("firstName.keyword", firstNames));
        }

        List<String> lastNames = params.get("lastName");
        if (CollectionUtils.isNotEmpty(lastNames)) {
            query.must(QueryBuilders.termsQuery("lastName.keyword", lastNames));
        }

        List<String> values = params.get(DELETED);
        if (CollectionUtils.isNotEmpty(values) && !StringUtils.equals(DELETED, facet)) {
            List<Boolean> booleans = values.stream()
                    .map(BooleanUtils::toBoolean)
                    .collect(Collectors.toList());
            query.must(QueryBuilders.termsQuery(DELETED, booleans));
        }

        return query;
    }

}
