/*
 * Copyright (c) 2014-2022 craftworks GmbH - All Rights Reserved
 * Unauthorized copying of this file, via any medium is
 * strictly prohibited. Proprietary and confidential
 * Created by Lukas Haselsteiner on 2021-09-06
 */
package at.craftworks.__companyLower__.__projectLower__.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.math.BigDecimal;

/**
 * A Matcher that checks if two {@link Number} objects are "equal"<br>
 * The two objects are converted to {@link BigDecimal} via a conversion to the string representation, and then the comparision is done
 */
public class NumberCloseToMatcher<T extends Number> extends BaseMatcher<T> {
    private final BigDecimal expected;
    private final BigDecimal error;

    public NumberCloseToMatcher(T expected, Number error) {
        if (expected instanceof BigDecimal expectedBd && error instanceof BigDecimal errorBd) {
            this.expected = expectedBd;
            this.error = errorBd;
        } else if (expected != null) {
            this.expected = new BigDecimal(expected.toString());
            this.error = new BigDecimal(error.toString());
        } else {
            this.expected = null;
            this.error = null;
        }
    }

    @Override
    public boolean matches(Object actual) {
        if (actual instanceof Number) {
            var bigDecimalActual = new BigDecimal(actual.toString());
            return bigDecimalActual.subtract(expected).abs().compareTo(error) <= 0;
        }
        return actual == expected;
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(expected);
    }
}