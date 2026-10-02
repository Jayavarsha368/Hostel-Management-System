# FreelancerConnect

A two-sided Android marketplace connecting **freelancers** with **clients**.

**Database**: Local MongoDB (via MongoDB Compass)  
**Architecture**: Android (MVVM + Retrofit) ↔ Node.js/Express REST API ↔ MongoDB

---

## Architecture Overview

```
┌─────────────────────┐       HTTP/JSON       ┌──────────────────────┐       ┌──────────────┐
│  Android App        │ ◄──────────────────► │  Express.js API      │ ◄───► │  MongoDB     │
│  (Kotlin + Retrofit)│    localhost:3000     │  (Node.js backend)   │       │  (Compass)   │
└─────────────────────┘                       └──────────────────────┘       └──────────────┘
```

> **Android Emulator**: uses `10.0.2.2` to reach the host machine's `localhost`  
> **Physical Device**: change `BASE_URL` in `ApiClient.kt` to your PC's local IP (e.g. `192.168.1.x`)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Android Language | Kotlin |
| Android Architecture | MVVM (ViewModel + LiveData) |
| HTTP Client | Retrofit 2 + OkHttp 4 |
| Auth | JWT (stored in SharedPreferences) |
| Backend | Node.js + Express.js |
| ODM | Mongoose 8 |
| Database | MongoDB (local, via MongoDB Compass) |
| Password Hashing | bcryptjs |
| Image Loading | Coil |

---

## Quick Start

### 1. Start MongoDB
Open **MongoDB Compass** and connect to `mongodb://localhost:27017`.  
The database `freelancerconnect` will be created automatically on first run.

### 2. Start the Backend
```bash
cd backend
npm install
npm run dev       # uses nodemon for auto-restart
```
Server starts at → `http://localhost:3000`  
Test it: `http://localhost:3000/health`

### 3. Enable Android message and application push notifications
The app uses Firebase Cloud Messaging for notifications in the phone's notification tray. In the Firebase Console, create a service-account key for the same Firebase project used by `app/google-services.json`. Store the downloaded JSON key outside this repository, then set this path in `backend/.env`:

```env
FIREBASE_SERVICE_ACCOUNT_PATH=C:/Users/your-name/AppData/Roaming/FreelancerConnect/firebase-service-account.json
```

Restart the backend after setting the path. Sign in to the app and allow notifications when Android asks. Do not commit or share the service-account key; it grants access to the Firebase project.

### 4. Enable Google Sign-In
In Firebase Console, enable Google under **Authentication → Sign-in method** and register the Android app's SHA-1 fingerprint. Copy the OAuth **Web client ID** from the same Firebase/Google Cloud project. Use that same ID in `backend/.env`:

```env
GOOGLE_WEB_CLIENT_ID=your-web-client-id.apps.googleusercontent.com
```

Set `GOOGLE_WEB_CLIENT_ID` as a Windows environment variable before starting VS Code so Gradle can configure the Android client. Restart the backend and rebuild/redeploy the app after configuration. Do not put this ID in tracked source files. Until both sides are configured, Google sign-in intentionally reports that setup is required; email/password sign-in remains available.

### 5. Run on a Physical Android Phone from VS Code

Android Studio is not required. Install/configure a Java 17 JDK (`JAVA_HOME`) and the Android SDK with **Platform-Tools**. This project already has a Gradle wrapper, so VS Code can build and install the app directly.

1. On your phone, enable **Developer options** and **USB debugging**, connect it by USB, and approve the debugging prompt.
2. In VS Code, run **Terminal → Run Task → Android: Check connected phone**. The phone should appear as `device` (not `unauthorized`).
3. Start MongoDB, then use **Terminal → Run Task → Backend: Start API**. Wait for `MongoDB connected` and `API running` in the terminal.
4. Run **Terminal → Run Task → Android: Deploy to phone** to build, install, establish USB ADB port forwarding, and launch the app. Keep the phone connected by USB and the API running. Use **Android: Build debug APK** if you only need an APK; it is written to `app/build/outputs/apk/debug/app-debug.apk`.

The Gradle tasks require Java 17 available to VS Code through a valid `JAVA_HOME`.

### 6. Deploy for public use
USB forwarding and local MongoDB are development-only. For public use, deploy the API and database separately:

1. Create a MongoDB Atlas cluster and database user. Add the Render service's outbound IPs to Atlas Network Access, or use a temporary broad IP rule only during setup. Copy the Atlas connection string for `MONGO_URI`.
2. In Render, create a Blueprint from this repository using `render.yaml`. The blueprint creates the public Node API and a persistent disk for uploaded resumes and portfolios. The configured Starter web service and disk are paid Render resources.
3. Set the generated API's `MONGO_URI`. Configure `GOOGLE_WEB_CLIENT_ID` if Google Sign-In is enabled. For push notifications, set `FIREBASE_SERVICE_ACCOUNT_JSON` to the Firebase service-account JSON contents. Set secrets only in the hosting dashboard, never in Git.
4. Wait for Render's `/health` check to pass, then build the Android release against the HTTPS URL:

```powershell
.\gradlew.bat assembleRelease -PAPI_BASE_URL=https://YOUR-API.onrender.com/
```

