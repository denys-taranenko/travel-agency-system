package com.epam.agency.repository;

import com.epam.agency.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByUsername(String username);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, UUID id);

    boolean existsByEmailAndIdNot(String email, UUID id);

    Optional<User> findUserByUsername(String username);

    Optional<User> findUserByEmail(String email);

    @Query("SELECT u FROM User u WHERE " +
            "(:accountStatus IS NULL OR u.accountStatus = :accountStatus) AND " +
            "(COALESCE(:search, '') = '' OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))) ")
    Page<User> findAllUsers(@Param("accountStatus") Boolean accountStatus,
                            @Param("search") String search,
                            Pageable pageable);
}
