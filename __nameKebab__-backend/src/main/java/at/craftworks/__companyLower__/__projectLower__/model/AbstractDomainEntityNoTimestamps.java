/*
 * Copyright (c) 2014-2019 craftworks GmbH - All Rights Reserved
 * Unauthorized copying of this file, via any medium is
 * strictly prohibited. Proprietary and confidential
 * Created by Lukas Haselsteiner on 2019-10-16
 */
package at.craftworks.__companyLower__.__projectLower__.model;

import org.hibernate.proxy.HibernateProxyHelper;

import javax.persistence.MappedSuperclass;
import java.util.Objects;

@MappedSuperclass
public abstract class AbstractDomainEntityNoTimestamps<ID> {
    // TODO move UUID_GENERATOR_STRATEGY into template
    //static final String GENERATOR_NAME = "uuidGen";
    //static final String UUID_GENERATOR_STRATEGY = "TODO";

    public abstract ID getId();

    // suppress IDEA warning, we do check class, but unwrap it from HibernateProxy
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        // either this or o could be wrapped in HibernateProxy, so we are comparing the actual class
        if (HibernateProxyHelper.getClassWithoutInitializingProxy(this) !=
                HibernateProxyHelper.getClassWithoutInitializingProxy(o)) {
            return false;
        }

        AbstractDomainEntityNoTimestamps<?> otherEntity = (AbstractDomainEntityNoTimestamps<?>) o;
        if (this.getId() == null && otherEntity.getId() == null) {
            // both IDs null, i.e. both Entities not in DB
            // => compare references for equality
            return this == otherEntity;
        }
        return Objects.equals(this.getId(), otherEntity.getId());
    }

    @Override
    public int hashCode() {
        if (this.getId() != null) {
            return this.getId().hashCode();
        }
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "[" + getClass().getName() + " ID=" + getId() + "]";
    }
}
