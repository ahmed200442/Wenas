const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_UID = "owner_admin_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read or create chat rooms, members, user_roles, or fadfada posts", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("chat_rooms").get());
  await assertFails(unauthDb.collection("members").get());
  await assertFails(unauthDb.collection("user_roles").get());
  await assertFails(unauthDb.collection("fadfada_posts").get());
});

test("Role-Based Access Control (RBAC): hamadanagy1979@gmail.com has OWNER privileges and can assign roles; regular user cannot self-assign OWNER or SUPER_ADMIN", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com" }).firestore();
  const ownerDb = testEnv.authenticatedContext(ADMIN_UID, { email: "hamadanagy1979@gmail.com" }).firestore();
  const now = new Date(Date.now() - 1000);

  // Regular user Alice CANNOT self-assign OWNER role
  await assertFails(
    aliceDb.collection("user_roles").doc(ALICE_UID).set({
      userId: ALICE_UID,
      memberIdCode: "WNS-1001",
      displayName: "أحمد المصري",
      role: "OWNER",
      permissions: ["MANAGE_USERS", "MANAGE_ROOMS"],
      assignedBy: ALICE_UID,
      updatedAt: now,
    })
  );

  // Regular user Alice CAN register her default MEMBER role
  await assertSucceeds(
    aliceDb.collection("user_roles").doc(ALICE_UID).set({
      userId: ALICE_UID,
      memberIdCode: "WNS-1001",
      displayName: "أحمد المصري",
      role: "MEMBER",
      permissions: ["JOIN_ROOMS"],
      assignedBy: ALICE_UID,
      updatedAt: now,
    })
  );

  // Primary App Owner (hamadanagy1979@gmail.com) CAN register OWNER role and promote Bob to SUPER_ADMIN
  await assertSucceeds(
    ownerDb.collection("user_roles").doc(ADMIN_UID).set({
      userId: ADMIN_UID,
      memberIdCode: "WNS-0001",
      displayName: "صاحب التطبيق الأساسي 👑",
      role: "OWNER",
      permissions: ["MANAGE_USERS", "MANAGE_ROOMS", "MANAGE_ROLES", "SYSTEM_SETTINGS"],
      assignedBy: "hamadanagy1979@gmail.com",
      updatedAt: now,
    })
  );

  await assertSucceeds(
    ownerDb.collection("user_roles").doc(BOB_UID).set({
      userId: BOB_UID,
      memberIdCode: "WNS-2002",
      displayName: "محمد سامي",
      role: "SUPER_ADMIN",
      permissions: ["MANAGE_USERS", "MANAGE_ROOMS"],
      assignedBy: "hamadanagy1979@gmail.com",
      updatedAt: now,
    })
  );
});

test("Shared Rooms, Members, Seats, and Mic Requests: all signed-in members can sync and see active rooms, 8 voice seats, and mic queue", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    aliceDb.collection("members").doc(ALICE_UID).set({
      userId: ALICE_UID,
      memberIdCode: "WNS-1001",
      displayName: "أحمد المصري",
      avatarEmoji: "👑",
      roleBadge: "VIP عضو",
      supabaseSynced: true,
      updatedAt: now,
    })
  );

  // Bob can discover Alice's synced public member profile (with her memberIdCode)
  await assertSucceeds(bobDb.collection("members").where("supabaseSynced", "==", true).get());

  await assertSucceeds(
    aliceDb.collection("chat_rooms").doc("room_alice").set({
      roomId: "room_alice",
      roomName: "Alice Voice Lounge",
      creatorId: ALICE_UID,
      timestamp: now,
    })
  );

  // Bob can list shared rooms
  await assertSucceeds(bobDb.collection("chat_rooms").get());

  // Alice sits on Seat 0 (Host)
  await assertSucceeds(
    aliceDb
      .collection("chat_rooms")
      .doc("room_alice")
      .collection("seats")
      .doc("seat_0")
      .set({
        seatId: "seat_0",
        roomId: "room_alice",
        seatIndex: 0,
        occupantUserId: ALICE_UID,
        occupantMemberCode: "WNS-1001",
        occupantName: "أحمد المصري",
        occupantEmoji: "👑",
        seatRole: "HOST",
        isSpeaking: true,
        isMuted: false,
        hasPendingMicRequest: false,
        isSeatLocked: false,
        updatedAt: now,
      })
  );

  // Bob requests mic in Alice's room with his unique memberIdCode
  await assertSucceeds(
    bobDb
      .collection("chat_rooms")
      .doc("room_alice")
      .collection("mic_requests")
      .doc("micreq_bob")
      .set({
        requestId: "micreq_bob",
        roomId: "room_alice",
        userId: BOB_UID,
        memberIdCode: "WNS-2002",
        userName: "محمد سامي",
        userEmoji: "🙋",
        queueNumber: 1,
        status: "PENDING",
        requestedSeatIndex: 1,
        timestamp: now,
      })
  );

  // Both Alice and Bob can list seats and mic requests in the room
  await assertSucceeds(
    bobDb
      .collection("chat_rooms")
      .doc("room_alice")
      .collection("seats")
      .where("roomId", "==", "room_alice")
      .get()
  );
  await assertSucceeds(
    aliceDb
      .collection("chat_rooms")
      .doc("room_alice")
      .collection("mic_requests")
      .where("roomId", "==", "room_alice")
      .get()
  );
});

