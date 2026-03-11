package com.fsse2510.fsse2510_project_backend.controller;

import com.fsse2510.fsse2510_project_backend.data.transaction.dto.response.TransactionResponseDto;
import com.fsse2510.fsse2510_project_backend.mapper.transaction.TransactionDtoMapper;
import com.fsse2510.fsse2510_project_backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/transactions")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class TransactionAdminController {

    private final TransactionService transactionService;
    private final TransactionDtoMapper transactionDtoMapper;

    @GetMapping
    public Page<TransactionResponseDto> getAllTransactions(
            @PageableDefault(size = 20, sort = "datetime", direction = Sort.Direction.DESC) Pageable pageable) {
        return transactionService.getAllTransactions(pageable)
                .map(transactionDtoMapper::toDto);
    }

    @GetMapping("/{tid}")
    public TransactionResponseDto getTransactionById(@PathVariable Integer tid) {
        return transactionDtoMapper.toDto(
                transactionService.getAdminTransactionById(tid));
    }

    @PatchMapping("/{tid}/status")
    public TransactionResponseDto updateTransactionStatus(@PathVariable Integer tid, @RequestParam String status) {
        return transactionDtoMapper.toDto(
                transactionService.adminUpdateTransactionStatus(tid, status));
    }
}
