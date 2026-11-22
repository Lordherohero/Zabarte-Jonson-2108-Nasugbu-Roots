Nasugbu Roots
A heritage tourism app that brings visitors, local guides, and site owners together to explore the history of Nasugbu, Batangas.

What the app does
- Register and log in with email and password (Firebase Authentication), and log out from the menu
- Browse heritage spots on the home screen, filter by category, and search by name
- See each spot's details: category, description, entry fee, availability, guide policy, coordinates, and reviews with an average rating
- Explore an offline map with a pin for every spot, and tap a pin to open that spot
- Book a visit: choose a date (past dates are disabled), 1 to 12 hours, and an optional guide
- Booking rules: a spot cannot be booked twice on the same date, and a guide cannot be booked for two places on the same date
- A booking without a guide is confirmed at once, and a guided booking starts as pending
- My Visits lists bookings as Upcoming, Completed, or Cancelled, and lets you cancel
- Write a review after a confirmed visit has passed, with a 1 to 5 rating, a comment, and an optional guide rating
- Data is stored in Cloud Firestore and updates on screen in real time
  
Guide and site owner accounts can register, and they stay pending until verified. The guide, owner, and manager menus are in the console prototype, not in the Android app yet.

Built with
- Kotlin
- Jetpack Compose with Material 3
- Firebase Authentication and Cloud Firestore
- Mapsforge for the offline map
- Android Studio, Git, and GitHub

How to run
1. Install Android Studio and clone this repository.
2. Put `google-services.json` from the project's Firebase console in the `app` folder.
3. Make sure the offline map file is in `app/src/main/assets`.
4. Choose Sync Project with Gradle Files.
5. Run on an emulator or a phone with USB debugging (Android 7.0 or later). An internet connection is needed for login and data.

Project structure

Package: Contents 
model: TouristSpot, Booking, Review, Guide, Searchable, Enums
data: Repository, UserRepository, BookingRules, ReviewRules, CurrentUser, SampleData, SampleGuides
ui: HomeScreen, DestinationDetailsScreen, BookingFormScreen, MyBookingsScreen, ReviewFormScreen, NasugbuMapScreen, SpotListScreen, DataState
ui.auth: AuthenticationNavigation, AuthenticationModel, login and registration screens

limitations
- Guide, owner, and manager screens are not in the Android app, so guided bookings stay pending
- The entry fee is charged once per booking, not per person
- Saved spots are kept only while the app is open
- Password reset and the Find Tour Guides page are placeholders
- The Firestore database uses development (test mode) rules
