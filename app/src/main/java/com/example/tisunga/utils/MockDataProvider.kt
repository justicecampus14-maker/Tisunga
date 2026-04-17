package com.example.tisunga.utils

import com.example.tisunga.data.model.*

object MockDataProvider {

    fun getMockUser() = User(
        id = "1",
        firstName = "Michael",
        middleName = "Enock",
        lastName = "Phiri",
        phone = "0882752624",
        role = "chairperson"
    )

    fun getMockGroups() = listOf(
        Group(
            id = "1",
            name = "Doman Group",
            description = "We save to show love",
            location = "Zomba, Chikanda",
            minContribution = 2000.0,
            savingPeriod = 6,
            maxMembers = 15,
            startDate = "20th May 2026",
            endDate = "21st May 2027",
            meetingDay = "Friday",
            meetingTime = "3:00pm",
            totalSavings = 1000090.0,
            mySavings = 12500.0,
            isActive = true,
            groupCode = "67WEISH6"
        ),
        Group(
            id = "2",
            name = "Chikondano Group",
            description = "We save to show love",
            location = "Blantyre, Ndirande",
            minContribution = 2000.0,
            savingPeriod = 12,
            maxMembers = 20,
            startDate = "1st Jan 2026",
            endDate = "31st Dec 2026",
            meetingDay = "Saturday",
            meetingTime = "2:00pm",
            totalSavings = 200000.0,
            mySavings = 5000.0,
            isActive = true,
            groupCode = "ABCDEFGH"
        )
    )

    fun getMockMembers() = listOf(
        User(id="1", firstName="You", lastName="",
             phone="0997486222", role="chairperson"),
        User(id="2", firstName="Joypus", lastName="Phirri",
             phone="0997486222", role="secretary"),
        User(id="3", firstName="Zechael", lastName="Chisi",
             phone="0997489899", role="member"),
        User(id="4", firstName="Alinafe", lastName="Zamwe",
             phone="0997489899", role="member"),
        User(id="5", firstName="Alinafe", lastName="Zandiwo",
             phone="0998928373", role="member"),
        User(id="6", firstName="Zanene", lastName="Tswangati",
             phone="0998928373", role="member"),
        User(id="7", firstName="Zanene", lastName="Tswangati",
             phone="0998928373", role="member"),
        User(id="8", firstName="Zanene", lastName="Tswangati",
             phone="0998928373", role="member")
    )

    fun getMockLoans() = listOf(
        Loan(
            id="1", borrowerId="1", groupId="1",
            borrowerName = "Michael Enock",
            principalAmount=650000.0,
            interestRate=5.0,
            totalRepayable=700000.0,
            remainingBalance=350000.0,
            durationMonths=6,
            status="active",
            approverName="Laston Mzumala",
            approvedAt="Feb 01 2026",
            dueDate="nov 04, 2026",
            purpose="Business",
            createdAt="2026-02-01T00:00:00Z",
            updatedAt="2026-02-01T00:00:00Z"
        ),
        Loan(
            id="2", borrowerId="1", groupId="2",
            borrowerName = "Michael Enock",
            principalAmount=650000.0,
            interestRate=5.0,
            totalRepayable=700000.0,
            remainingBalance=350000.0,
            durationMonths=3,
            status="active",
            approverName="Laston Mzumala",
            approvedAt="Feb 01 2026",
            dueDate="nov 04, 2026",
            purpose="School fees",
            createdAt="2026-02-01T00:00:00Z",
            updatedAt="2026-02-01T00:00:00Z"
        )
    )

    fun getMockPendingLoans() = listOf(
        Loan(
            id="3", borrowerId="2", groupId="1",
            borrowerName = "Joypus Phirri",
            principalAmount=650000.0,
            interestRate=5.0,
            totalRepayable=682500.0,
            remainingBalance=50000.0,
            durationMonths=2,
            status="pending",
            approverName=null,
            approvedAt=null,
            dueDate="nov 04, 2026",
            purpose="Medical",
            createdAt="2026-02-01T00:00:00Z",
            updatedAt="2026-02-01T00:00:00Z"
        )
    )

    fun getMockContributions() = listOf(
        Contribution(
            id="1",
            userId="1",
            groupId="1",
            amount=27000.0, type="regular",
            status = "completed",
            transactionRef = "REF001",
            externalRef = "EXT001",
            phoneUsed = "0882752624",
            failureReason = null,
            createdAt = "mar 4, 2089",
            updatedAt = "mar 4, 2089"
        ),
        Contribution(
            id="2",
            userId="1",
            groupId="1",
            amount=27000.0, type="special",
            status = "completed",
            transactionRef = "REF002",
            externalRef = "EXT002",
            phoneUsed = "0882752624",
            failureReason = null,
            createdAt = "mar 10, 2089",
            updatedAt = "mar 10, 2089"
        ),
        Contribution(
            id="3",
            userId="1",
            groupId="1",
            amount=27000.0, type="regular",
            status = "completed",
            transactionRef = "REF003",
            externalRef = "EXT003",
            phoneUsed = "0882752624",
            failureReason = null,
            createdAt = "jun 28, 2089",
            updatedAt = "jun 28, 2089"
        ),
        Contribution(
            id="4",
            userId="1",
            groupId="1",
            amount=27000.0, type="special",
            status = "completed",
            transactionRef = "REF004",
            externalRef = "EXT004",
            phoneUsed = "0882752624",
            failureReason = null,
            createdAt = "mar 4, 2089",
            updatedAt = "mar 4, 2089"
        ),
        Contribution(
            id="5",
            userId="1",
            groupId="1",
            amount=27000.0, type="regular",
            status = "completed",
            transactionRef = "REF005",
            externalRef = "EXT005",
            phoneUsed = "0882752624",
            failureReason = null,
            createdAt = "mar 4, 2089",
            updatedAt = "mar 4, 2089"
        )
    )