test("Anonymous Fadfada with Member ID Code: regular members see memberIdCode without real name/email; App Owner Admin (hamadanagy1979@gmail.com) CAN read author name/email", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ownerDb = testEnv
    .authenticatedContext(ADMIN_UID, { email: "hamadanagy1979@gmail.com" })
    .firestore();
  const now = new Date(Date.now() - 1000);

  // Alice creates anonymous fadfada post with her unique memberIdCode (without name or email)
  await assertSucceeds(
    aliceDb.collection("fadfada_posts").doc("fadfada_1").set({
      postId: "fadfada_1",
      authorId: ALICE_UID,
      memberIdCode: "WNS-1001",
      content: "يا رب فرج قريب وراحة بال",
      moodTag: "💭 فضفضة عامة",
      heartsCount: 0,
      timestamp: now,
    })
  );

  // Alice submits her real name, email, and memberIdCode to the admin-only identity collection
  await assertSucceeds(
    aliceDb.collection("fadfada_admin_identities").doc("fadfada_1").set({
      postId: "fadfada_1",
      authorId: ALICE_UID,
      memberIdCode: "WNS-1001",
      authorName: "أحمد المصري",
      authorEmail: "alice@example.com",
      timestamp: now,
    })
  );

  // Bob (regular member) CAN read the anonymous post with memberIdCode
  await assertSucceeds(bobDb.collection("fadfada_posts").doc("fadfada_1").get());

  // Bob (regular member) CANNOT read or list the author's name and email from fadfada_admin_identities
  await assertFails(
    bobDb.collection("fadfada_admin_identities").doc("fadfada_1").get()
  );
  await assertFails(bobDb.collection("fadfada_admin_identities").get());

  // App Owner Admin (hamadanagy1979@gmail.com) CAN read and list the author's name and email from fadfada_admin_identities
  await assertSucceeds(
    ownerDb.collection("fadfada_admin_identities").doc("fadfada_1").get()
  );
  await assertSucceeds(ownerDb.collection("fadfada_admin_identities").get());
});

