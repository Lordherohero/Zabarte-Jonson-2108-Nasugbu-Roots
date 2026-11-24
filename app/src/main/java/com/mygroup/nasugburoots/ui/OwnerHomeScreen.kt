
package com.mygroup.nasugburoots.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mygroup.nasugburoots.data.Repository
import com.mygroup.nasugburoots.model.TouristSpot
import com.mygroup.nasugburoots.ui.auth.AccountProfile
import com.mygroup.nasugburoots.ui.theme.Cream
import com.mygroup.nasugburoots.ui.theme.DeepForest
import com.mygroup.nasugburoots.ui.theme.Muted
import kotlinx.coroutines.launch

private val OwnerGold = Color(0xFFF2CA69)
private val OwnerDarkGreen = Color(0xFF193E2E)
private val OwnerLightGreen = Color(0xFFE8F0E9)
private val OwnerBorder = Color(0xFFE6E8E0)
private val OwnerText = Color(0xFF19362D)
private val OwnerWhite = Color(0xFFFFFFFF)

@Composable
fun OwnerHomeScreen(
    profile: AccountProfile,
    onLogout: () -> Unit
) {
    var ownedSpots by remember {
        mutableStateOf<List<TouristSpot>>(emptyList())
    }
    var isLoading by remember { mutableStateOf(true) }
    var isAssigning by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var selectedSection by remember { mutableStateOf("Dashboard") }

    var selectedSpot by remember {
        mutableStateOf<TouristSpot?>(null)
    }
    var showDetails by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    DisposableEffect(profile.userId) {
        val registration = Repository.listenToSpots { allSpots ->
            ownedSpots = allSpots.filter {
                it.ownerUid == profile.userId
            }
            isLoading = false
        }

        onDispose {
            registration.remove()
        }
    }

    val assignSites: () -> Unit = {
        if (!isAssigning) {
            isAssigning = true
            message = ""

            Repository.assignSampleSpotsToOwner(
                ownerUid = profile.userId
            ) { _, resultMessage ->
                isAssigning = false
                message = resultMessage
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Cream
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        "NASUGBU ROOTS",
                        color = DeepForest,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Site Owner Portal",
                        color = Muted,
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(24.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(OwnerLightGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                profile.fullName.firstOrNull()
                                    ?.uppercase() ?: "O",
                                color = DeepForest,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                profile.fullName,
                                color = OwnerText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Site Owner",
                                color = Muted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                HorizontalDivider(color = OwnerBorder)

                Spacer(Modifier.height(12.dp))

                listOf("Dashboard", "My Heritage Sites").forEach { item ->
                    NavigationDrawerItem(
                        label = {
                            Text(
                                item,
                                fontSize = 14.sp,
                                fontWeight = if (selectedSection == item) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        },
                        selected = selectedSection == item,
                        onClick = {
                            selectedSection = item
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 3.dp
                        ),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = OwnerLightGreen,
                            selectedTextColor = DeepForest,
                            unselectedTextColor = OwnerText
                        )
                    )
                }

                Spacer(Modifier.weight(1f))
                HorizontalDivider(color = OwnerBorder)

                NavigationDrawerItem(
                    label = {
                        Text(
                            "Logout",
                            color = Color(0xFFB54747),
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    selected = false,
                    onClick = onLogout,
                    modifier = Modifier.padding(12.dp)
                )

                Spacer(Modifier.height(12.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OwnerWhite)
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "☰",
                    color = DeepForest,
                    fontSize = 25.sp,
                    modifier = Modifier.clickable {
                        scope.launch { drawerState.open() }
                    }
                )

                Spacer(Modifier.width(14.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        "NASUGBU ROOTS",
                        color = DeepForest,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        selectedSection,
                        color = Muted,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(OwnerLightGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        profile.fullName.firstOrNull()
                            ?.uppercase() ?: "O",
                        color = DeepForest,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = OwnerBorder)

            if (selectedSection == "My Heritage Sites") {
                OwnerHeritageSitesContent(
                    ownedSpots = ownedSpots,
                    isLoading = isLoading,
                    isAssigning = isAssigning,
                    message = message,
                    onAssignSites = assignSites,
                    onView = {
                        selectedSpot = it
                        showDetails = true
                    },
                    onEdit = {
                        selectedSpot = it
                        showEdit = true
                    }
                )
            } else {
                OwnerDashboardContent(
                    profile = profile,
                    ownedSpots = ownedSpots,
                    isLoading = isLoading,
                    isAssigning = isAssigning,
                    message = message,
                    onAssignSites = assignSites,
                    onView = {
                        selectedSpot = it
                        showDetails = true
                    },
                    onEdit = {
                        selectedSpot = it
                        showEdit = true
                    }
                )
            }
        }
    }

    selectedSpot?.let { spot ->
        if (showDetails) {
            OwnerSpotDetailsDialog(
                spot = spot,
                onDismiss = {
                    showDetails = false
                    selectedSpot = null
                },
                onEdit = {
                    showDetails = false
                    showEdit = true
                }
            )
        }

        if (showEdit) {
            OwnerEditSpotDialog(
                spot = spot,
                ownerUid = profile.userId,
                onDismiss = {
                    showEdit = false
                    selectedSpot = null
                },
                onSaved = { resultMessage ->
                    message = resultMessage
                    showEdit = false
                    selectedSpot = null
                }
            )
        }
    }
}

@Composable
private fun OwnerDashboardContent(
    profile: AccountProfile,
    ownedSpots: List<TouristSpot>,
    isLoading: Boolean,
    isAssigning: Boolean,
    message: String,
    onAssignSites: () -> Unit,
    onView: (TouristSpot) -> Unit,
    onEdit: (TouristSpot) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("Welcome back,", color = Muted, fontSize = 15.sp)

        Text(
            profile.fullName,
            color = DeepForest,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(6.dp))

        Text(
            "Here's what's happening with your heritage sites.",
            color = Muted,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(22.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DeepForest)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(DeepForest, OwnerDarkGreen)
                        )
                    )
                    .padding(22.dp)
            ) {
                Text(
                    "YOUR HERITAGE, YOUR STORY",
                    color = OwnerGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "Preserve the beauty of Nasugbu.",
                    color = Cream,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Manage your heritage sites and help visitors discover local stories.",
                    color = Cream.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Dashboard Overview",
            color = DeepForest,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(14.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OwnerStatCard(
                "My Sites",
                if (isLoading) "—" else ownedSpots.size.toString(),
                Modifier.weight(1f)
            )
            OwnerStatCard(
                "Available",
                if (isLoading) "—" else ownedSpots.count {
                    it.availability
                }.toString(),
                Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OwnerStatCard(
                "Verified",
                if (isLoading) "—" else ownedSpots.count {
                    it.verified
                }.toString(),
                Modifier.weight(1f)
            )
            OwnerStatCard(
                "Unavailable",
                if (isLoading) "—" else ownedSpots.count {
                    !it.availability
                }.toString(),
                Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(26.dp))

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "My Heritage Sites",
                color = DeepForest,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text("${ownedSpots.size} sites", color = Muted, fontSize = 12.sp)
        }

        Spacer(Modifier.height(14.dp))

        OwnerSitesList(
            ownedSpots,
            isLoading,
            isAssigning,
            message,
            onAssignSites,
            onView,
            onEdit
        )

        Spacer(Modifier.height(26.dp))

        Text(
            "Account Information",
            color = DeepForest,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = OwnerWhite)
        ) {
            Column(Modifier.padding(18.dp)) {
                OwnerInfoRow("Name", profile.fullName)
                HorizontalDivider(
                    Modifier.padding(vertical = 10.dp),
                    color = OwnerBorder
                )
                OwnerInfoRow("Email", profile.email)
                HorizontalDivider(
                    Modifier.padding(vertical = 10.dp),
                    color = OwnerBorder
                )
                OwnerInfoRow("Role", "SITE OWNER")
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun OwnerHeritageSitesContent(
    ownedSpots: List<TouristSpot>,
    isLoading: Boolean,
    isAssigning: Boolean,
    message: String,
    onAssignSites: () -> Unit,
    onView: (TouristSpot) -> Unit,
    onEdit: (TouristSpot) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            "My Heritage Sites",
            color = DeepForest,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(6.dp))

        Text(
            "View and manage the heritage locations assigned to your account.",
            color = Muted,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(20.dp))

        OwnerSitesList(
            ownedSpots,
            isLoading,
            isAssigning,
            message,
            onAssignSites,
            onView,
            onEdit
        )
    }
}

@Composable
private fun OwnerSitesList(
    ownedSpots: List<TouristSpot>,
    isLoading: Boolean,
    isAssigning: Boolean,
    message: String,
    onAssignSites: () -> Unit,
    onView: (TouristSpot) -> Unit,
    onEdit: (TouristSpot) -> Unit
) {
    if (isLoading) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = DeepForest)
            Spacer(Modifier.height(12.dp))
            Text("Loading your heritage sites...", color = Muted, fontSize = 13.sp)
        }
    } else if (ownedSpots.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = OwnerWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    "No heritage sites assigned yet.",
                    color = DeepForest,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "You can assign the five existing sample sites to your account.",
                    color = Muted,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onAssignSites,
                    enabled = !isAssigning,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepForest
                    )
                ) {
                    if (isAssigning) {
                        CircularProgressIndicator(
                            Modifier.size(18.dp),
                            color = Cream,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Assigning sites...")
                    } else {
                        Text("Assign My 5 Sample Sites")
                    }
                }
            }
        }
    } else {
        ownedSpots.forEach { spot ->
            OwnerSpotCard(
                spot = spot,
                onView = { onView(spot) },
                onEdit = { onEdit(spot) }
            )
            Spacer(Modifier.height(12.dp))
        }
    }

    if (message.isNotBlank()) {
        Spacer(Modifier.height(12.dp))
        Text(
            message,
            color = DeepForest,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun OwnerStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = OwnerWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(title, color = Muted, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                color = DeepForest,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun OwnerSpotCard(
    spot: TouristSpot,
    onView: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = OwnerWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        spot.placeName,
                        color = DeepForest,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        spot.category.ifBlank { "Heritage Site" },
                        color = Muted,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (spot.availability) {
                        OwnerLightGreen
                    } else {
                        Color(0xFFFCE8E6)
                    }
                ) {
                    Text(
                        if (spot.availability) "Available" else "Unavailable",
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        ),
                        color = if (spot.availability) {
                            DeepForest
                        } else {
                            Color(0xFFB54747)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                spot.locationAddress.ifBlank { "Location not specified" },
                color = Muted,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = OwnerBorder)
            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Entry Fee", color = Muted, fontSize = 11.sp)
                    Text(
                        "₱${spot.entryFee}",
                        color = DeepForest,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    if (spot.verified) "✓ Verified" else "Pending verification",
                    color = if (spot.verified) DeepForest else Muted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onView,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("View Details")
                }

                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepForest
                    )
                ) {
                    Text("Edit Site")
                }
            }
        }
    }
}

@Composable
private fun OwnerSpotDetailsDialog(
    spot: TouristSpot,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Cream,
        title = {
            Text(
                "Heritage Site Details",
                color = DeepForest,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    spot.placeName,
                    color = DeepForest,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                OwnerDetailRow("Place ID", spot.placeId.toString())
                OwnerDetailRow("Category", spot.category)
                OwnerDetailRow("Description", spot.description)
                OwnerDetailRow("Address", spot.locationAddress)
                OwnerDetailRow("Entry Fee", "₱${spot.entryFee}")
                OwnerDetailRow(
                    "Availability",
                    if (spot.availability) "Available" else "Unavailable"
                )
                OwnerDetailRow(
                    "Allows Guides",
                    if (spot.allowsGuides) "Yes" else "No"
                )
                OwnerDetailRow(
                    "Verification",
                    if (spot.verified) "Verified" else "Not verified"
                )
                OwnerDetailRow("Latitude", spot.latitude.toString())
                OwnerDetailRow("Longitude", spot.longitude.toString())
                OwnerDetailRow(
                    "Image URL",
                    spot.imageUrl.ifBlank { "No image URL" }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepForest
                )
            ) {
                Text("Edit Site")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = DeepForest)
            }
        }
    )
}

