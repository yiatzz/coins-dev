package br.com.yiatzz.coins.core.query;

import br.com.yiatzz.coins.core.query.type.StringEqualsIgnoreCaseCondition;
import com.googlecode.cqengine.attribute.Attribute;

public class CustomQueryFactory {

    public static <O, A extends String> StringEqualsIgnoreCaseCondition<O, A> equalsIgnoreCase(Attribute<O, A> attribute, A attributeValue) {
        return new StringEqualsIgnoreCaseCondition<>(attribute, attributeValue);
    }
}