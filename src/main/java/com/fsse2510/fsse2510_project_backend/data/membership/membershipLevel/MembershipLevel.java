package com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel;

@lombok.Getter
public enum MembershipLevel {
    NO_MEMBERSHIP(0),
    BRONZE(1),
    SILVER(2),
    GOLD(3),
    DIAMOND(4);

    private final int rank;

    MembershipLevel(int rank) {
        this.rank = rank;
    }

    public MembershipLevel getPrevious() {
        if (this.rank == 0)
            return this;
        return MembershipLevel.values()[this.rank - 1];
    }

    public MembershipLevel getNext() {
        if (isMaxLevel())
            return this;
        return MembershipLevel.values()[this.rank + 1];
    }

    public boolean isMaxLevel() {
        return this == DIAMOND;
    }
}
