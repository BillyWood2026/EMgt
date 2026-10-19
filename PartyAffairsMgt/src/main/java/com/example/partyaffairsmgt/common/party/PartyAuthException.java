package com.example.partyaffairsmgt.common.party;

/** 党务权限异常，返回 403。 */
public class PartyAuthException extends RuntimeException {
    public PartyAuthException(String message) {
        super(message);
    }
}
