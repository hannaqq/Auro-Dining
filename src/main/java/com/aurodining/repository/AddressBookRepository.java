package com.aurodining.repository;

import com.aurodining.entity.AddressBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressBookRepository extends JpaRepository<AddressBook, Long> {

    List<AddressBook> findByUserIdOrderByUpdateTimeDesc(Long userId);

    List<AddressBook> findByUserId(Long userId);

    AddressBook findByUserIdAndIsDefault(Long userId, Integer isDefault);
}
