package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ChatRoomRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun createChatRoom_validPayload_createsDocumentAndReturnsMetadata(): Unit = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val repository = ChatRoomRepository(firestore)
    val customRoomId = "room_${UUID.randomUUID().toString().replace("-", "")}"

    val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createChatRoom(
        roomName = ROOM_NAME,
        creatorId = aliceUid,
        customRoomId = customRoomId
      )
    }
    assertTrue(createResult.isSuccess)
    val metadata = createResult.getOrThrow()
    assertEquals(customRoomId, metadata.roomId)
    assertEquals(ROOM_NAME, metadata.roomName)
    assertEquals(aliceUid, metadata.creatorId)
    assertNotNull(metadata.timestamp)
  }

  @Test
  fun saveMemberProfile_and_enterAndLeaveRoom_publishesJoinLeaveBannersAndActiveMembers(): Unit = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val repository = ChatRoomRepository(firestore)
    val customRoomId = "room_${UUID.randomUUID().toString().replace("-", "")}"

    val memberProfileResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.saveMemberProfile(
        displayName = "أحمد المصري",
        avatarEmoji = "👑",
        roleBadge = "VIP عضو",
        supabaseSynced = true,
        userId = aliceUid
      )
    }
    assertTrue("saveMemberProfile failed: ${memberProfileResult.exceptionOrNull()}", memberProfileResult.isSuccess)

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createChatRoom(
        roomName = ROOM_NAME,
        creatorId = aliceUid,
        customRoomId = customRoomId
      ).getOrThrow()
    }

    val joinEventResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.enterRoom(
        roomId = customRoomId,
        memberName = "أحمد المصري",
        avatarEmoji = "👑",
        roleBadge = "VIP عضو",
        supabaseLinked = true,
        userId = aliceUid
      )
    }
    assertTrue(joinEventResult.isSuccess)
    assertEquals("دخل العضو أحمد المصري", joinEventResult.getOrThrow().bannerText)

    val activeMembers = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeRoomActiveMembers(customRoomId).first { it.any { m -> m.userId == aliceUid } }
    }
    assertTrue(activeMembers.any { it.memberName == "أحمد المصري" })

    val leaveEventResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.leaveRoom(
        roomId = customRoomId,
        memberName = "أحمد المصري",
        avatarEmoji = "👑",
        userId = aliceUid
      )
    }
    assertTrue(leaveEventResult.isSuccess)
    assertEquals("خرج العضو أحمد المصري", leaveEventResult.getOrThrow().bannerText)
  }

  @Test
  fun createAnonymousFadfada_regularUserCannotReadIdentity_adminOwnerCanReadIdentity(): Unit = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = ChatRoomRepository(firestore)
    val customPostId = "fadfada_${UUID.randomUUID().toString().replace("-", "")}"

    val postResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.createAnonymousFadfadaPost(
        content = "فضفضة من القلب بدون اسم",
        moodTag = "💭 فضفضة عامة",
        authorName = "أحمد المصري",
        authorEmail = ALICE_EMAIL,
        authorId = aliceUid,
        customPostId = customPostId
      )
    }
    assertTrue("createAnonymousFadfadaPost failed: ${postResult.exceptionOrNull()}", postResult.isSuccess)

    // Regular member Bob can observe the anonymous post, but CANNOT read the author's name/email
    signInTestUser(BOB_EMAIL)
    val bobRepo = ChatRoomRepository(firestore)
    val bobPosts = withTimeout(FLOW_TIMEOUT_MS) {
      bobRepo.observeAnonymousFadfadaPosts().first { it.any { p -> p.postId == customPostId } }
    }
    assertTrue(bobPosts.any { it.postId == customPostId && it.content == "فضفضة من القلب بدون اسم" })

    val bobIdentityAttempt = withTimeout(DEFAULT_TIMEOUT_MS) {
      bobRepo.getFadfadaAdminIdentity(customPostId)
    }
    assertTrue(bobIdentityAttempt.isFailure)
    val deniedEx = bobIdentityAttempt.exceptionOrNull() as? FirebaseFirestoreException
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, deniedEx?.code)

    // App Owner (hamadanagy1979@gmail.com) CAN read the author's real name and email
    signInTestUser(OWNER_ADMIN_EMAIL)
    val ownerRepo = ChatRoomRepository(firestore)
    val ownerIdentityResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      ownerRepo.getFadfadaAdminIdentity(customPostId)
    }
    assertTrue("Admin owner identity fetch failed: ${ownerIdentityResult.exceptionOrNull()}", ownerIdentityResult.isSuccess)
    val identity = ownerIdentityResult.getOrThrow()
    assertNotNull(identity)
    assertEquals("أحمد المصري", identity?.authorName)
    assertEquals(ALICE_EMAIL, identity?.authorEmail)
  }

  @Test
  fun publishPushNotification_and_createVerifiedPaymentReceipt_succeedsAndEnforcesUserOwnership(): Unit = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = ChatRoomRepository(firestore)
    val customNotifId = "notif_${UUID.randomUUID().toString().replace("-", "")}"
    val customReceiptId = "rcpt_${UUID.randomUUID().toString().replace("-", "")}"

    val notifResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.publishPushNotificationEvent(
        senderName = "أحمد المصري",
        recipientQuery = "محمد سامي",
        roomId = "room_1",
        roomName = ROOM_NAME,
        notificationType = "ROOM_INVITE",
        messageBody = "تعال انضم للغرفة",
        senderId = aliceUid,
        customNotificationId = customNotifId
      )
    }
    assertTrue("publishPushNotificationEvent failed: ${notifResult.exceptionOrNull()}", notifResult.isSuccess)
    assertTrue(notifResult.getOrThrow().isRoomInvite)

    val receiptResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.createVerifiedPaymentReceipt(
        memberName = "أحمد المصري",
        planId = "vip_gold_monthly",
        planTitle = "باقة VIP الذهبية",
        amountEgp = 150,
        paymentMethod = "BANK_CARD",
        transactionReference = "CARD-****-0366",
        userId = aliceUid,
        customReceiptId = customReceiptId
      )
    }
    assertTrue("createVerifiedPaymentReceipt failed: ${receiptResult.exceptionOrNull()}", receiptResult.isSuccess)
    assertTrue(receiptResult.getOrThrow().verified)
  }

  @Test
  fun getChatRoomById_sharedRoomVisibility_succeedsForAllSignedInUsersAndSupabaseRealtime(): Unit = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = ChatRoomRepository(firestore)
    val customRoomId = "room_${UUID.randomUUID().toString().replace("-", "")}"

    withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.createChatRoom(
        roomName = ROOM_NAME,
        creatorId = aliceUid,
        customRoomId = customRoomId
      ).getOrThrow()
    }

    signInTestUser(BOB_EMAIL)
    val bobRepo = ChatRoomRepository(firestore)
    val result = withTimeout(DEFAULT_TIMEOUT_MS) {
      bobRepo.getChatRoomById(customRoomId)
    }
    assertTrue("Expected shared room to be readable by Bob: ${result.exceptionOrNull()}", result.isSuccess)
    assertEquals(ROOM_NAME, result.getOrThrow()?.roomName)

    val realtimeRooms = withTimeout(FLOW_TIMEOUT_MS) {
      bobRepo.observeChatRooms(null).first { list -> list.any { it.roomId == customRoomId } }
    }
    assertTrue(realtimeRooms.any { it.roomId == customRoomId })
  }

  @Test
  fun getChatRoomById_unauthenticatedUser_failsWithPermissionDenied(): Unit = runBlocking {
    signOutAndAwait()
    val customRoomId = "room_${UUID.randomUUID().toString().replace("-", "")}"
    val repository = ChatRoomRepository(firestore)
    val result = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.getChatRoomById(customRoomId)
    }
    assertTrue("Expected failure when unauthenticated, got: $result", result.isFailure)
    val exception = result.exceptionOrNull() as? FirebaseFirestoreException
    assertNotNull(exception)
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, exception?.code)
  }

  @Test
  fun recordGiftTransactionInFirestore_persistsAndSyncsBetweenUsers(): Unit = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = ChatRoomRepository(firestore)
    val customRoomId = "room_gift_${UUID.randomUUID().toString().replace("-", "").take(12)}"
    val customTxId = "gift_tx_${UUID.randomUUID().toString().replace("-", "").take(12)}"

    withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.createChatRoom(
        roomName = ROOM_NAME,
        creatorId = aliceUid,
        customRoomId = customRoomId
      ).getOrThrow()

      val giftDoc = aliceRepo.recordGiftTransactionInFirestore(
        roomId = customRoomId,
        roomName = ROOM_NAME,
        giftId = "gift_crown",
        giftNameAr = "التاج الملكي",
        giftEmoji = "👑",
        costCoins = 100,
        quantity = 5,
        senderName = "أحمد المصري",
        senderMemberCode = "WNS-1001",
        recipientId = "bob_recipient_1",
        recipientName = "محمد سامي",
        pkTeamSupported = "RED",
        pkBonusPoints = 1525,
        senderId = aliceUid,
        customTransactionId = customTxId
      ).getOrThrow()

      assertEquals(customTxId, giftDoc.transactionId)
      assertEquals(customRoomId, giftDoc.roomId)
      assertEquals("التاج الملكي", giftDoc.giftNameAr)
      assertEquals(100, giftDoc.costCoins)
      assertEquals(5, giftDoc.quantity)
    }

    // Bob signs in and observes the room's gift transactions in Firestore
    signInTestUser(BOB_EMAIL)
    val bobRepo = ChatRoomRepository(firestore)
    val observedGifts = withTimeout(FLOW_TIMEOUT_MS) {
      bobRepo.observeRoomGiftTransactions(customRoomId).first { list ->
        list.any { it.transactionId == customTxId }
      }
    }
    assertTrue(observedGifts.any { it.transactionId == customTxId && it.recipientName == "محمد سامي" })
  }

  @Test
  fun publishAndObserveWebRtcSignals_exchangesSdpAndIceAndFiltersStaleSignals(): Unit = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val bobUid = signInTestUser(BOB_EMAIL)

    // Sign back in as Alice to publish a stale OFFER before Bob's session starts, then a fresh OFFER
    signInTestUser(ALICE_EMAIL)
    val aliceRepo = ChatRoomRepository(firestore)
    val customRoomId = "room_rtc_${UUID.randomUUID().toString().replace("-", "").take(12)}"
    val staleSignalId = "sig_stale_${UUID.randomUUID().toString().replace("-", "").take(10)}"
    val freshOfferId = "sig_offer_${UUID.randomUUID().toString().replace("-", "").take(10)}"
    val freshIceId = "sig_ice_${UUID.randomUUID().toString().replace("-", "").take(10)}"

    withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.createChatRoom(
        roomName = ROOM_NAME,
        creatorId = aliceUid,
        customRoomId = customRoomId
      ).getOrThrow()

      // 1. Old signal from a previous session (epoch = 1000L)
      aliceRepo.publishWebRtcSignal(
        roomId = customRoomId,
        targetUserId = bobUid,
        signalType = "OFFER",
        sdp = "v=0\r\no=- stale 2 IN IP4 127.0.0.1\r\n",
        sessionEpochMs = 1000L,
        senderName = "أحمد المصري",
        senderId = aliceUid,
        customSignalId = staleSignalId
      ).getOrThrow()

      // 2. Fresh SDP Offer for current session (epoch = 50000L)
      aliceRepo.publishWebRtcSignal(
        roomId = customRoomId,
        targetUserId = bobUid,
        signalType = "OFFER",
        sdp = "v=0\r\no=- fresh 2 IN IP4 127.0.0.1\r\nm=audio 9 UDP/TLS/RTP/SAVPF 111\r\n",
        sessionEpochMs = 50000L,
        senderName = "أحمد المصري",
        senderId = aliceUid,
        customSignalId = freshOfferId
      ).getOrThrow()

      // 3. Fresh ICE Candidate for current session (epoch = 50100L)
      aliceRepo.publishWebRtcSignal(
        roomId = customRoomId,
        targetUserId = bobUid,
        signalType = "ICE_CANDIDATE",
        sdpMid = "0",
        sdpMLineIndex = 0,
        candidate = "candidate:1 1 UDP 2122252543 192.168.1.10 54321 typ host",
        sessionEpochMs = 50100L,
        senderName = "أحمد المصري",
        senderId = aliceUid,
        customSignalId = freshIceId
      ).getOrThrow()
    }

    // Bob joins at sessionStartEpochMs = 40000L -> must ONLY receive fresh signals and NEVER replay staleSignalId
    signInTestUser(BOB_EMAIL)
    val bobRepo = ChatRoomRepository(firestore)
    val incomingSignals = withTimeout(FLOW_TIMEOUT_MS) {
      bobRepo.observeIncomingWebRtcSignals(
        roomId = customRoomId,
        targetUserId = bobUid,
        minSessionEpochMs = 40000L
      ).first { list ->
        list.any { it.signalId == freshOfferId } && list.any { it.signalId == freshIceId }
      }
    }

    assertTrue("Stale signal must not be replayed on new member entry", incomingSignals.none { it.signalId == staleSignalId })
    assertTrue(incomingSignals.any { it.signalId == freshOfferId && it.signalType == "OFFER" })
    assertTrue(incomingSignals.any { it.signalId == freshIceId && it.signalType == "ICE_CANDIDATE" })

    // Bob deletes consumed signal
    withTimeout(DEFAULT_TIMEOUT_MS) {
      bobRepo.deleteWebRtcSignal(customRoomId, freshOfferId).getOrThrow()
    }
  }

  private companion object {
    const val ALICE_EMAIL = "alice@test.com"
    const val BOB_EMAIL = "bob@test.com"
    const val OWNER_ADMIN_EMAIL = "hamadanagy1979@gmail.com"
    const val ROOM_NAME = "🌙 سهرة مصرية ونس"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 3000L
  }
}
