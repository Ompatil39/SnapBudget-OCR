# SnapBudget OCR

An intelligent Android expense tracker with OCR-based receipt scanning and automatic expense categorization.

## Features

### Receipt Scanning & OCR
- Capture receipts using device camera with real-time preview
- Upload images from gallery
- Powered by Google ML Kit Text Recognition
- Automatic text extraction from receipt images

### Structured Data Extraction
- **Merchant Name**: Intelligent merchant detection from receipt headers
- **Total Amount**: Multi-pattern currency detection (supports ₹, Rs., numeric formats)
- **Transaction Date**: Multiple date format parsing with future date validation
- **GST Number**: Indian GST format validation (15-digit structure)
- **Line Items**: Optional item-level extraction for verification

### Validation & Post-Processing
- **Confidence Scoring**: Field-level confidence scores (0-100%)
- **Currency Validation**: Rupee symbol and numeric pattern matching
- **Date Validation**: Rejects future dates, accepts common Indian formats
- **GST Validation**: Verifies Indian GST structure (State Code + PAN + Entity + Z + Checksum)
- **Cross-verification**: Line item sum vs. total amount comparison
- **Error Correction**: Post-processing for common OCR errors

### Automatic Expense Categorization
Pre-defined categories with intelligent classification:
- Food & Dining (restaurants, cafes, Swiggy, Zomato)
- Travel & Transport (Uber, Ola, fuel, trains, flights)
- Shopping (Big Bazaar, DMart, e-commerce)
- Utilities & Bills (electricity, water, mobile, broadband)
- Entertainment (movies, streaming services)
- Health & Medical (hospitals, pharmacies, clinics)
- Education (schools, courses, books)
- Others (fallback category)

**Classification Logic**:
- Keyword-based matching
- Merchant name pattern recognition
- User-defined rule overrides
- Category suggestions with confidence scores

### Data Storage
- Local SQLite database using Room
- Offline functionality
- Persistent transaction history
- Full CRUD operations (Create, Read, Update, Delete)
- Image persistence for receipt records

### Dashboard & Reporting
- Monthly total expenditure display
- Category-wise expense breakdown
- Pie chart visualization (MPAndroidChart)
- Recent transactions list
- Date range filtering
- Search functionality

## Architecture

### Technology Stack
- **Language**: Kotlin
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Architecture Pattern**: MVVM (Model-View-ViewModel)

### Key Libraries
| Library | Purpose |
|---------|---------|
| CameraX | Modern camera API for receipt capture |
| ML Kit Text Recognition | On-device OCR processing |
| Room | Local database persistence |
| MPAndroidChart | Pie/bar chart visualization |
| Material Design 3 | Modern UI components |

### Module Structure
```
com.snapbudget.ocr/
├── data/
│   ├── model/          # Transaction, Category entities
│   ├── db/             # Room database, DAO, Converters
│   └── repository/     # Data access abstraction
├── ocr/
│   ├── OcrProcessor.kt      # ML Kit integration
│   ├── ReceiptParser.kt     # Text parsing logic
│   └── ReceiptProcessor.kt  # Validation & processing
├── categorization/
│   └── CategoryClassifier.kt # Smart categorization engine
├── ui/
│   ├── dashboard/      # Main dashboard with charts
│   ├── camera/         # Receipt capture
│   ├── receipt/        # Review & edit screen
│   └── history/        # Transaction history & filters
└── util/               # Formatters and helpers
```

## OCR Accuracy Enhancement Strategy

1. **Prebuilt OCR**: Uses Google ML Kit's proven text recognition
2. **Multi-Pattern Extraction**: Multiple regex patterns for each field
3. **Validation Layer**:
   - GST format verification
   - Currency symbol detection
   - Date sanity checks
   - Amount range validation
4. **Cross-Verification**: Line items sum vs. extracted total
5. **Confidence Scoring**: Field-level reliability indicators
6. **Error Display**: Raw OCR shown for user verification

## User Experience

### Design Principles
- **Minimal Steps**: 2-3 steps from capture to save
- **Clean UI**: Material Design 3 components
- **Visual Feedback**: Confidence scores, validation warnings
- **Dark Mode**: Automatic theme switching support
- **Biometric Security**: Optional fingerprint/face unlock

### Workflow
1. **Capture**: Camera or gallery selection
2. **Review**: OCR results with manual edit option
3. **Save**: Automatic categorization and storage

## Building the Project

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34

### Setup
1. Clone the repository
2. Open in Android Studio
3. Sync project with Gradle
4. Run on device or emulator (API 24+)

### Permissions Required
- `CAMERA` - Receipt capture
- `READ_MEDIA_IMAGES` - Gallery access
- `USE_BIOMETRIC` - Optional authentication

## Screenshots

[To be added]

## Future Enhancements

- Cloud sync with Firebase
- Multi-currency support
- Recurring expense detection
- Budget setting and alerts
- Export to PDF/Excel
- Receipt cloud backup
- Advanced analytics with trends

## License

MIT License

## Acknowledgments

- Google ML Kit for on-device OCR
- MPAndroidChart for visualization
- Material Design team for UI components