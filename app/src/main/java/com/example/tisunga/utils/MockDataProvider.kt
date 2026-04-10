package com.example.tisunga.utils

import com.example.tisunga.data.model.*

object MockDataProvider {

    fun getMockUser() = User(
        id = 1,
        firstName = "Michael",
        middleName = "Enock",
        lastName = "Phiri",
        phone = "0882752624",
        nationalId = null, // Set to null to test profile completion prompt
        role = "chairperson"
    )

    fun getMockGroups() = listOf(
        Group(
            id = 1,
            name = "Doman Group",
            description = "We save to show love",
            location = "Zomba, Chikanda",
            minContribution = 2000.0,
            savingPeriod = 6,
            maxMembers = 15,
            visibility = "public",
            startDate = "20th May 2026",
            endDate = "21st May 2027",
            meetingDay = "Friday",
            meetingTime = "3:00pm",
            totalSavings = 1000090.0,
            mySavings = 12500.0,
            status = "active",
            groupCode = "67WEISH6"
        ),
        Group(
            id = 2,
            name = "Chikondano Group",
            description = "We save to show love",
            location = "Blantyre, Ndirande",
            minContribution = 2000.0,
            savingPeriod = 12,
            maxMembers = 20,
            visibility = "public",
            startDate = "1st Jan 2026",
            endDate = "31st Dec 2026",
            meetingDay = "Saturday",
            meetingTime = "2:00pm",
            totalSavings = 200000.0,
            mySavings = 5000.0,
            status = "active",
            groupCode = "ABCDEFGH"
        )
    )

    fun getMockMembers() = listOf(
        User(id=1, firstName="You", lastName="",
             phone="0997486222", role="chairperson"),
        User(id=2, firstName="Joypus", lastName="Phirri",
             phone="0997486222", role="secretary"),
        User(id=3, firstName="Zechael", lastName="Chisi",
             phone="0997489899", role="member"),
        User(id=4, firstName="Alinafe", lastName="Zamwe",
             phone="0997489899", role="member"),
        User(id=5, firstName="Alinafe", lastName="Zandiwo",
             phone="0998928373", role="member"),
        User(id=6, firstName="Zanene", lastName="Tswangati",
             phone="0998928373", role="member"),
        User(id=7, firstName="Zanene", lastName="Tswangati",
             phone="0998928373", role="member"),
        User(id=8, firstName="Zanene", lastName="Tswangati",
             phone="0998928373", role="member")
    )

    fun getMockLoans() = listOf(
        Loan(
            id=1, memberId=1, groupId=1,
            groupName="Doman Group Loan",
            memberName = "Michael Enock",
            amount=650000.0,
            interestRate=5.0,
            repayableAmount=700000.0,
            remainingAmount=350000.0,
            percentRepaid=0.5f,
            status="active",
            approvedBy="Laston Mzumala",
            approvalDate="Feb 01 2026",
            dueDate="nov 04, 2026",
            purpose="Business",
            period="6 months"
        ),
        Loan(
            id=2, memberId=1, groupId=2,
            groupName="Kalulu Group Loan",
            memberName = "Michael Enock",
            amount=650000.0,
            interestRate=5.0,
            repayableAmount=700000.0,
            remainingAmount=350000.0,
            percentRepaid=0.5f,
            status="active",
            approvedBy="Laston Mzumala",
            approvalDate="Feb 01 2026",
            dueDate="nov 04, 2026",
            purpose="School fees",
            period="3 months"
        )
    )

    fun getMockPendingLoans() = listOf(
        Loan(
            id=3, memberId=2, groupId=1,
            groupName="Doman Group Loan",
            memberName = "Joypus Phirri",
            amount=650000.0,
            interestRate=5.0,
            repayableAmount=682500.0,
            remainingAmount=50000.0,
            percentRepaid=0.5f,
            status="pending",
            approvedBy=null,
            approvalDate=null,
            dueDate="nov 04, 2026",
            purpose="Medical",
            period="2 months"
        )
    )

    fun getMockContributions() = listOf(
        Contribution(
            id=1,
            userId=1, userName="Michael Enock",
            groupId=1,
            amount=27000.0, type="regular",
            timestamp="mar 4, 2089",
            status = "completed"
        ),
        Contribution(
            id=2,
            userId=1, userName="Michael Enock",
            groupId=1,
            amount=27000.0, type="special",
            timestamp="mar 10, 2089",
            status = "completed"
        ),
        Contribution(
            id=3,
            userId=1, userName="Michael Enock",
            groupId=1,
            amount=27000.0, type="regular",
            timestamp="jun 28, 2089",
            status = "completed"
        ),
        Contribution(
            id=4,
            userId=1, userName="Michael Enock",
            groupId=1,
            amount=27000.0, type="special",
            timestamp="mar 4, 2089",
            status = "completed"
        ),
        Contribution(
            id=5,
            userId=1, userName="Michael Enock",
            groupId=1,
            amount=27000.0, type="regular",
            timestamp="mar 4, 2089",
            status = "completed"
        )
    )

    fun getMockEvents() = listOf(
        Event(
            id=1, groupId=1,
            title="Uchiae \u0026 Micheal Wedding",
            type="Wedding",
            date="sep 09, 2045",
            amount=600.0,
            amountType = "Fixed",
            status="active",
            raisedAmount=12000.0,
            description = "Wedding contribution"
        ),
        Event(
            id=2, groupId=1,
            title="Laston mzumala's Birthday",
            type="Birthday",
            date="jun 08, 2008",
            amount=0.0,
            amountType = "Flexible",
            status="active",
            raisedAmount=5000.0,
            description = "Birthday gift"
        ),
        Event(
            id=3, groupId=1,
            title="Emma's father. Mr Ducan",
            type="Funeral",
            date="Apr 08, 2024",
            amount=0.0,
            amountType = "Flexible",
            status="closed",
            raisedAmount=45000.0,
            description = "Funeral support"
        )
    )

    fun getMockTransactions() = listOf(
        Transaction(
            transId="TISU29993.90",
            groupId=1,
            type="contribution",
            amount=20000.0,
            description="The Doman Account have received MK 20,000 from 099978223, Chikula Phiri, group member. Bal MK 1,000000",
            timestamp="Friday, March 19, 2026 5:00PM",
            balanceAfter = 1000000.0
        ),
        Transaction(
            transId="TISU29993.91",
            groupId=1,
            type="loan_withdrawal",
            amount=20000.0,
            description="An amount of MK 20,000. Has been withdrawn from the group by Chikula Phiri 099978223, group member, For an approved loan. Bal MK 1,000000",
            timestamp="Friday, March 19, 2026 5:00PM",
            balanceAfter = 1000000.0
        ),
        Transaction(
            transId="TISU29993.92",
            groupId=1,
            type="loan_repayment",
            amount=1000.0,
            description="Loan Repayment. Group Member Chikondi Tiwatu has sent Mk 1,000. Towards a 2 months of MK 20,000. Loan Balance: Completed",
            timestamp="Friday, March 19, 2026 5:00PM",
            balanceAfter = 1001000.0
        ),
        Transaction(
            transId="TISU29993.93",
            groupId=1,
            type="loan_approval",
            amount=20000.0,
            description="Loan application of Chikondi Phiri has been approved By the Chair Tiyamike Tobias. Loan Amount: MK 20,000. Duration: 2 Months. Interest: MK 2000. Group Balance: 50,000",
            timestamp="Friday, March 19, 2026 5:00PM",
            balanceAfter = 50000.0
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
