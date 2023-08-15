package com.kafkalibevent.example.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record Book (
        @NotNull
        Integer bookId,
        @NotBlank
        String bookAuthor,
        @NotBlank
        String bookName
){
}
