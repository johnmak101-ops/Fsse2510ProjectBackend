# Membership State Machine

> Membership tier lifecycle: upgrade, maintain, and downgrade logic.

## Tier Hierarchy

```mermaid
flowchart LR
    N[NO_MEMBERSHIP<br/>rank 0] --> B[BRONZE<br/>rank 1]
    B --> S[SILVER<br/>rank 2]
    S --> G[GOLD<br/>rank 3]
    G --> D[DIAMOND<br/>rank 4]
```

## Membership Evaluation Flow

> Triggered after every successful transaction via `finalizeSuccess`.

```mermaid
flowchart TD
    A[Transaction SUCCESS] --> B[Calculate earnedPoints<br/>= total × pointRate]
    B --> C[Add to accumulatedSpending]
    C --> D[Add to cycleSpending]

    D --> E{accumulatedSpending >=<br/>next tier minSpend?}
    E -- Yes --> F[🔺 UPGRADE to next tier]
    F --> F1[Reset cycleSpending = 0]
    F1 --> F2[Set cycleEndDate<br/>= now + 1 year]
    F2 --> G[Save User]

    E -- No --> H{cycleEndDate expired?}
    H -- No --> G
    H -- Yes --> I{isInGracePeriod?}
    I -- No --> I1[Enter Grace Period<br/>gracePeriodDays from config]
    I1 --> G
    I -- Yes --> J{Grace period<br/>expired?}
    J -- No --> G
    J -- Yes --> K{cycleSpending >=<br/>current tier minSpend?}
    K -- Yes --> L[✅ MAINTAIN tier<br/>Reset cycle]
    K -- No --> M[🔻 DOWNGRADE<br/>to previous tier]
    M --> M1[Reset cycleSpending = 0]
    M1 --> M2[New cycleEndDate]
    L --> G
    M2 --> G
```

## Membership Config Table

| Level | Min Spend | Point Rate | Grace Period |
|-------|-----------|------------|--------------|
| NO_MEMBERSHIP | $0 | 0% | 0 days |
| BRONZE | $500 | 1% | 30 days |
| SILVER | $2,000 | 2% | 30 days |
| GOLD | $5,000 | 3% | 60 days |
| DIAMOND | $10,000 | 5% | 90 days |

> **Note:** Values above are example configs from `membership_config` table. Actual values are admin-configurable.
