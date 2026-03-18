# Checkout & Transaction Flow

> End-to-end checkout flow from cart to payment completion.

## 1. Create Transaction

```mermaid
flowchart TD
    A[User clicks Checkout] --> B[createTransaction]
    B --> C{Cart Empty?}
    C -- Yes --> C1[❌ CartEmptyException]
    C -- No --> D[getOrCreateUser]
    D --> E[initTransactionEntity<br/>status = PENDING]
    E --> F[processTransactionItems]

    F --> F1[Fetch Pending SKU Quantities<br/>from other PENDING/PROCESSING txns]
    F1 --> F2[Calculate virtualStock<br/>= stock - pending]
    F2 --> F3{cartQty > virtualStock?}
    F3 -- Yes --> F4[❌ NotEnoughStockException]
    F3 -- No --> F5[Build TransactionProduct<br/>snapshot price + subtotal]

    F5 --> G{Coupon Code<br/>provided?}
    G -- Yes --> G1[validateCoupon<br/>check tier, expiry, min spend]
    G1 --> G2[Apply PERCENTAGE<br/>or FIXED discount]
    G -- No --> H{Points<br/>requested?}
    G2 --> H

    H -- Yes --> H1[Calculate virtualPoints<br/>= points - pending points]
    H1 --> H2[Apply points discount<br/>rate: 100 pts = $1]
    H -- No --> I[snapshotAddress<br/>copy address to transaction]
    H2 --> I

    I --> J{Total in Dead Zone?<br/>0 < total < MIN_PAYMENT}
    J -- Yes --> J1[❌ IllegalPaymentOperationException]
    J -- No --> K[Save Transaction]
    K --> L[Clear User Cart]
    L --> M[✅ Return TransactionResponseData]
```

## 2. Prepare Payment (Stripe Checkout)

```mermaid
flowchart TD
    A[preparePayment] --> B{Transaction Status?}
    B -- ABORTED/FAILED --> B1[❌ Cannot pay]
    B -- SUCCESS --> B2[Return ALREADY_PAID]
    B -- PENDING/PROCESSING --> C{Total == $0?}
    C -- Yes --> C1[Return skip_stripe URL]
    C -- No --> D{Total < MIN_AMOUNT?}
    D -- Yes --> D1[❌ Amount too low]
    D -- No --> E[Create Stripe<br/>Checkout Session]
    E --> F[Return session URL<br/>+ transaction ID]
```

## 3. Finish Transaction (Payment Verification)

```mermaid
flowchart TD
    A[finishTransaction] --> B{Status?}
    B -- SUCCESS --> B1[Return existing data]
    B -- ABORTED --> B2[❌ Security Alert]
    B -- PENDING --> C[Set PROCESSING]
    B -- PROCESSING --> D{Bypass Stripe?}
    C --> D

    D -- "Yes (zero amount)" --> G[finalizeSuccess]
    D -- No --> E[Retrieve PaymentIntent]
    E --> E1{Intent Status?}
    E1 -- "succeeded" --> F[Verify Amount Match]
    E1 -- "requires_capture" --> F2[Capture Payment]
    E1 -- Other --> E2[❌ Payment not ready]

    F --> F1{Stripe $ == Transaction $?}
    F1 -- No --> F3[❌ Amount Mismatch<br/>Security Alert]
    F1 -- Yes --> G
    F2 --> G

    G[finalizeSuccess] --> G1[Deduct Stock]
    G1 --> G2[Deduct Points Used]
    G2 --> G3[Earn Points<br/>based on membership tier]
    G3 --> G4[Evaluate Membership<br/>Upgrade/Downgrade]
    G4 --> G5[Set status = SUCCESS]
    G5 --> G6[✅ Return TransactionResponseData]

    G --> H{Exception?}
    H -- Yes --> H1[handleFailureWithRecovery]
    H1 --> H2[Restore Stock]
    H2 --> H3[Restore Points]
    H3 --> H4[Set status = FAILED]
```

## 4. Transaction State Machine

```mermaid
stateDiagram-v2
    [*] --> PENDING : createTransaction
    PENDING --> PROCESSING : preparePayment / finishTransaction
    PENDING --> ABORTED : abortTransaction / Stripe webhook canceled

    PROCESSING --> SUCCESS : finishTransaction verified
    PROCESSING --> FAILED : Payment failed / webhook
    PROCESSING --> ABORTED : Stripe webhook canceled

    SUCCESS --> [*]
    FAILED --> [*]
    ABORTED --> [*]

    note right of SUCCESS : Stock deducted\nPoints deducted/earned\nMembership evaluated
    note right of FAILED : Stock restored\nPoints restored
    note right of ABORTED : Cart items recovered
```
