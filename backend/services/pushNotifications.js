const path = require('path');
const admin = require('firebase-admin');
const User = require('../models/User');

let messaging;
let missingCredentialsWarningLogged = false;

function getMessaging() {
  if (messaging) return messaging;

  const serviceAccountPath = process.env.FIREBASE_SERVICE_ACCOUNT_PATH ||
    process.env.GOOGLE_APPLICATION_CREDENTIALS;
  if (!serviceAccountPath) {
    if (!missingCredentialsWarningLogged) {
      console.warn('[FCM] Push is disabled. Configure FIREBASE_SERVICE_ACCOUNT_PATH in backend/.env.');
      missingCredentialsWarningLogged = true;
    }
    return null;
  }

  const serviceAccount = require(path.resolve(process.cwd(), serviceAccountPath));
  const app = admin.apps[0] || admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
  });
  messaging = admin.messaging(app);
  return messaging;
}

async function sendPushToUser(userId, data) {
  const client = getMessaging();
  if (!client) return;

  const user = await User.findById(userId).select('+pushTokens');
  const tokens = user?.pushTokens || [];
  if (tokens.length === 0) return;

  const response = await client.sendEachForMulticast({
    tokens,
    data: Object.fromEntries(
      Object.entries(data).map(([key, value]) => [key, String(value)])
    ),
    android: { priority: 'high' },
  });

  const invalidTokens = response.responses.flatMap((result, index) => {
    const code = result.error?.code;
    return code === 'messaging/invalid-registration-token' ||
      code === 'messaging/registration-token-not-registered'
      ? [tokens[index]]
      : [];
  });

  if (invalidTokens.length > 0) {
    await User.updateOne(
      { _id: userId },
      { $pull: { pushTokens: { $in: invalidTokens } } }
    );
  }
}

module.exports = { sendPushToUser };
