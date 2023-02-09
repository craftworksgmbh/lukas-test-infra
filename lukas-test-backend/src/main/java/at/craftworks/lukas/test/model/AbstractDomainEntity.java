/*
 * Copyright (c) 2014-2019 craftworks GmbH - All Rights Reserved
 * Unauthorized copying of this file, via any medium is
 * strictly prohibited. Proprietary and confidential
 * Created by Lukas Haselsteiner on 2019-05-23
 */
package at.craftworks.lukas.test.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@EntityListeners({AuditingEntityListener.class}) // http://www.baeldung.com/database-auditing-jpa
@MappedSuperclass
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public abstract class AbstractDomainEntity<ID> extends AbstractDomainEntityNoTimestamps<ID> {
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    @Getter
    @Setter
    protected LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "last_modified_at", nullable = false)
    @Getter
    @Setter
    protected LocalDateTime lastModifiedAt;

    @CreatedBy
    @Column(name = "created_by")
    @Getter
    @Setter
    protected String createdBy;

    @LastModifiedBy
    @Column(name = "last_modified_by")
    @Getter
    @Setter
    protected String lastModifiedBy;
}
