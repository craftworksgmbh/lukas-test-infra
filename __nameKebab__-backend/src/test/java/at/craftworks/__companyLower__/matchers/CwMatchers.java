/*
 * Copyright (c) 2014-2022 craftworks GmbH - All Rights Reserved
 * Unauthorized copying of this file, via any medium is
 * strictly prohibited. Proprietary and confidential
 * Created by Lukas Haselsteiner on 2021-09-06
 */
package at.craftworks.__companyLower__.__projectLower__.matchers;

import java.math.BigDecimal;

/**
 * sample usages:
 * <pre>
 *     .body("content[0].item.partVariant.currentCost", CwMatchers.sameNumber(new BigDecimal("340.0003")))
 *     .body("untrackedChanges.label", CwMatchers.closeToNumber(10))
 * </pre>
 */
public class CwMatchers {
    /***
     * Checks if expected and actual are the same number
     * @param value Value to Check
     * @return Matcher
     */
    public static <T extends Number> org.hamcrest.Matcher<? super BigDecimal> sameNumber(T value) {
        return new NumberMatcher<>(value);
    }

    /**
     * A matcher that matches if any kind of numerics are close to each other
     * @param operand expected value
     * @param error Acceptable error
     * @return Matcher object
     */
    public static <T extends Number> org.hamcrest.Matcher<T> closeToNumber(T operand, T error) {
        return new NumberCloseToMatcher<>(operand, error);
    }

    /**
     * A matcher that matches if any kind of numerics are close to each other, acceptable error is 0.005
     * @param operand expected value
     * @return Matcher object
     */
    public static <T extends Number> org.hamcrest.Matcher<T> closeToNumber(T operand) {
        return new NumberCloseToMatcher<>(operand, 0.005);
    }
}
