package br.com.yiatzz.coins.core.query.type;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.simple.SimpleQuery;

public class StringEqualsIgnoreCaseCondition<O, A extends String> extends SimpleQuery<O, A> {

    private final A value;

    public StringEqualsIgnoreCaseCondition(Attribute<O, A> attribute, A value) {
        super(attribute);
        this.value = value;
    }

    @Override
    protected boolean matchesSimpleAttribute(SimpleAttribute<O, A> attribute, O object, QueryOptions queryOptions) {
        return matchesValue(attribute.getValue(object, queryOptions), queryOptions);
    }

    @Override
    protected boolean matchesNonSimpleAttribute(Attribute<O, A> attribute, O object, QueryOptions queryOptions) {
        for (A attributeValue : attribute.getValues(object, queryOptions)) {
            if (matchesValue(attributeValue, queryOptions)) {
                return true;
            }
        }

        return false;
    }

    public boolean matchesValue(A aValue, QueryOptions options) {
        return value.equalsIgnoreCase(aValue);
    }

    @Override
    protected int calcHashCode() {
        int result = attribute.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}