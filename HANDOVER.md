# Optimeter - Utility Metrics Tracker App

## 📱 Project Overview
Optimeter is a native Android application built using Kotlin and **Jetpack Compose**. The core purpose of the app is to allow users to track their utility consumption (Electricity, Gas, Water) efficiently.

This project was recently translated from a React-based Figma AI mockup to a fully functional Jetpack Compose Android UI, maintaining strict adherence to the defined React/Tailwind CSS design tokens, colors, layouts, and font hierarchies.

## 🚀 Current State (What's Done)
The application currently functions as a highly polished UI framework with placeholder navigation and localized state management.

### Implemented Features:
1.  **Home Dashboard (`HomeTab.kt`)**: 
    - Custom `<select>` dropdown behavior translated to Material 3 `ExposedDropdownMenuBox` for switching between user properties ("My Home").
    - "Quick Actions" card to immediately trigger the barcode/camera scanner.
    - Customized `MeterCard` components displaying historical electricity, gas, and water usage with matching CSS `oklch` color spaces across both Light and Dark modes.
2.  **Analytics / Statistics (`StatisticsTab.kt`)**: 
    - Custom segmented tab selector (resembling React toggle buttons) to flip between utility graphs.
    - Total, Average, and percentage Trend cards tracking consumption relative to previous periods.
    - Integrated with **Vico** charts for a smooth area graph mapping the mock array data.
    - Vertical scrolling fixes applied for proper device exploration.
3.  **Add Reading / Camera (`AddReadingTab.kt`)**: 
    - Fully wired `ActivityResultContracts` to launch the device's native **Camera**.
    - Fully wired `ActivityResultContracts` to launch the local **Photo Picker/Gallery** for uploading existing photos.
    - Integrated **Coil** for asynchronous circular-cropped image previews straight from the `Uri`/`Bitmap`.
    - Dropdowns to select `MeterType` and an unselected transparent bordered input array for manual reading entry.
4.  **Settings & Management (`SettingsTab.kt`)**: 
    - Property management block ("My Homes") using interactive states and semantic color schemes for dynamic visibility (handling the red delete trash icon perfectly on Dark Mode).
    - Fully functioning native `AlertDialog` wired to the "Add Home" button to capture new home name and street address logic.
    - Standard Theme and Configuration switches.
5.  **Bottom Navigation & Global Theming (`BottomNavBar.kt`, `Theme.kt`, `Color.kt`, `Type.kt`)**: 
    - Defeated the default Android 12+ "Material You" `dynamicColor` engine, enforcing the strict brand CSS color space against wallpaper interference.
    - Disabled the bulk Material 3 pill highlighter on the bottom bar and forced an explicit contrast token (`#9E9E9E`) for unmatched inactive visibility on both Dark/Light modes.
    - Extracted the React `.css` system variables to rebuild standard Tailwind HTML Typography classes (`h1-h4`, `label`, `p`, etc.) identically into Material 3 `TextStyle` arrays mapping to Jetpack's clean `FontFamily.SansSerif`.

## 🛠 Tech Stack
-   **Language:** Kotlin
-   **UI Toolkit:** Jetpack Compose (Material 3)
-   **Architecture Scheme:** Clean Architecture / MVVM Pattern preparations
-   **Dependency Injection:** Hilt (Included in Gradle, awaiting implementation)
-   **Image Loading:** Coil
-   **Charts:** Vico
-   **Local Storage Prep:** DataStore Preferences
-   **Cloud/ML Prep:** Firebase (Auth, Firestore, Storage) / ML Kit Text Recognition

## 📈 Next Steps (Handover Plan)
To turn this pixel-perfect UI prototype into a production application, the next developer should focus heavily on the background architecture and data persistence layers.

### Phase 1: Local State to ViewModel Migration
-   Currently, all tabs (`HomeTab`, `SettingsTab`, `StatisticsTab`, etc.) use localized React-style Compose state closures (e.g., `var expanded by remember { mutableStateOf(false) }`). 
-   **Task:** Scaffold `HomeViewModel`, `AddReadingViewModel`, etc. using Dagger Hilt.
-   **Task:** Extract `consumptionData` arrays, `MeterType` enums, and currently mocked read-strings into a `StateFlow` pushed from the ViewModels to drive the UI reactively.

### Phase 2: Database / Persistence Layer Setup
-   Initialize Room DB (if fully local) or tie up the already listed Gradle dependency for **Firebase Firestore**.
-   **Task:** Connect the `showAddHomeDialog` logic inside `SettingsTab` to actually push the `newHomeName` and `newHomeAddress` strings to a database so it saves context between sessions.
-   **Task:** Tie the `meterTypes` arrays in `HomeTab` and `StatisticsTab` to fetch historical readings from a DB mapping the currently selected active "Home" UID.

### Phase 3: OCR & Machine Learning Execution
-   The Gradle file imports `com.google.mlkit:text-recognition:16.0.0`. 
-   **Task:** Hook the `.launch()` result outputs from `cameraLauncher` and `galleryLauncher` inside `AddReadingTab.kt` to pipe the resulting `Bitmap` or `Uri` directly into ML Kit's `TextRecognizer`.
-   **Task:** Use Regex or positional OCR to parse raw numbers from the meter display text, automatically overwriting the `readingValue` `OutlinedTextField` string instead of the current mocked demo strings (`"1250"`, `"450"`).

### Phase 4: Navigation Flow
-   Replace the enum-driven local State navigation in `DashboardScreen.kt` with a formal Jetpack Compose `NavHost` architecture for deeper routing (e.g., clicking on a historical graph item in `StatisticsTab` pushing onto a new detail inspection Screen).

## 🗃 Build Instructions
The project is built against Android SDK 35 and Java 1.8 compatibility using Gradle 9.x. 

1. Ensure standard Android Studio Jellyfish/Koala installed (`jbr` Java runtime).
2. Clean rebuild to sync Jetpack Compose BOM 2024.06.00 and Vico 1.14.0.
3. Target device must support minimum Android SDK 26 (Android 8.0 Oreo).
