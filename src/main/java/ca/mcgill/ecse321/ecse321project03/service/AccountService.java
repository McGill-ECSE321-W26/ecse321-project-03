package ca.mcgill.ecse321.ecse321project03.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;

import ca.mcgill.ecse321.ecse321project03.dto.AccountCreationDto;
import ca.mcgill.ecse321.ecse321project03.repository.AccountRepository;
import jakarta.transaction.Transactional;
import ca.mcgill.ecse321.ecse321project03.model.Account;
import ca.mcgill.ecse321.ecse321project03.exception.OnlineFashionBoutiqueException;


// Tutorial 7
@Service
@Validated
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Transactional
    public Account createAccount(@Valid AccountCreationDto accountCreationDto) {
        
        Account account = new Account(accountCreationDto.getUsername(), accountCreationDto.getPassword(), accountCreationDto.getEmail(), accountCreationDto.getAddress(), 0);

        // Save the account to the database
        return accountRepository.save(account);
    }

    public Account findAccountById (int id) {
        Account accountFromDB = accountRepository.findById(id).orElse(null);
        if (accountFromDB == null) {
             throw new OnlineFashionBoutiqueException(HttpStatus.NOT_FOUND, "No person found with id " + id);
        }
        return accountFromDB;
    }
    
}
