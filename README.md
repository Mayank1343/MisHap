# Mishapp Awareness App

Mishapp Awareness App is an Android application built to spread awareness and allow users to view and share posts related to incidents, safety updates, and general information.
The app uses a simple login flow and displays posts in a feed using modern Android components.

---

## 📱 Tech Stack

* **Language:** Kotlin
* **UI:** XML layouts (View-based UI, not Compose for screens)
* **Architecture:** Fragment-based navigation
* **Components Used:**

  * RecyclerView
  * CardView
  * Navigation Component
  * Firebase (Auth, Firestore, Storage – ready for use)
  * Google Maps (optional feature)

---

## 📂 Project Structure (Important)

```
app/
 ├── java/com/example/mishappawarenessapp/
 │   ├── model/
 │   │   └── Post.kt              # Data model for posts
 │   ├── ui/
 │   │   ├── HomeFragment.kt      # Home feed screen
 │   │   ├── PostAdapter.kt       # RecyclerView adapter
 │   │   ├── LoginActivity.kt     # Login screen
 │   │   ├── MainActivity.kt      # Hosts fragments
 │   │   └── other fragments...
 │
 ├── res/
 │   ├── layout/
 │   │   ├── fragment_home.xml    # Home screen layout
 │   │   ├── item_post.xml        # Single post UI
 │   │   └── activity_login.xml
 │   ├── drawable/
 │   └── values/
 │
 └── AndroidManifest.xml
```

---

## 🧠 How the App Works (Flow)

1. **LoginActivity**

   * App starts here
   * Handles user login (Firebase-ready)

2. **MainActivity**

   * Loads after successful login
   * Hosts fragments

3. **HomeFragment**

   * Displays a feed of posts using `RecyclerView`
   * Uses `PostAdapter` to bind data

4. **PostAdapter**

   * Connects `Post` data model to `item_post.xml`
   * Handles UI rendering of each post card

---

## 🧩 Data Model

Posts are represented using a simple Kotlin data class:

```kotlin
data class Post(
    val username: String,
    val content: String,
    val imageRes: Int?,
    val upvotes: Int,
    val downvotes: Int,
    val timestamp: String
)
```

---

## ▶️ How to Compile and Run the Project

### Prerequisites

* Android Studio (latest stable version)
* Internet connection (for Gradle & Firebase dependencies)
* Android device or emulator (API 24+ recommended)

---

### Steps to Run

1. **Clone or download the project**

   ```
   git clone <repository-url>
   ```

2. **Open in Android Studio**

   * File → Open → Select project folder

3. **Sync Gradle**

   * Wait for Gradle sync to complete
   * If prompted, click **Sync Now**

4. **Run the app**

   * Select a device/emulator
   * Click ▶ Run

---

## ⚠️ Common Setup Notes (Important)

* Make sure **every XML view** has:

  * `android:layout_width`
  * `android:layout_height`

* If build fails:

  * Try **Build → Rebuild Project**
  * Then **File → Invalidate Caches / Restart**

* App uses **View-based UI**, not Jetpack Compose for screens.

---

## 🔐 Firebase Setup (Optional)

Firebase dependencies are already added.
To enable Firebase fully:

1. Create a Firebase project
2. Add `google-services.json` to `/app`
3. Enable Authentication & Firestore
4. Sync project again

---

## 🤝 Team Notes

* Keep layouts **structurally correct** (proper closing tags)
* Avoid mixing Compose UI inside fragments unless planned
* Commit changes frequently
* Use Logcat (`app + error`) to debug crashes

---

## 📌 Current Status

* Login flow: ✅ Working
* Home feed: ✅ Working
* RecyclerView posts: ✅ Stable
* Firebase: 🔄 Ready for integration

---

## 📬 Future Enhancements

* Create post feature
* Live upvote/downvote
* Firebase-backed posts
* Location-based incidents
* Profile screen improvements

---

## 🧑‍💻 Maintained By

Project team – Mishapp Awareness App
