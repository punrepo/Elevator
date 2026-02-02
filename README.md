# Skyline Elevators
Skyline Elevators is a smart building solution designed to modernize vertical transportation. This application provides a high-performance, real-time control interface for elevator systems, prioritizing data transparency and operational safety.

# The Problem - solved here
In large-scale buildings, elevator usage is often a "black box" for management. This project enables real-time tracking and auditing of elevator usage by logging precise request timestamps and floor data to the cloud for operational analysis.

## Key Features
Reactive Dashboard: A declarative UI built for instant feedback on floor selection.

Safety Interlocks: Logical state management prevents elevator movement when the "Hold Door" safety mode is active.

Cloud Telemetry: Every floor request is logged with a high-precision timestamp (java.util.Date) and synced to a NoSQL cloud database.

Hardware Optimized: Native performance deployed directly to physical Samsung Galaxy hardware.

## Tech Stack
Language: Kotlin

UI Framework: Jetpack Compose

Backend/Cloud: Firebase Realtime Database

IDE: Android Studio (optimized for Apple Silicon M4 Pro)

Utilities: Java/Android Time APIs (SimpleDateFormat, java.util.Date)

## Installation & Setup
1. Prerequisites
   An Android device (Samsung Galaxy or similar) with USB Debugging enabled.

Android Studio Ladybug or newer.

A google-services.json file from your Firebase console.

2. Building from Source
   Clone the Repository:

Bash
git clone https://github.com/punrepo/Elevator.git
Add Configuration: Place your google-services.json into the app/ directory.

Sync Gradle: Open in Android Studio and click "Sync Project with Gradle Files."

Deploy: Select your physical device from the toolbar and click the Green Play Button (▶️).

3. Sideloading the APK
   If you just want to run the app without the source code:

visit https://i.diawi.com/xJ5DQ9

Download the app-debug.apk from the [Download/Releases] section.

Transfer the file to your Samsung phone.

Open the file on your phone and select "Install" (Allow "Install from Unknown Sources" if prompted).

## Data Structure
The app logs data to Firebase in the following JSON format:

JSON
{
"elevator_logs": {
"-Nxyz123": {
"device": "Samsung_Galaxy",
"floor": 7,
"time": "2026-02-01 14:30:05"
}
}
}

## 📱 Interface & Live Demo 

<table width="100%"> 
<tr> <th width="50%">📸 System UI</th> <th width="50%">🎬 Live Functionality</th> </tr>
 <tr> <td align="center"> 
<img src="https://github.com/user-attachments/assets/ca8bbfba-e892-452f-9aff-23847a9f5e8f" width="100%" alt="App Screenshot"> </td> <td align="center"> 
<video src="https://github.com/user-attachments/assets/595d3ad3-5c32-4bb8-97b5-b53292e34d98" width="100%" controls>
</video> </td> </tr> </table>

**Note:** The demo above showcases real-time floor selection and the "Hold Door" safety logic integrated with Firebase cloud logging.

