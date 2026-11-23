package com.mygroup.nasugburoots.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.mygroup.nasugburoots.model.Guide

data class PendingAccount(
    val userId: String,
    val fullName: String,
    val email: String,
    val role: String,
    val details: String
)

object AccountRepository {

    private const val TAG = "AccountRepository"

    private val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    fun listenToPendingAccounts(onChange: (List<PendingAccount>) -> Unit): ListenerRegistration {
        return db.collection("users")
            .whereEqualTo("status", "PENDING_VERIFICATION")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e(TAG, "Pending accounts listener failed", error)
                    return@addSnapshotListener
                }
                val accounts = snapshot.documents.map { doc ->
                    val role = doc.getString("role") ?: ""
                    val details = if (role == "LOCAL_GUIDE") {
                        "Location: ${doc.getString("location") ?: ""}. ${doc.getString("biography") ?: ""}"
                    } else {
                        "Establishment: ${doc.getString("establishmentName") ?: ""}, ${doc.getString("siteAddress") ?: ""}"
                    }
                    PendingAccount(
                        userId = doc.id,
                        fullName = doc.getString("fullName") ?: "",
                        email = doc.getString("email") ?: "",
                        role = role,
                        details = details
                    )
                }
                onChange(accounts)
            }
    }

    fun approveAccount(account: PendingAccount, nextGuideId: Int) {
        val batch = db.batch()
        batch.update(db.collection("users").document(account.userId), "status", "ACTIVE")

        if (account.role == "LOCAL_GUIDE") {
            val guide = Guide(
                guideId = nextGuideId,
                name = account.fullName,
                languages = "",
                ratePerHour = 150.0,
                verified = true,
                userId = account.userId
            )
            batch.set(db.collection("guides").document(nextGuideId.toString()), guide)
        }

        batch.commit()
            .addOnFailureListener { Log.e(TAG, "Could not approve account", it) }
    }

    fun rejectAccount(userId: String) {
        db.collection("users").document(userId)
            .update("status", "REJECTED")
            .addOnFailureListener { Log.e(TAG, "Could not reject account", it) }
    }

    fun updateGuideProfile(guideId: Int, languages: String, ratePerHour: Double) {
        db.collection("guides").document(guideId.toString())
            .update("languages", languages, "ratePerHour", ratePerHour)
            .addOnFailureListener { Log.e(TAG, "Could not update guide profile", it) }
    }
}