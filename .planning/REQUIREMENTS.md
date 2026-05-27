# Requirements: Android OCR

## 1. Functional Requirements

### 1.1 Document Scanning & OCR
- [x] **REQ-1.1.1:** Real-time text recognition using Google ML Kit.
- [x] **REQ-1.1.2:** Camera integration with live preview and auto-capture capabilities.
- [ ] **REQ-1.1.3:** Support for image selection from the device gallery.
- [x] **REQ-1.1.4:** Visual feedback for detected text (bounding boxes) during scanning.
- [x] **REQ-1.1.5:** Text extraction and basic editing of extracted text.

### 1.2 PDF Generation
- [x] **REQ-1.2.1:** Generation of searchable PDF files from extracted text.
- [x] **REQ-1.2.2:** Support for multi-page PDF documents.
- [x] **REQ-1.2.3:** Standard PDF layout with text wrapping and basic formatting.

### 1.3 Synchronization & Security
- [x] **REQ-1.3.1:** User authentication via Firebase (Email/Password or Social).
- [x] **REQ-1.3.2:** Real-time synchronization of scan history and metadata using Cloud Firestore.
- [x] **REQ-1.3.3:** Secure storage of PDF documents in Firebase Cloud Storage.
- [x] **REQ-1.3.4:** Data isolation: users can only access their own documents.
- [x] **REQ-1.3.5:** Offline-first support: users can scan and edit documents without an internet connection, with sync occurring upon reconnection.

### 1.4 User Interface
- [x] **REQ-1.4.1:** Modern "Bento Grid" home screen for managing recent scans.
- [x] **REQ-1.4.2:** Intuitive scanner interface with clear feedback and controls.
- [x] **REQ-1.4.3:** Support for Material 3 Dynamic Colors.
- [x] **REQ-1.4.4:** Dark mode and light mode support.

## 2. Non-Functional Requirements

### 2.1 Performance
- **NREQ-2.1.1:** OCR analysis should not lag the camera preview.
- **NREQ-2.1.2:** App startup time should be less than 2 seconds.

### 2.2 Reliability
- **NREQ-2.2.1:** Background sync should be resilient to network failures (resumable uploads).

### 2.3 Scalability
- **NREQ-2.3.1:** The Firestore schema should support efficient querying as the number of scans grows.

## 3. Constraints
- **CON-3.1:** Minimum Android API level 24 (Nougat).
- **CON-3.2:** Compliance with Android Scoped Storage requirements.
