package com.fsse2510.fsse2510_project_backend.controller;

import com.fsse2510.fsse2510_project_backend.data.transaction.dto.response.TransactionResponseDto;
import com.fsse2510.fsse2510_project_backend.mapper.transaction.TransactionDtoMapper;
import com.fsse2510.fsse2510_project_backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/transactions")
@RequiredArgsConstructor
@Validated
public class TransactionAdminController {

    private final TransactionService transactionService;
    private final TransactionDtoMapper transactionDtoMapper;

    @GetMapping
    public List<TransactionResponseDto> getAllTransactions() {
        return transactionService.getAllTransactions().stream()
                .map(transactionDtoMapper::toDto)
                .toList();
    }

    @GetMapping("/{tid}")
    public TransactionResponseDto getTransactionById(@PathVariable Integer tid) {
        return transactionDtoMapper.toDto(
                transactionService.getAdminTransactionById(tid));
    }
}