@Composable
private fun OwnerDetailRow(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp)
    ) {
        Text(
            label,
            color = Muted,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(3.dp))
        Text(
            value.ifBlank { "Not specified" },
            color = OwnerText,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(6.dp))
        HorizontalDivider(color = OwnerBorder)
    }
}

@Composable
private fun OwnerEditSpotDialog(
    spot: TouristSpot,
    ownerUid: String,
    onDismiss: () -> Unit,
    onSaved: (String) -> Unit
) {
    var placeName by remember(spot.placeId) {
        mutableStateOf(spot.placeName)
    }
    var description by remember(spot.placeId) {
        mutableStateOf(spot.description)
    }
    var category by remember(spot.placeId) {
        mutableStateOf(spot.category)
    }
    var locationAddress by remember(spot.placeId) {
        mutableStateOf(spot.locationAddress)
    }
    var entryFee by remember(spot.placeId) {
        mutableStateOf(spot.entryFee.toString())
    }
    var allowsGuides by remember(spot.placeId) {
        mutableStateOf(spot.allowsGuides)
    }
    var availability by remember(spot.placeId) {
        mutableStateOf(spot.availability)
    }

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) onDismiss()
        },
        containerColor = Cream,
        title = {
            Text(
                "Edit Heritage Site",
                color = DeepForest,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Update the information of ${spot.placeName}.",
                    color = Muted,
                    fontSize = 12.sp
                )

                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = placeName,
                    onValueChange = { placeName = it },
                    label = { Text("Site Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = locationAddress,
                    onValueChange = { locationAddress = it },
                    label = { Text("Location Address") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = entryFee,
                    onValueChange = { entryFee = it },
                    label = { Text("Entry Fee (₱)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Allows Tour Guides",
                        modifier = Modifier.weight(1f),
                        color = OwnerText,
                        fontSize = 14.sp
                    )
                    Switch(
                        checked = allowsGuides,
                        onCheckedChange = { allowsGuides = it }
                    )
                }

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Site Available",
                        modifier = Modifier.weight(1f),
                        color = OwnerText,
                        fontSize = 14.sp
                    )
                    Switch(
                        checked = availability,
                        onCheckedChange = { availability = it }
                    )
                }

                if (errorMessage.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        errorMessage,
                        color = Color(0xFFB54747),
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSaving,
                onClick = {
                    val parsedFee = entryFee.toDoubleOrNull()

                    when {
                        placeName.isBlank() -> {
                            errorMessage = "Site name is required."
                        }

                        parsedFee == null || parsedFee < 0.0 -> {
                            errorMessage = "Enter a valid non-negative entry fee."
                        }

                        else -> {
                            isSaving = true
                            errorMessage = ""

                            val updatedSpot = spot.copy(
                                placeName = placeName.trim(),
                                description = description.trim(),
                                category = category.trim(),
                                locationAddress = locationAddress.trim(),
                                entryFee = parsedFee,
                                allowsGuides = allowsGuides,
                                availability = availability
                            )

                            Repository.updateTouristSpot(
                                spot = updatedSpot,
                                ownerUid = ownerUid
                            ) { success, resultMessage ->
                                isSaving = false

                                if (success) {
                                    onSaved(resultMessage)
                                } else {
                                    errorMessage = resultMessage
                                }
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepForest
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        Modifier.size(17.dp),
                        color = Cream,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Saving...")
                } else {
                    Text("Save Changes")
                }
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSaving,
                onClick = onDismiss
            ) {
                Text("Cancel", color = DeepForest)
            }
        }
    )
}

@Composable
private fun OwnerInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Muted, fontSize = 13.sp)

        Spacer(Modifier.width(12.dp))

        Text(
            value,
            color = OwnerText,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
