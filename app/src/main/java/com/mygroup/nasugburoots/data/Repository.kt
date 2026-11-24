
package com.mygroup.nasugburoots.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.mygroup.nasugburoots.model.Booking
import com.mygroup.nasugburoots.model.Review
import com.mygroup.nasugburoots.model.TouristSpot
import com.mygroup.nasugburoots.model.Guide
import com.mygroup.nasugburoots.model.BookingStatus

object Repository {

    private const val TAG = "Repository"

    private var seedStarted = false

    private val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    // --------------------------------------------------
    // SEED SAMPLE DATA
    // --------------------------------------------------

    fun seedIfEmpty() {
        if (seedStarted) return

        seedStarted = true

        db.collection("spots")
            .get()
            .addOnSuccessListener { snapshot ->

                val existingSpotIds = snapshot.documents
                    .map { it.id }
                    .toSet()

                val batch = db.batch()
                var addedCount = 0

                sampleSpots.forEach { spot ->

                    val documentId = spot.placeId.toString()

                    if (documentId !in existingSpotIds) {
                        val document = db.collection("spots")
                            .document(documentId)

                        batch.set(document, spot)
                        addedCount++
                    }
                }

                if (addedCount == 0) {
                    Log.d(TAG, "Sample spots already exist.")
                    return@addOnSuccessListener
                }

                batch.commit()
                    .addOnSuccessListener {
                        Log.d(
                            TAG,
                            "Successfully added $addedCount sample spots."
                        )
                    }
                    .addOnFailureListener { error ->
                        Log.e(
                            TAG,
                            "Failed to seed sample spots.",
                            error
                        )
                        seedStarted = false
                    }
            }
            .addOnFailureListener { error ->
                Log.e(
                    TAG,
                    "Failed to check existing spots.",
                    error
                )
                seedStarted = false
            }
    }

    // --------------------------------------------------
    // ASSIGN SAMPLE SPOTS TO SITE OWNER
    // --------------------------------------------------

    fun assignSampleSpotsToOwner(
        ownerUid: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        if (ownerUid.isBlank()) {
            onComplete(false, "Invalid owner account.")
            return
        }

        val spotIds = listOf("1", "2", "3", "4", "5")

        db.collection("spots")
            .whereIn("placeId", listOf(1, 2, 3, 4, 5))
            .get()
            .addOnSuccessListener { snapshot ->

                val documents = snapshot.documents

                if (documents.size != 5) {
                    onComplete(
                        false,
                        "Some sample spots are missing. No changes were made."
                    )
                    return@addOnSuccessListener
                }

                val invalidSpot = documents.any { document ->

                    val currentOwnerId =
                        document.getLong("ownerId")?.toInt()

                    val currentOwnerUid =
                        document.getString("ownerUid").orEmpty()

                    document.id !in spotIds ||
                            currentOwnerId != 4 ||
                            (
                                    currentOwnerUid.isNotBlank() &&
                                            currentOwnerUid != ownerUid
                                    )
                }

                if (invalidSpot) {
                    onComplete(
                        false,
                        "Ownership check failed. No changes were made."
                    )
                    return@addOnSuccessListener
                }

                val batch = db.batch()

                documents.forEach { document ->
                    batch.update(
                        document.reference,
                        "ownerUid",
                        ownerUid
                    )
                }

                batch.commit()
                    .addOnSuccessListener {
                        Log.d(
                            TAG,
                            "Sample spot ownership updated."
                        )

                        onComplete(
                            true,
                            "All five sample spots are now assigned to your account."
                        )
                    }
                    .addOnFailureListener { error ->
                        Log.e(
                            TAG,
                            "Ownership update failed.",
                            error
                        )

                        onComplete(
                            false,
                            "Failed to update spot ownership."
                        )
                    }
            }
            .addOnFailureListener { error ->
                Log.e(
                    TAG,
                    "Failed to check sample spot ownership.",
                    error
                )

                onComplete(
                    false,
                    "Could not access the spots collection."
                )
            }
    }

    // --------------------------------------------------
    // TOURIST SPOTS
    // --------------------------------------------------

