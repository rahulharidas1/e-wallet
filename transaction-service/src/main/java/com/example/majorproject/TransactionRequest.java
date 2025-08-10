package com.example.majorproject;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {

    private int amount;
    private String fromUser;
    private String toUser;
    private String purpose;
}
