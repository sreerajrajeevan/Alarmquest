# ⏰ AlarmQuest (Nothing OS-Style ML Alarm)

AlarmQuest is a premium, high-contrast, Nothing OS-styled alarm clock application built with Jetpack Compose. Instead of standard snoozing/dismissing, AlarmQuest leverages on-device Machine Learning (Google ML Kit) to verify physical household objects in real time using the device's camera. Alarms will only silence when the user successfully photographs a designated Quest Object (e.g., a "coffee maker", "sink", or "mirror") with at least an 80% accuracy threshold.

---

## 📥 Direct APK Installation Links

To install **AlarmQuest** directly on your Android mobile device, you can use either of the options below:

### Option 1: Direct Download from GitHub (Requires Exporting to GitHub first)
Once you have **exported** this project to your GitHub repository, the links below will work. Since the repository might be private or not yet fully pushed/configured, make sure to:
1. **Export the Project**: Use the **Export to GitHub** (or **Push to GitHub**) option in Google AI Studio (located in the top-right toolbar or Settings menu) to create your repository under your username.
2. **Ensure correct URLs**: Replace `SreerajRajeevan7` or the repository name `AlarmQuest` in the links below with your actual GitHub username and repository name if they differ:

*   👉 **[📥 View Active GitHub actions & builds](https://github.com/SreerajRajeevan7/AlarmQuest/actions)** *(Go here to find live workflow runs. Once a run finishes, click it and scroll to the bottom to download `app-debug` from the **Artifacts** section.)*
*   👉 **[📥 Direct Download Latest Release APK](https://github.com/SreerajRajeevan7/AlarmQuest/releases/latest/download/app-debug.apk)** *(Only works if you have created a Release tagged on GitHub.)*

> **⚠️ Why did you get a 404 Error?**
> A `404` error occurs because the GitHub repository `https://github.com/SreerajRajeevan7/AlarmQuest` does not exist yet or hasn't had any completed GitHub Actions runs or Releases. Once you use AI Studio to **Push/Export** this project to your GitHub account and let the automated workflow build, this error will disappear!


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
