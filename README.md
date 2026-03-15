# SnapBudget

> **Snap. Extract. Save. Zero typing required.**

An intelligent Android expense tracker that scans receipts using OCR and automatically categorizes your expenses — transforming a 10-minute chore into a 2-second habit.

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Min SDK](https://img.shields.io/badge/Min_SDK-24_(Android_7.0)-blue?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-lightgrey?style=flat-square)

---

## Digitization Pipeline

![LabTrack Demo](images/pipeline.jpeg)

---

## App Preview

<p align="center">
  <img src="images/1.jpeg" width="200">
   
   <img src="images/6.jpeg" width="200">
   
  <img src="images/4.jpeg" width="200">
   
  <img src="images/5.jpeg" width="200">
</p>

---

## Features

![OCR](https://img.shields.io/badge/-Receipt_Scanning-0A0A0A?style=flat-square&logo=google&logoColor=white)
Capture via camera or upload from gallery using Google ML Kit on-device OCR.

![Extraction](https://img.shields.io/badge/-Smart_Extraction-0A0A0A?style=flat-square&logo=databricks&logoColor=white)
Auto-detects merchant name, total amount (₹), date, GST number, and line items.

![AI](https://img.shields.io/badge/-Auto_Categorization-0A0A0A?style=flat-square&logo=openai&logoColor=white)
Classifies expenses into Food, Travel, Shopping, Utilities, Health, and more.

![Confidence](https://img.shields.io/badge/-Confidence_Scoring-0A0A0A?style=flat-square&logo=checkmarx&logoColor=white)
Field-level reliability scores with cross-validation and OCR error correction.

![Offline](https://img.shields.io/badge/-Offline_First-0A0A0A?style=flat-square&logo=sqlite&logoColor=white)
Fully functional without internet; all data stored locally via Room (SQLite).

![Dashboard](https://img.shields.io/badge/-Dashboard_%26_Reports-0A0A0A?style=flat-square&logo=chartdotjs&logoColor=white)
Monthly totals, category-wise breakdown, and interactive pie chart visualizations.

---

## Key Metrics

| Metric | Value | Notes |
|---|---|---|
| Processing Speed | **< 2 seconds** | Time to process receipts and transactions |
| Extraction Accuracy | **> 95%** | Data extraction and categorization accuracy |
| Offline Capability | **90%** | Tasks performed without internet connection |
| Weekly Time Saved | **15–20 min/user** | Per user, weekly |
| Server Cost | **₹0** | Local-first architecture |

---

## Tech Stack

| Layer | Technology |
|---|---|
| ![](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white) | Kotlin |
| ![](https://img.shields.io/badge/Architecture-MVVM-blue?style=flat-square) | Model-View-ViewModel |
| ![](https://img.shields.io/badge/OCR-ML_Kit-4285F4?style=flat-square&logo=google&logoColor=white) | Google ML Kit Text Recognition |
| ![](https://img.shields.io/badge/Camera-CameraX-4285F4?style=flat-square&logo=google&logoColor=white) | Jetpack CameraX |
| ![](https://img.shields.io/badge/Database-Room_SQLite-003B57?style=flat-square&logo=sqlite&logoColor=white) | Room (SQLite) |
| ![](https://img.shields.io/badge/Charts-MPAndroidChart-FF6600?style=flat-square) | MPAndroidChart |
| ![](https://img.shields.io/badge/UI-Material_Design_3-757575?style=flat-square&logo=materialdesign&logoColor=white) | Material Design 3 |

---

## Getting Started

### Prerequisites

![Android Studio](https://img.shields.io/badge/Android_Studio-Hedgehog+-3DDC84?style=flat-square&logo=androidstudio&logoColor=white)
![JDK](https://img.shields.io/badge/JDK-17-007396?style=flat-square&logo=openjdk&logoColor=white)
![SDK](https://img.shields.io/badge/Android_SDK-34-3DDC84?style=flat-square&logo=android&logoColor=white)

```bash
git clone https://github.com/your-username/snapbudget-ocr
# Open in Android Studio → Sync Gradle → Run on device or emulator (API 24+)
```

**Required permissions:** `CAMERA` · `READ_MEDIA_IMAGES` 

---

