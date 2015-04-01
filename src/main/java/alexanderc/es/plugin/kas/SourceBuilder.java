package alexanderc.es.plugin.kas;

import alexanderc.es.plugin.kas.Exception.*;
import alexanderc.es.plugin.kas.Exception.Exception;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.source.FetchSourceContext;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AlexanderC on 4/1/15.
 */
public class SourceBuilder {

    public static final String KEY_FIELD = "_kas_key";

    protected SearchSourceBuilder sourceBuilder;
    protected FetchSourceContext sourceContext = new FetchSourceContext(null, KEY_FIELD);
    protected BaseFilterBuilder filterBuilder;
    protected BaseQueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
    protected List<TermsFilterBuilder> terms = new ArrayList<TermsFilterBuilder>();

    public SourceBuilder(SearchSourceBuilder sourceBuilder, String kasKey) {
        this.sourceBuilder = sourceBuilder;
        this.filterBuilder = this.getKasInternalsFilter(kasKey);
    }

    public SourceBuilder setQuery(String query) {
        return this.setQuery(QueryBuilders.queryString(query));
    }

    public SourceBuilder setQuery(BaseQueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
        return this;
    }

    protected OrFilterBuilder getKasInternalsFilter(String kasKey) {
        return FilterBuilders.orFilter(
                FilterBuilders.missingFilter(KEY_FIELD),
                FilterBuilders.termFilter(KEY_FIELD, ""),
                FilterBuilders.termFilter(KEY_FIELD, kasKey)
        );
    }

    public SourceBuilder addTerms(String terms) throws alexanderc.es.plugin.kas.Exception.Exception {
        terms = terms.replaceAll(",+", ",");
        String[] termsList = terms.split(",");

        return this.addTerms(termsList);
    }

    public SourceBuilder addTerms(String[] termsList) throws Exception {
        for(String termItem : termsList) {
            Integer delimiterPos = termItem.indexOf(':');

            if(-1 == delimiterPos) {
                throw new MissingTermDelimiterException("Missing term delimiter in " + termItem);
            }

            String termField = termItem.substring(0, delimiterPos);
            String termValue = termItem.substring(delimiterPos + 1);

            if(termField.equalsIgnoreCase(KEY_FIELD)) {
                throw new ForbiddenException("Searching for an internal term is forbidden");
            }

            if(0 == termValue.indexOf('(') && termValue.length() - 1 == termValue.lastIndexOf(')')) {
                termValue = termValue.substring(1, termValue.length() - 1);

                String[] termValuesVector = termValue.split("\\|");

                List<String> cleanTermsFilterVector = new ArrayList<String>();

                for(String termValueItem : termValuesVector) {
                    cleanTermsFilterVector.add(termValueItem.replaceAll("^\"(.+)\"$", "$1"));
                }

                this.terms.add(FilterBuilders.inFilter(
                        termField,
                        cleanTermsFilterVector.toArray(new String[cleanTermsFilterVector.size()])
                ));
            } else {
                termValue = termValue.replaceAll("^\"(.+)\"$", "$1");

                this.terms.add(FilterBuilders.termsFilter(termField, termValue));
            }
        }

        return this;
    }

    public SourceBuilder addSort(String sort) {
        sort = sort.replaceAll(",+", ",");
        String[] sortParts = sort.split(",");

        return this.addSortParts(sortParts);
    }

    public SourceBuilder addSortParts(String[] sortParts) {
        for(String sortField : sortParts) {
            SortOrder sortOrder = SortOrder.ASC;

            sortField = sortField.replaceAll("[^a-zA-Z0-9_\\-\\.]+", "");

            if(0 == sortField.indexOf('-')) {
                sortField = sortField.substring(1);
                sortOrder = SortOrder.DESC;
            }

            this.addSort(sortField, sortOrder);
        }

        return this;
    }

    public SourceBuilder addSort(String field, SortOrder order) {
        this.sourceBuilder.sort(SortBuilders.fieldSort(field).order(order));
        return this;
    }

    public SourceBuilder addFields(String fields) {
        fields = fields.replaceAll(",+", ",");
        String[] fieldsList = fields.split(",");

        return this.addFields(fieldsList);
    }

    public SourceBuilder addFields(String[] fieldsList) {
        this.sourceContext.includes(fieldsList);
        return this;
    }

    public SourceBuilder setFrom(Integer from) {
        this.sourceBuilder.from(from);
        return this;
    }

    public SourceBuilder setSize(Integer size) {
        this.sourceBuilder.size(size);
        return this;
    }

    protected SearchSourceBuilder build() {
        FilteredQueryBuilder filteredQuery = QueryBuilders.filteredQuery(
                this.queryBuilder,
                this.terms.isEmpty() ? this.filterBuilder : FilterBuilders.andFilter(
                        this.filterBuilder,
                        FilterBuilders.andFilter(
                                this.terms.toArray(new TermsFilterBuilder[this.terms.size()])
                        )
                )
        );

        this.sourceBuilder.fetchSource(this.sourceContext);
        this.sourceBuilder.query(filteredQuery);

        return this.sourceBuilder;
    }
}
