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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val GuideGold = Color(0xFFF2CA69)
private val LightGreen = Color(0xFFE8F0E9)
private val PageBackground = Color(0xFFFBFAF5)

@Composable
fun GuideDashboardScreen(
    profile: AccountProfile,
    onLogout: () -> Unit
) {
    val guides = rememberGuides()
    val spots = rememberSpots()
    val allBookings = rememberBookings()

    val myGuide = guides.find {
        it.userId == profile.userId
    }

    val myBookings = if (myGuide != null) {
        allBookings
            .filter { it.guideId == myGuide.guideId }
            .sortedBy { it.date }
    } else {
        emptyList()
    }

    val today = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date())
    }

    val activeBookings = myBookings.filter {
        it.status != BookingStatus.CANCELLED
    }

    val pendingCount = myBookings.count {
        it.status == BookingStatus.PENDING
    }

    val confirmedCount = myBookings.count {
        it.status == BookingStatus.CONFIRMED
    }

    val upcomingCount = activeBookings.count {
        it.date >= today
    }

    val totalVisitors = activeBookings.sumOf {
        it.visitors
    }

    var languages by remember(myGuide?.guideId) {
        mutableStateOf(myGuide?.languages ?: "")
    }

    var rateText by remember(myGuide?.guideId) {
        mutableStateOf(
            if (myGuide != null) {
                myGuide.ratePerHour.toString()
            } else {
                ""
            }
        )
    }

    var message by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
    ) {

        // TOP HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Forest)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "NASUGBU ROOTS",
                    color = GuideGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Guide Dashboard",
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
            }

            TextButton(onClick = onLogout) {
                Text(
                    text = "Log out",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 18.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // WELCOME CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DeepForest
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "WELCOME BACK,",
                            color = GuideGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = profile.fullName,
                            color = Color.White,
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (myGuide?.verified == true) {
                                    "✓ VERIFIED TOUR GUIDE"
                                } else {
                                    "PENDING VERIFICATION"
                                },
                                color = if (myGuide?.verified == true) {
                                    GuideGold
                                } else {
                                    Color.White
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Manage your tours and booking requests in one place.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // STATISTICS
            item {
                Text(
                    text = "Your Overview",
                    color = DeepForest,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GuideStatCard(
                        title = "Total Bookings",
                        value = myBookings.size.toString(),
                        modifier = Modifier.weight(1f)
                    )

                    GuideStatCard(
                        title = "Pending",
                        value = pendingCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GuideStatCard(
                        title = "Confirmed",
                        value = confirmedCount.toString(),
                        modifier = Modifier.weight(1f)
                    )

                    GuideStatCard(
                        title = "Visitors",
                        value = totalVisitors.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // UPCOMING TOURS SUMMARY
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = LightGreen
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Upcoming Tours",
                                color = DeepForest,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Your scheduled visits",
                                color = Muted,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = upcomingCount.toString(),
                            color = Forest,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // GUIDE PROFILE
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            text = "My Guide Profile",
                            color = DeepForest,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Keep your information updated for tourists.",
                            color = Muted,
                            fontSize = 12.sp
                        )

                        if (myGuide == null) {
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Your guide profile is not available yet. Please contact the manager.",
                                color = Muted,
                                fontSize = 13.sp
                            )
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = languages,
                                onValueChange = {
                                    languages = it
                                },
                                label = {
                                    Text("Languages you speak")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = rateText,
                                onValueChange = {
                                    rateText = it
                                },
                                label = {
                                    Text("Rate per hour (PHP)")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    val rate = rateText.toDoubleOrNull()

                                    if (languages.isBlank() ||
                                        rate == null ||
                                        rate < 0.0
                                    ) {
                                        message = "Enter valid languages and hourly rate."
                                    } else {
                                        AccountRepository.updateGuideProfile(
                                            myGuide.guideId,
                                            languages.trim(),
                                            rate
                                        )

                                        message = "Profile saved."
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Forest
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Save Profile")
                            }

                            if (message.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = message,
                                    color = Muted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // BOOKING REQUESTS
            item {
                Text(
                    text = "Booking Requests & Tours",
                    color = DeepForest,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )

                Text(
                    text = "Review and manage your assigned bookings.",
                    color = Muted,
                    fontSize = 12.sp
                )
            }

            if (myBookings.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No bookings yet",
                                color = DeepForest,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "New booking requests will appear here.",
                                color = Muted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(myBookings) { booking ->
                    val spot = spots.find {
                        it.placeId == booking.placeId
                    }

                    GuideBookingCard(
                        booking = booking,
                        spotName = spot?.placeName ?: "Unknown spot"
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "NASUGBU ROOTS • HERITAGE TOURISM",
                    modifier = Modifier.fillMaxWidth(),
                    color = Muted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun GuideStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = value,
                color = Forest,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                color = Muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = spotName,
                color = DeepForest,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Date: ${booking.date}", color = Muted)
            Text("Duration: ${booking.hours} hour(s)", color = Muted)
            Text("Visitors: ${booking.visitors}", color = Muted)
            Text(
                text = "Your guide fee: PHP ${
                    "%.2f".format(
                        Locale.US,
                        booking.guideRate * booking.hours
                    )
                }",
                color = Forest,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Status: ${booking.status.label}",
                color = when (booking.status) {
                    BookingStatus.PENDING -> Color(0xFFB7791F)
                    BookingStatus.CONFIRMED -> Forest
                    BookingStatus.CANCELLED -> Color(0xFFB3261E)
                },
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            if (booking.status == BookingStatus.PENDING) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            Repository.updateBookingStatus(
                                booking.bookingId,
                                BookingStatus.CONFIRMED
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Forest
                        )
                    ) {
                        Text("Confirm")
                    }

                    OutlinedButton(
                        onClick = {
                            Repository.updateBookingStatus(
                                booking.bookingId,
                                BookingStatus.CANCELLED
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Decline")
                    }
                }
            } else if (booking.status == BookingStatus.CONFIRMED) {
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        Repository.updateBookingStatus(
                            booking.bookingId,
                            BookingStatus.CANCELLED
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel Booking")
                }
            }
        }
    }
}