test("Push Notifications & Verified Payment Receipts: authenticated user can publish push notification and create verified payment receipt", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    aliceDb.collection("push_notifications").doc("notif_1").set({
      notificationId: "notif_1",
      senderId: ALICE_UID,
      senderName: "أحمد المصري",
      recipientQuery: "محمد علي",
      roomId: "room_alice",
      roomName: "غرفة السهرة",
      notificationType: "ROOM_INVITE",
      messageBody: "تفضل معنا في الغرفة",
      timestamp: now,
    })
  );

  await assertSucceeds(bobDb.collection("push_notifications").doc("notif_1").get());

  // Unverified payment receipt (verified: false) MUST fail
  await assertFails(
    aliceDb.collection("payment_receipts").doc("rcpt_unverified").set({
      receiptId: "rcpt_unverified",
      userId: ALICE_UID,
      memberName: "أحمد المصري",
      planId: "vip_gold_monthly",
      planTitle: "باقة VIP الذهبية",
      amountEgp: 150,
      paymentMethod: "BANK_CARD",
      transactionReference: "CARD-****-0366",
      verified: false,
      timestamp: now,
    })
  );

  // Verified payment receipt (verified: true) succeeds for the user
  await assertSucceeds(
    aliceDb.collection("payment_receipts").doc("rcpt_1").set({
      receiptId: "rcpt_1",
      userId: ALICE_UID,
      memberName: "أحمد المصري",
      planId: "vip_gold_monthly",
      planTitle: "باقة VIP الذهبية",
      amountEgp: 150,
      paymentMethod: "BANK_CARD",
      transactionReference: "CARD-****-0366",
      verified: true,
      timestamp: now,
    })
  );

  // Another regular user (Bob) cannot read Alice's private payment receipt
  await assertFails(bobDb.collection("payment_receipts").doc("rcpt_1").get());
});

test("Interactive Room Gift Transactions: authenticated user can send and document gift transaction in Firestore (/chat_rooms/{roomId}/room_gifts and /gift_transactions), and cannot spoof senderId", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 1000);

  // Alice sends a gift to Bob inside room_alice and documents it in room_gifts subcollection
  await assertSucceeds(
    aliceDb
      .collection("chat_rooms")
      .doc("room_alice")
      .collection("room_gifts")
      .doc("gift_tx_1")
      .set({
        transactionId: "gift_tx_1",
        roomId: "room_alice",
        roomName: "غرفة السهرة المصرية",
        giftId: "gift_crown",
        giftNameAr: "التاج الملكي",
        giftEmoji: "👑",
        costCoins: 100,
        quantity: 1,
        senderId: ALICE_UID,
        senderMemberCode: "WNS-1001",
        senderName: "أحمد المصري",
        recipientId: BOB_UID,
        recipientName: "محمد سامي",
        pkTeamSupported: "RED",
        pkBonusPoints: 300,
        timestamp: now,
      })
  );

  // Alice also documents the gift transaction in the global gift_transactions ledger
  await assertSucceeds(
    aliceDb.collection("gift_transactions").doc("gift_tx_1").set({
      transactionId: "gift_tx_1",
      roomId: "room_alice",
      roomName: "غرفة السهرة المصرية",
      giftId: "gift_crown",
      giftNameAr: "التاج الملكي",
      giftEmoji: "👑",
      costCoins: 100,
      quantity: 1,
      senderId: ALICE_UID,
      senderMemberCode: "WNS-1001",
      senderName: "أحمد المصري",
      recipientId: BOB_UID,
      recipientName: "محمد سامي",
      pkTeamSupported: "RED",
      pkBonusPoints: 300,
      timestamp: now,
    })
  );

  // Bob can read and list the room gift transactions in room_alice
  await assertSucceeds(
    bobDb
      .collection("chat_rooms")
      .doc("room_alice")
      .collection("room_gifts")
      .where("roomId", "==", "room_alice")
      .get()
  );
  await assertSucceeds(bobDb.collection("gift_transactions").doc("gift_tx_1").get());

  // Bob CANNOT spoof senderId as ALICE_UID when creating a gift transaction
  await assertFails(
    bobDb.collection("gift_transactions").doc("gift_tx_spoof").set({
      transactionId: "gift_tx_spoof",
      roomId: "room_alice",
      roomName: "غرفة السهرة المصرية",
      giftId: "gift_rose",
      giftNameAr: "وردة حمراء",
      giftEmoji: "🌹",
      costCoins: 10,
      quantity: 1,
      senderId: ALICE_UID,
      senderMemberCode: "WNS-1001",
      senderName: "أحمد المصري",
      recipientId: BOB_UID,
      recipientName: "محمد سامي",
      pkTeamSupported: "BLUE",
      pkBonusPoints: 30,
      timestamp: now,
    })
  );
});

