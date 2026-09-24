package com.aurodining.service;

import com.aurodining.entity.AddressBook;
import java.util.List;

public interface AddressBookService {

    AddressBook save(AddressBook addressBook);

    List<AddressBook> list(AddressBook addressBook);

    AddressBook getById(Long id);

    void update(AddressBook addressBook);

    void delete(Long id);

    void setDefault(AddressBook addressBook);

    AddressBook getDefault(Long userId);
}