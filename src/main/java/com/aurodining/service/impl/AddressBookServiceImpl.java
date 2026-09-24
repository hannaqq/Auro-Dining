package com.aurodining.service.impl;

import com.aurodining.entity.AddressBook;
import com.aurodining.repository.AddressBookRepository;
import com.aurodining.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {

    @Autowired
    private AddressBookRepository addressBookRepository;

    @Override
    public AddressBook save(AddressBook addressBook) {
        return addressBookRepository.save(addressBook);
    }

    @Override
    public List<AddressBook> list(AddressBook addressBook) {
        // Use the custom repository method to sort by update time
        return addressBookRepository.findByUserIdOrderByUpdateTimeDesc(addressBook.getUserId());
    }

    @Override
    public AddressBook getById(Long id) {
        // Return null if not found
        return addressBookRepository.findById(id).orElse(null);
    }

    @Override
    public void update(AddressBook addressBook) {
        addressBookRepository.save(addressBook);
    }

    @Override
    public void delete(Long id) {
        addressBookRepository.deleteById(id);
    }

    /**
     * Set Default Address Logic:
     * 1. Find all addresses for this user and set 'isDefault' to 0.
     * 2. Set the target address 'isDefault' to 1.
     */
    @Override
    @Transactional
    public void setDefault(AddressBook addressBook) {
        // 1. Get all addresses for the current user
        List<AddressBook> addressList = addressBookRepository.findByUserId(addressBook.getUserId());

        // 2. Set all to non-default (0)
        for (AddressBook addr : addressList) {
            addr.setIsDefault(0);
        }
        addressBookRepository.saveAll(addressList);

        // 3. Set the current address to default (1)
        // Retrieve the entity first to ensure we have the full object
        AddressBook target = addressBookRepository.findById(addressBook.getId()).orElse(null);
        if(target != null){
            target.setIsDefault(1);
            addressBookRepository.save(target);
        }
    }

    @Override
    public AddressBook getDefault(Long userId) {
        return addressBookRepository.findByUserIdAndIsDefault(userId, 1);
    }
}