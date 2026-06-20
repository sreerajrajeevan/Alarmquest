# ⏰ AlarmQuest (Nothing OS-Style ML Alarm)

AlarmQuest is a premium, high-contrast, Nothing OS-styled alarm clock application built with Jetpack Compose. Instead of standard snoozing/dismissing, AlarmQuest leverages on-device Machine Learning (Google ML Kit) to verify physical household objects in real time using the device's camera. Alarms will only silence when the user successfully photographs a designated Quest Object (e.g., a "coffee maker", "sink", or "mirror") with at least an 80% accuracy threshold.

---

## 📥 Direct APK Installation Links

To install **AlarmQuest** directly on your Android mobile device, you can use either of the options below:

### Option 1: Direct Download from GitHub (Recommended)
Once you have exported this project to your GitHub repository, the compiled APK can be downloaded directly using this link pattern:

*   👉 **[📥 Download Latest AlarmQuest APK](https://github.com/SreerajRajeevan7/AlarmQuest/actions)** *(Click here, navigate to the latest green workflow run, and download the `app-debug.apk` artifact from the **Artifacts** section at the bottom.)*
*   👉 Alternatively, if you set up a Release tags trigger: **[📥 Download Latest Release APK](https://github.com/SreerajRajeevan7/AlarmQuest/releases/latest/download/app-debug.apk)**

> *(Note: Replace `SreerajRajeevan7` or the repository name in the URL if your repository URL differs.)*

### Option 2: Download Directly from Google AI Studio Workspace
1. Locate the **Export** menu or build settings button in the top-right toolbar of the Google AI Studio page.
2. Select **Generate APK / Build APK** (or export project files).
3. Scan the generated QR code or download the parsed `.apk` file directly on your mobile device.

---

## ⚙️ How to Enable Automated APK Builds on GitHub
We have included a highly-optimized Github Action workflow in this project. As soon as you push your code to your GitHub repository:
1. GitHub Actions will auto-compile the codebase and build a clean, secure debug APK file.
2. The generated visual APK will be attached as a downloadable build artifact directly on your repository run!

### Steps to Install on Mobile:
1. Download the `app-debug.apk` file on your phone.
2. Open the downloaded file using your phone's File Manager.
3. If prompted, allow installations from "Unknown Sources" (your web browser or file manager) in Android security settings.
4. Press **Install** and launch **AlarmQuest** to begin your wake-up challenges!

---

## 🌟 Visual Theme & Design Spec
*   **Aesthetic Styling**: Pure monochrome blacks (`#000000`), Nothing Dark Grey (`#121212`), high-contrast Pure White elements (`#FFFFFF`), and Nothing Alert Red (`#FF0000`).
*   **Typography**: Styled cleanly around spaced Monospace dot-matrix glyph grids reminiscent of the iconic Nothing glyph fonts.
*   **Edge-to-Edge**: Full support for Android edge-to-edge screens with immersive status bars.
