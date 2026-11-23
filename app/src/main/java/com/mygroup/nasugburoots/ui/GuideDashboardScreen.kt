package com.mygroup.nasugburoots.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mygroup.nasugburoots.data.AccountRepository
import com.mygroup.nasugburoots.data.Repository
import com.mygroup.nasugburoots.model.Booking
import com.mygroup.nasugburoots.model.BookingStatus
import com.mygroup.nasugburoots.ui.auth.AccountProfile
import com.mygroup.nasugburoots.ui.theme.Cream
import com.mygroup.nasugburoots.ui.theme.DeepForest
import com.mygroup.nasugburoots.ui.theme.Forest
import com.mygroup.nasugburoots.ui.theme.Muted

@Composable
fun GuideDashboardScreen(
    profile: AccountProfile,
    onLogout: () -> Unit
) {
    val guides = rememberGuides()
    val spots = rememberSpots()
    val allBookings = rememberBookings()

    val myGuide = guides.find { guide -> guide.userId == profile.userId }
    val myBookings = if (myGuide != null) {
        allBookings
            .filter { booking -> booking.guideId == myGuide.guideId }
            .sortedBy { booking -> booking.date }
    } else {
        emptyList()
    }

    var languages by remember(myGuide?.guideId) {
        mutableStateOf(myGuide?.languages ?: "")
    }
    var rateText by remember(myGuide?.guideId) {
        mutableStateOf(if (myGuide != null) myGuide.ratePerHour.toString() else "")
    }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Forest)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Guide dashboard",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = profile.fullName,
                    color = Cream,
                    fontSize = 13.sp
                )
            }

            TextButton(onClick = onLogout) {
                Text(text = "Log out", color = Color.White)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "My guide profile",
                            color = DeepForest,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        if (myGuide == null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your guide profile is not ready yet. Ask a manager to approve your account.",
                                color = Muted,
                                fontSize = 13.sp
                            )
                        } else {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = languages,
                                onValueChange = { languages = it },
                                label = { Text("Languages you speak") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = rateText,
                                onValueChange = { rateText = it },
                                label = { Text("Rate per hour (PHP)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    val rate = rateText.toDoubleOrNull()
                                    if (languages.isBlank() || rate == null || rate < 0.0) {
                                        message = "Enter your languages and a valid rate."
                                    } else {
                                        AccountRepository.updateGuideProfile(
                                            myGuide.guideId,
                                            languages.trim(),
                                            rate
                                        )
                                        message = "Profile saved."
                                    }
                                }
                            ) {
                                Text("Save profile")
                            }

                            if (message.isNotEmpty()) {
                                Text(text = message, color = Muted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Booking requests",
                    color = DeepForest,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Serif
                )
            }

            if (myBookings.isEmpty()) {
                item {
                    Text(text = "No booking requests yet.", color = Muted, fontSize = 13.sp)
                }
            } else {
                items(myBookings) { booking ->
                    val spot = spots.find { it.placeId == booking.placeId }
                    val spotName = if (spot != null) spot.placeName else "Unknown spot"
                    GuideBookingCard(booking = booking, spotName = spotName)
                }
            }
        }
    }
}

@Composable
private fun GuideBookingCard(
    booking: Booking,
    spotName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = spotName,
                color = DeepForest,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Date: ${booking.date}")
            Text(text = "Duration: ${booking.hours} hour(s)")
            Text(text = "Visitors: ${booking.visitors}")
            Text(text = "Your fee: PHP ${booking.guideRate * booking.hours}")
            Text(text = "Status: ${booking.status.label}", fontWeight = FontWeight.SemiBold)

            if (booking.status == BookingStatus.PENDING) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            Repository.updateBookingStatus(booking.bookingId, BookingStatus.CONFIRMED)
                        }
                    ) {
                        Text("Confirm")
                    }

                    OutlinedButton(
                        onClick = {
                            Repository.updateBookingStatus(booking.bookingId, BookingStatus.CANCELLED)
                        }
                    ) {
                        Text("Decline")
                    }
                }
            } else if (booking.status == BookingStatus.CONFIRMED) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        Repository.updateBookingStatus(booking.bookingId, BookingStatus.CANCELLED)
                    }
                ) {
                    Text("Cancel booking")
                }
            }
        }
    }
}