    fun listenToSpots(
        onChange: (List<TouristSpot>) -> Unit
    ): ListenerRegistration {

        return db.collection("spots")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e(TAG, "Error listening to spots.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {

                    val spots = snapshot.documents.mapNotNull { document ->
                        document.toObject(TouristSpot::class.java)
                    }

                    onChange(
                        spots.sortedBy { it.placeId }
                    )
                }
            }
    }

    // --------------------------------------------------
    // UPDATE EXISTING TOURIST SPOT
    // --------------------------------------------------

    fun updateTouristSpot(
        spot: TouristSpot,
        ownerUid: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        if (ownerUid.isBlank()) {
            onComplete(false, "Invalid owner account.")
            return
        }

        if (spot.placeName.isBlank()) {
            onComplete(false, "Site name cannot be empty.")
            return
        }

        if (spot.entryFee < 0.0) {
            onComplete(false, "Entry fee cannot be negative.")
            return
        }

        val document = db.collection("spots")
            .document(spot.placeId.toString())

        document.get()
            .addOnSuccessListener { snapshot ->

                if (!snapshot.exists()) {
                    onComplete(false, "Heritage site not found.")
                    return@addOnSuccessListener
                }

                val currentOwnerUid =
                    snapshot.getString("ownerUid").orEmpty()

                if (currentOwnerUid != ownerUid) {
                    onComplete(
                        false,
                        "You are not authorized to edit this site."
                    )
                    return@addOnSuccessListener
                }

                val updates = mapOf(
                    "placeName" to spot.placeName,
                    "description" to spot.description,
                    "category" to spot.category,
                    "locationAddress" to spot.locationAddress,
                    "entryFee" to spot.entryFee,
                    "allowsGuides" to spot.allowsGuides,
                    "availability" to spot.availability
                )

                document.update(updates)
                    .addOnSuccessListener {
                        Log.d(
                            TAG,
                            "Heritage site successfully updated."
                        )

                        onComplete(
                            true,
                            "Heritage site updated successfully."
                        )
                    }
                    .addOnFailureListener { error ->
                        Log.e(
                            TAG,
                            "Failed to update heritage site.",
                            error
                        )

                        onComplete(
                            false,
                            "Failed to save changes."
                        )
                    }
            }
            .addOnFailureListener { error ->
                Log.e(
                    TAG,
                    "Failed to verify site ownership.",
                    error
                )

                onComplete(
                    false,
                    "Could not verify site ownership."
                )
            }
    }

    // --------------------------------------------------
    // GUIDES
    // --------------------------------------------------

    fun listenToGuides(
        onChange: (List<Guide>) -> Unit
    ): ListenerRegistration {

        return db.collection("guides")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e(TAG, "Error listening to guides.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {

                    val guides = snapshot.documents.mapNotNull { document ->
                        document.toObject(Guide::class.java)
                    }

                    onChange(
                        guides.sortedBy { it.guideId }
                    )
                }
            }
    }

    // --------------------------------------------------
    // BOOKINGS
    // --------------------------------------------------

    fun listenToBookings(
        onChange: (List<Booking>) -> Unit
    ): ListenerRegistration {

        return db.collection("bookings")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e(TAG, "Error listening to bookings.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {

                    val bookings = snapshot.documents.mapNotNull { document ->
                        document.toObject(Booking::class.java)
                            ?.copy(bookingId = document.id)
                    }

                    onChange(bookings)
                }
            }
    }

    fun addBooking(booking: Booking) {

        val documentId = if (booking.bookingId.isBlank()) {
            db.collection("bookings").document().id
        } else {
            booking.bookingId
        }

        db.collection("bookings")
            .document(documentId)
            .set(booking.copy(bookingId = documentId))
            .addOnSuccessListener {
                Log.d(TAG, "Booking successfully added.")
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Failed to add booking.", error)
            }
    }

    fun updateBookingStatus(
        bookingId: String,
        status: BookingStatus
    ) {

        db.collection("bookings")
            .document(bookingId)
            .update("status", status.name)
            .addOnSuccessListener {
                Log.d(TAG, "Booking status successfully updated.")
            }
            .addOnFailureListener { error ->
                Log.e(
                    TAG,
                    "Failed to update booking status.",
                    error
                )
            }
    }

    // --------------------------------------------------
    // REVIEWS
    // --------------------------------------------------

    fun listenToReviews(
        onChange: (List<Review>) -> Unit
    ): ListenerRegistration {

        return db.collection("reviews")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e(TAG, "Error listening to reviews.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {

                    val reviews = snapshot.documents.mapNotNull { document ->
                        document.toObject(Review::class.java)
                            ?.copy(reviewId = document.id)
                    }

                    onChange(reviews)
                }
            }
    }

    fun addReview(review: Review) {

        val documentId = if (review.reviewId.toString().isBlank()) {
            db.collection("reviews").document().id
        } else {
            review.reviewId.toString()
        }

        db.collection("reviews")
            .document(documentId)
            .set(review)
            .addOnSuccessListener {
                Log.d(TAG, "Review successfully added.")
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Failed to add review.", error)
            }
    }
}