For Google Sign-In, also pass `-PGOOGLE_WEB_CLIENT_ID=YOUR_WEB_CLIENT_ID` and set the same ID in Render. With signing configured, the APK is at `app/build/outputs/apk/release/app-release.apk`; without it, the output is `app-release-unsigned.apk` and cannot be installed. Direct distribution requires each user to allow APK installation; Google Play distribution also requires a Play Console listing. Release builds do not log HTTP bodies.

To sign a directly distributed release, create a private keystore once and keep a secure backup:

```powershell
keytool -genkeypair -v -keystore freelancerconnect-release.jks -alias freelancerconnect -keyalg RSA -keysize 2048 -validity 10000
```

Configure `ANDROID_KEYSTORE_PATH`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and `ANDROID_KEY_PASSWORD` as local environment variables before building. Never commit the keystore or passwords.

---

## Project Structure

```
FreelancerConnect/
│
├── backend/                          ← Node.js REST API
│   ├── server.js                     ← Express entry point + MongoDB connection
│   ├── .env                          ← MONGO_URI, JWT_SECRET, PORT
│   ├── models/
│   │   ├── User.js
│   │   ├── Job.js
│   │   ├── Application.js
│   │   ├── Message.js
│   │   ├── Review.js
│   │   └── Notification.js
│   ├── routes/
│   │   ├── auth.js                   ← POST /auth/register, /auth/login, /auth/reset-password
│   │   ├── users.js                  ← GET/PUT /users/:id
│   │   ├── jobs.js                   ← CRUD /jobs
│   │   ├── applications.js           ← /applications + status update
│   │   ├── messages.js               ← /messages/:conversationId
│   │   └── notifications.js          ← /notifications + mark-read
│   └── middleware/
│       └── auth.js                   ← JWT Bearer token validation
│
└── app/src/main/java/com/example/freelancerconnect/
    │
    ├── api/                          ← Replaces Firebase package entirely
    │   ├── ApiClient.kt              ← Retrofit singleton (base URL: 10.0.2.2:3000)
    │   ├── ApiService.kt             ← All endpoint definitions (Retrofit interface)
    │   ├── ApiModels.kt              ← Request/Response data classes (DTOs)
    │   ├── ApiResult.kt              ← Sealed class: Success / Error / Loading
    │   └── SessionManager.kt         ← JWT token + user info in SharedPreferences
    │
    ├── activities/                   ← Screen controllers (19 files, one class each)
    │   ├── BaseActivity.kt           ← session: SessionManager + navigateTo()
    │   ├── SplashActivity.kt         ← Checks session.isLoggedIn
    │   ├── LoginActivity.kt          ← Calls AuthViewModel, session.saveSession()
    │   ├── RegisterActivity.kt
    │   ├── ForgotPasswordActivity.kt
    │   ├── RoleSelectionActivity.kt  ← Updates role via ProfileViewModel
    │   ├── ProfileActivity.kt        ← Saves profile via ProfileViewModel
    │   ├── DashboardActivity.kt
    │   ├── FeatureActivities.kt      ← PostJobActivity (uses JobViewModel)
    │   └── ... (10 stub activities)
    │
    ├── viewmodels/
    │   ├── AuthViewModel.kt          ← login / register / resetPassword (coroutines)
    │   ├── JobViewModel.kt           ← fetchJobs / createJob
    │   ├── ApplicationViewModel.kt   ← fetchApplications / submitApplication / updateStatus
    │   └── ProfileViewModel.kt       ← fetchProfile / saveProfile
    │
    ├── adapters/                     ← ListAdapter + DiffUtil (4 individual files)
    ├── models/                       ← Local Kotlin data classes (7 individual files)
    └── utils/
        └── SkillMatcher.kt
```

---

## API Endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/auth/register` | ❌ | Create account, returns JWT |
| POST | `/auth/login` | ❌ | Login, returns JWT |
| POST | `/auth/reset-password` | ❌ | Request password reset |
| GET | `/users/me` | ✅ | Get current user profile |
| PUT | `/users/:id` | ✅ | Update profile |
| GET | `/jobs` | ✅ | List all jobs |
| POST | `/jobs` | ✅ | Create job posting |
| DELETE | `/jobs/:id` | ✅ | Delete own job |
| GET | `/applications` | ✅ | My applications (role-based) |
| POST | `/applications` | ✅ | Apply for a job |
| PUT | `/applications/:id/status` | ✅ | Accept/Reject application |
| GET | `/messages/:conversationId` | ✅ | Get chat messages |
| POST | `/messages` | ✅ | Send message |
| GET | `/notifications` | ✅ | My notifications |
| PUT | `/notifications/:id/read` | ✅ | Mark notification as read |

✅ = Requires `Authorization: Bearer <jwt_token>` header

---

## MongoDB Collections (auto-created)

| Collection | Description |
|---|---|
| `users` | All user accounts (freelancers + clients) |
| `jobs` | Job postings created by clients |
| `applications` | Freelancer applications for jobs |
| `messages` | Chat messages between users |
| `reviews` | User reviews and ratings |
| `notifications` | System notifications |

View and manage all data in **MongoDB Compass** → `mongodb://localhost:27017/freelancerconnect`