test("WebRTC Native Signaling via Firestore: members can exchange SDP Offer/Answer and ICE Candidates and delete consumed signals", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com" }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@example.com" }).firestore();
  const now = new Date(Date.now() - 1000);

  // Alice sends an SDP OFFER to Bob in room_alice
  await assertSucceeds(
    aliceDb
      .collection("chat_rooms")
      .doc("room_alice")
      .collection("webrtc_signals")
      .doc("sig_offer_1")
      .set({
        signalId: "sig_offer_1",
        roomId: "room_alice",
        senderId: ALICE_UID,
        senderName: "أحمد المصري",
        targetUserId: BOB_UID,
        signalType: "OFFER",
        sdp: "v=0\r\no=- 4611731400430051336 2 IN IP4 127.0.0.1\r\ns=-\r\nt=0 0\r\na=group:BUNDLE 0\r\nm=audio 9 UDP/TLS/RTP/SAVPF 111\r\n",
        sdpMid: "",
        sdpMLineIndex: -1,
        candidate: "",
        sessionEpochMs: 1700000000000,
        timestamp: now,
      })
  );

  // Bob can query signals targeted to BOB_UID in room_alice
  await assertSucceeds(
    bobDb
      .collection("chat_rooms")
      .doc("room_alice")
      .collection("webrtc_signals")
      .where("roomId", "==", "room_alice")
      .where("targetUserId", "==", BOB_UID)
      .get()
  );

  // Bob responds with an SDP ANSWER to Alice
  await assertSucceeds(
    bobDb
      .collection("chat_rooms")
      .doc("room_alice")
      .collection("webrtc_signals")
      .doc("sig_answer_1")
      .set({
        signalId: "sig_answer_1",
        roomId: "room_alice",
        senderId: BOB_UID,
        senderName: "محمد سامي",
        targetUserId: ALICE_UID,
        signalType: "ANSWER",
        sdp: "v=0\r\no=- 8822731400430051336 2 IN IP4 127.0.0.1\r\ns=-\r\nt=0 0\r\na=group:BUNDLE 0\r\nm=audio 9 UDP/TLS/RTP/SAVPF 111\r\n",
        sdpMid: "",
        sdpMLineIndex: -1,
        candidate: "",
        sessionEpochMs: 1700000000500,
        timestamp: now,
      })
  );

  // Alice exchanges an ICE_CANDIDATE with Bob
  await assertSucceeds(
    aliceDb
      .collection("chat_rooms")
      .doc("room_alice")
      .collection("webrtc_signals")
      .doc("sig_ice_1")
      .set({
        signalId: "sig_ice_1",
        roomId: "room_alice",
        senderId: ALICE_UID,
        senderName: "أحمد المصري",
        targetUserId: BOB_UID,
        signalType: "ICE_CANDIDATE",
        sdp: "",
        sdpMid: "0",
        sdpMLineIndex: 0,
        candidate: "candidate:1 1 UDP 2122252543 192.168.1.10 54321 typ host",
        sessionEpochMs: 1700000000600,
        timestamp: now,
      })
  );

  // Bob can delete consumed signal addressed to him so stale signals are not replayed
  await assertSucceeds(
    bobDb
      .collection("chat_rooms")
      .doc("room_alice")
      .collection("webrtc_signals")
      .doc("sig_offer_1")
      .delete()
  );

  // Bob CANNOT spoof senderId as ALICE_UID when creating a WebRTC signal
  await assertFails(
    bobDb
      .collection("chat_rooms")
      .doc("room_alice")
      .collection("webrtc_signals")
      .doc("sig_spoof")
      .set({
        signalId: "sig_spoof",
        roomId: "room_alice",
        senderId: ALICE_UID,
        senderName: "أحمد المصري",
        targetUserId: BOB_UID,
        signalType: "OFFER",
        sdp: "v=0...",
        sdpMid: "",
        sdpMLineIndex: -1,
        candidate: "",
        sessionEpochMs: 1700000000900,
        timestamp: now,
      })
  );
});
