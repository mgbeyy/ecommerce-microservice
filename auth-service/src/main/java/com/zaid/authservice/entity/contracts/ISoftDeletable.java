package com.zaid.authservice.entity.contracts;

import java.time.LocalDateTime;

public interface ISoftDeletable {
    Boolean getIsDeleted();
    void setIsDeleted(Boolean isDeleted);

    String getDeletedBy();
    void setDeletedBy(String deletedBy);

    LocalDateTime getDeletedAt();
    void setDeletedAt(LocalDateTime deletedAt);
}