    fun getMockEvents() = listOf(
        Event(
            id="1",
            title="Uchiae & Micheal Wedding",
            description = "Wedding contribution",
            targetAmount=600.0,
            currentAmount=12000.0,
            endDate="sep 09, 2045",
            status="OPEN",
            createdAt="2026-01-01T00:00:00Z"
        ),
        Event(
            id="2",
            title="Laston mzumala's Birthday",
            description = "Birthday gift",
            targetAmount=0.0,
            currentAmount=5000.0,
            endDate="jun 08, 2008",
            status="OPEN",
            createdAt="2026-01-01T00:00:00Z"
        ),
        Event(
            id="3",
            title="Emma's father. Mr Ducan",
            description = "Funeral support",
            targetAmount=0.0,
            currentAmount=45000.0,
            endDate="Apr 08, 2024",
            status="CLOSED",
            createdAt="2026-01-01T00:00:00Z"
        )
    )

    fun getMockTransactions() = listOf(
        Transaction(
            id="1",
            groupId="1",
            userId="1",
            type=TransactionType.SAVINGS,
            amount=20000.0,
            balanceAfter = 1000000.0,
            description="The Doman Account have received MK 20,000 from 099978223, Chikula Phiri, group member. Bal MK 1,000000",
            tisuRef="TISU29993.90",
            createdAt="Friday, March 19, 2026 5:00PM",
            updatedAt="Friday, March 19, 2026 5:00PM",
            memberName = "Chikula Phiri"
        ),
        Transaction(
            id="2",
            groupId="1",
            userId="1",
            type=TransactionType.LOAN_OUT,
            amount=20000.0,
            balanceAfter = 1000000.0,
            description="An amount of MK 20,000. Has been withdrawn from the group by Chikula Phiri 099978223, group member, For an approved loan. Bal MK 1,000000",
            tisuRef="TISU29993.91",
            createdAt="Friday, March 19, 2026 5:00PM",
            updatedAt="Friday, March 19, 2026 5:00PM",
            memberName = "Chikula Phiri"
        ),
        Transaction(
            id="3",
            groupId="1",
            userId="1",
            type=TransactionType.LOAN_IN,
            amount=1000.0,
            balanceAfter = 1001000.0,
            description="Loan Repayment. Group Member Chikondi Tiwatu has sent Mk 1,000. Towards a 2 months of MK 20,000. Loan Balance: Completed",
            tisuRef="TISU29993.92",
            createdAt="Friday, March 19, 2026 5:00PM",
            updatedAt="Friday, March 19, 2026 5:00PM",
            memberName = "Chikondi Tiwatu"
        ),
        Transaction(
            id="4",
            groupId="1",
            userId="1",
            type=TransactionType.SYSTEM,
            amount=20000.0,
            balanceAfter = 50000.0,
            description="Loan application of Chikondi Phiri has been approved By the Chair Tiyamike Tobias. Loan Amount: MK 20,000. Duration: 2 Months. Interest: MK 2000. Group Balance: 50,000",
            tisuRef="TISU29993.93",
            createdAt="Friday, March 19, 2026 5:00PM",
            updatedAt="Friday, March 19, 2026 5:00PM",
            memberName = "Tiyamike Tobias"
        )
    )

    fun getMockNotifications() = listOf(
        mapOf(
            "senderName" to "Doman Group",
            "initials" to "DG",
            "avatarColor" to "0xFFCE93D8",
            "message" to "Transaction ID 2039393......",
            "date" to "3/20",
            "time" to "6:26PM",
            "unreadCount" to "5"
        ),
        mapOf(
            "senderName" to "Tisunga",
            "initials" to "TA",
            "avatarColor" to "0xFFEF9A9A",
            "message" to "We are currently workin....",
            "date" to "3/20",
            "time" to "6:26PM",
            "unreadCount" to "5"
        ),
        mapOf(
            "senderName" to "Chikondano Group",
            "initials" to "CG",
            "avatarColor" to "0xFF90CAF9",
            "message" to "Loan grated to Mphatso...",
            "date" to "3/20",
            "time" to "6:26PM",
            "unreadCount" to "1"
        ),
        mapOf(
            "senderName" to "Doman Group",
            "initials" to "DG",
            "avatarColor" to "0xFFF48FB1",
            "message" to "Transaction ID 2039393......",
            "date" to "3/20",
            "time" to "6:26PM",
            "unreadCount" to "5"
        ),
        mapOf(
            "senderName" to "Doman Group",
            "initials" to "DG",
            "avatarColor" to "0xFF80CBC4",
            "message" to "Transaction ID 2039393......",
            "date" to "3/20",
            "time" to "6:26PM",
            "unreadCount" to "5"
        )
    )

    fun getMockMemberShares() = listOf(
        MemberShare(1, "Michael Enock", 40000.0),
        MemberShare(2, "Joypus Phirri", 40000.0),
        MemberShare(3, "Zechael Chisi", 40000.0),
        MemberShare(4, "Alinafe Zamwe", 40000.0),
        MemberShare(5, "Zanene Tswangati", 40000.0)
    )

    const val MOCK_TOKEN = "mock_jwt_token_for_development"
    const val MOCK_GROUP_CODE = "67WEISH6"
    const val MOCK_TOTAL_SAVINGS = 200000.0
    const val MOCK_GROUP_BALANCE = 1000090.0
}
