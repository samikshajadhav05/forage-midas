package com.jpmc.midascore.repository;

import com.jpmc.midascore.entity.UserRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
// This interface should be empty. It inherits all necessary methods
// (like .save() and .findById()) from JpaRepository.
public interface UserRepository extends JpaRepository<UserRecord, Long> {
}
