/*
 * Copyright (c) 2014-2022 craftworks GmbH - All Rights Reserved
 * Unauthorized copying of this file, via any medium is
 * strictly prohibited. Proprietary and confidential
 * Created by Lukas Haselsteiner on 2021-09-06
 */
package at.craftworks.lukas.test.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.math.BigDecimal;

/**
 * A Matcher that checks if two {@link Number} objects are "equal"<br>
 * The two objects are converted to {@link BigDecimal} via a conversion to the string representation, and then
 * {@link BigDecimal#compareTo(BigDecimal)} is used to determine if they are equal
 */
public class NumberMatcher<T extends Number> extends BaseMatcher<T> {
    private final BigDecimal expected;

    public NumberMatcher(T num) {
        if (num instanceof BigDecimal bd) {
            this.expected = bd;
        } else if (num != null) {
            this.expected = new BigDecimal(num.toString());
        } else {
            this.expected = null;
        }
    }

    @Override
    public boolean matches(Object actual) {
        if (actual instanceof Number) {
            var bigDecimalActual = new BigDecimal(actual.toString());
            return bigDecimalActual.compareTo(expected) == 0;
        }
        return actual == expected;
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(expected);
    }
}