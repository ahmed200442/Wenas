package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.data.ChatRoomMetadata
import com.example.data.PushNotificationEvent
import com.example.data.SupabaseAccountService
import com.example.ui.screens.PushNotificationsAndPaymentScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ChatRoomsActionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PushNotificationsAndPaymentScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun inviteFriendToChatRoom_triggersPushNotificationCallback() {
        var invitedFriend = ""
        var invitedRoomName = ""

        val sampleRoom = ChatRoomMetadata(
            roomId = "room_1",
            roomName = "غرفة السهرة المصرية",
            creatorId = "user_1"
        )

        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = false) {
                PushNotificationsAndPaymentScreen(
                    actionState = ChatRoomsActionState(activeRoom = sampleRoom),
                    availableRooms = listOf(sampleRoom),
                    isDarkMode = false,
                    initialTabIndex = 0,
                    onSendFriendInvitePush = { friend, room, _ ->
                        invitedFriend = friend
                        invitedRoomName = room?.roomName.orEmpty()
                    },
                    onSendRoomMessagePush = {},
                    onProcessRealPayment = { _, _, _, _, _, _, _, _ -> false },
                    onBackToRooms = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("invite_friend_name_input")
            .performScrollTo()
            .performTextInput("محمد سامي")
        composeTestRule.onNodeWithTag("send_friend_invite_push_button")
            .performScrollTo()
            .performClick()

        assertEquals("محمد سامي", invitedFriend)
        assertEquals("غرفة السهرة المصرية", invitedRoomName)
    }

    @Test
    fun realPaymentValidation_blocksEmptyClickAndAcceptsValidPaymentDetails() {
        val service = SupabaseAccountService()

        // 1. Clicking pay without entering real card details MUST fail (cannot activate without paying)
        val emptyAttempt = service.validatePaymentCredentials(
            paymentMethod = "BANK_CARD",
            cardHolderName = "",
            cardNumber = "",
            expiryMmYy = "",
            cvv = "",
            walletPhone = "",
            transferReferenceNumber = ""
        )
        assertTrue(emptyAttempt.isFailure)

        // 2. Clicking pay with fake/invalid 16 digits that fail Luhn MUST fail
        val invalidCardAttempt = service.validatePaymentCredentials(
            paymentMethod = "BANK_CARD",
            cardHolderName = "AHMED ELMASRY",
            cardNumber = "4111111111111112",
            expiryMmYy = "08/28",
            cvv = "123",
            walletPhone = "",
            transferReferenceNumber = ""
        )
        assertTrue(invalidCardAttempt.isFailure)

        // 3. Valid 16-digit Luhn card + valid MM/YY + valid CVV succeeds
        val validCardAttempt = service.validatePaymentCredentials(
            paymentMethod = "BANK_CARD",
            cardHolderName = "AHMED ELMASRY",
            cardNumber = "4532015112830366",
            expiryMmYy = "08/28",
            cvv = "458",
            walletPhone = "",
            transferReferenceNumber = ""
        )
        assertTrue(validCardAttempt.isSuccess)
        assertEquals("CARD-****-0366", validCardAttempt.getOrThrow())

        // 4. Vodafone Cash without valid 11-digit phone or reference number MUST fail
        val invalidWalletAttempt = service.validatePaymentCredentials(
            paymentMethod = "VODAFONE_CASH",
            cardHolderName = "",
            cardNumber = "",
            expiryMmYy = "",
            cvv = "",
            walletPhone = "010123",
            transferReferenceNumber = "11"
        )
        assertFalse(invalidWalletAttempt.isSuccess)

        // 5. Valid Vodafone Cash 11-digit phone + transaction reference succeeds
        val validWalletAttempt = service.validatePaymentCredentials(
            paymentMethod = "VODAFONE_CASH",
            cardHolderName = "",
            cardNumber = "",
            expiryMmYy = "",
            cvv = "",
            walletPhone = "01012345678",
            transferReferenceNumber = "TXN987654"
        )
        assertTrue(validWalletAttempt.isSuccess)
    }

    @Test
    fun inAppNotifications_triggersFavoriteRoomLiveBroadcastAndPrivateInvite() {
        var favoriteLiveRoomTriggered: String? = null
        var privateInviteRecipientTriggered: String? = null

        val favLiveEvent = PushNotificationEvent(
            notificationId = "notif_fav_live_test",
            senderId = "host_1",
            senderName = "كابتن أحمد",
            recipientQuery = "المفضلة ⭐",
            roomId = "room_wanas_1",
            roomName = "غرفة السهر المصرية",
            notificationType = "FAVORITE_ROOM_LIVE",
            messageBody = "🔴 بدأ الآن بث مباشر في غرفتك المفضلة!"
        )

        val privateInviteEvent = PushNotificationEvent(
            notificationId = "notif_priv_inv_test",
            senderId = "friend_1",
            senderName = "محمد سامي",
            recipientQuery = "كريم",
            roomId = "room_wanas_1",
            roomName = "غرفة السهر المصرية",
            notificationType = "PRIVATE_INVITE",
            messageBody = "💌 دعوة خاصة للانضمام الفوري!"
        )

        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = false) {
                PushNotificationsAndPaymentScreen(
                    actionState = ChatRoomsActionState(
                        memberDisplayName = "أحمد",
                        favoriteRoomIds = setOf("room_wanas_1"),
                        pushNotifications = listOf(favLiveEvent, privateInviteEvent)
                    ),
                    availableRooms = listOf(
                        ChatRoomMetadata(roomId = "room_wanas_1", roomName = "غرفة السهر المصرية")
                    ),
                    isDarkMode = false,
                    initialTabIndex = 0,
                    onSendFriendInvitePush = { _, _, _ -> },
                    onSendRoomMessagePush = {},
                    onTriggerFavoriteRoomLivePush = { room, _ ->
                        favoriteLiveRoomTriggered = room?.roomName
                    },
                    onSendPrivateInvitePush = { recipient, _, _ ->
                        privateInviteRecipientTriggered = recipient
                    },
                    onProcessRealPayment = { _, _, _, _, _, _, _, _ -> false },
                    onBackToRooms = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("favorite_rooms_live_notifications_card")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("start_favorite_room_live_broadcast_push_button")
            .performScrollTo()
            .performClick()

        assertEquals("غرفة السهر المصرية", favoriteLiveRoomTriggered)

        composeTestRule.onNodeWithTag("send_private_invite_push_button")
            .performScrollTo()
            .performClick()

        assertEquals("صديق مقرب", privateInviteRecipientTriggered)

        composeTestRule.onNodeWithTag("push_notification_item_notif_fav_live_test")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("push_notification_item_notif_priv_inv_test")
            .performScrollTo()
            .assertIsDisplayed()
    }
}
