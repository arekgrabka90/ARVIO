package com.arflix.tv.ui.screens.profile

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.arflix.tv.R
import com.arflix.tv.data.model.Profile
import com.arflix.tv.data.model.KidsAgeLevel
import com.arflix.tv.ui.components.CloudEmailPasswordModal
import com.arflix.tv.ui.components.CloudPairModal
import com.arflix.tv.ui.components.ProfileAvatarVisual
import com.arflix.tv.ui.components.Toast
import com.arflix.tv.ui.theme.ErrorRed
import com.arflix.tv.util.LocalDeviceType
import com.arflix.tv.util.PinUtil
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.drawBehind
import com.arflix.tv.ui.skin.ArvioSkin
import com.arflix.tv.ui.skin.resolveAccentColor
import com.arflix.tv.ui.theme.AnimationConstants
import com.arflix.tv.ui.theme.appBackgroundDark

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ProfileSelectionScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onProfileSelected: (String) -> Unit,
    onShowAddProfile: () -> Unit,
    onConnectCloud: () -> Unit = {},
    isCloudConnected: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val effectiveCloudConnected = isCloudConnected || uiState.isCloudConnected
    ProfileSelectionContent(uiState, ProfileSelectionActions(
        createProfile = viewModel::createProfile,
        deleteProfile = viewModel::deleteProfile,
        dismissToast = viewModel::dismissToast,
        hideAddDialog = viewModel::hideAddDialog,
        hideEditDialog = viewModel::hideEditDialog,
        hidePinDialog = viewModel::hidePinDialog,
        preloadForProfile = viewModel::preloadForProfile,
        removeProfilePin = viewModel::removeProfilePin,
        removeSelectedAvatarImage = viewModel::removeSelectedAvatarImage,
        selectProfile = viewModel::selectProfile,
        selectProfileWithLockCheck = viewModel::selectProfileWithLockCheck,
        setKidsAgeLevel = viewModel::setKidsAgeLevel,
        setKidsProfile = viewModel::setKidsProfile,
        setNewProfileName = viewModel::setNewProfileName,
        setSelectedAvatarId = viewModel::setSelectedAvatarId,
        setSelectedAvatarImage = viewModel::setSelectedAvatarImage,
        setSelectedColorIndex = viewModel::setSelectedColorIndex,
        setupParentPin = viewModel::setupParentPin,
        setupProfilePin = viewModel::setupProfilePin,
        showAddDialog = viewModel::showAddDialog,
        showEditDialog = viewModel::showEditDialog,
        showPinSetupDialog = viewModel::showPinSetupDialog,
        toggleManageMode = viewModel::toggleManageMode,
        updateProfile = viewModel::updateProfile,
        verifyPinAndSelectProfile = viewModel::verifyPinAndSelectProfile,
        startCloudAuth = viewModel::startCloudAuth,
        openCloudEmailPasswordDialog = viewModel::openCloudEmailPasswordDialog,
        cancelCloudAuth = viewModel::cancelCloudAuth,
        closeCloudEmailPasswordDialog = viewModel::closeCloudEmailPasswordDialog,
        completeCloudAuthWithEmailPassword = viewModel::completeCloudAuthWithEmailPassword,
        retryCloudRestore = viewModel::retryCloudRestore,
    ), onProfileSelected, onConnectCloud, effectiveCloudConnected)
}

internal data class ProfileSelectionActions(
    val createProfile: () -> Unit = { },
    val deleteProfile: (Profile) -> Unit = { _ -> },
    val dismissToast: () -> Unit = { },
    val hideAddDialog: () -> Unit = { },
    val hideEditDialog: () -> Unit = { },
    val hidePinDialog: () -> Unit = { },
    val preloadForProfile: (Profile) -> Unit = { _ -> },
    val removeProfilePin: () -> Unit = { },
    val removeSelectedAvatarImage: () -> Unit = { },
    val selectProfile: (Profile) -> Unit = { _ -> },
    val selectProfileWithLockCheck: (Profile) -> Unit = { _ -> },
    val setKidsAgeLevel: (KidsAgeLevel) -> Unit = { _ -> },
    val setKidsProfile: (Boolean) -> Unit = { _ -> },
    val setNewProfileName: (String) -> Unit = { _ -> },
    val setSelectedAvatarId: (Int) -> Unit = { _ -> },
    val setSelectedAvatarImage: (String) -> Unit = { _ -> },
    val setSelectedColorIndex: (Int) -> Unit = { _ -> },
    val setupParentPin: (String) -> Unit = { _ -> },
    val setupProfilePin: (String) -> Unit = { _ -> },
    val showAddDialog: () -> Unit = { },
    val showEditDialog: (Profile) -> Unit = { _ -> },
    val showPinSetupDialog: () -> Unit = { },
    val toggleManageMode: () -> Unit = { },
    val updateProfile: () -> Unit = { },
    val verifyPinAndSelectProfile: (String) -> Unit = { _ -> },
    val startCloudAuth: () -> Unit = { },
    val openCloudEmailPasswordDialog: () -> Unit = { },
    val cancelCloudAuth: () -> Unit = { },
    val closeCloudEmailPasswordDialog: () -> Unit = { },
    val completeCloudAuthWithEmailPassword: (String, String, Boolean) -> Unit = { _, _, _ -> },
    val retryCloudRestore: () -> Unit = {},
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
internal fun ProfileSelectionContent(
    uiState: ProfileUiState,
    viewModel: ProfileSelectionActions,
    onProfileSelected: (String) -> Unit,
    onConnectCloud: () -> Unit = {},
    isCloudConnected: Boolean = false,
) {

    // Create focus requesters for each profile slot (max 5 profiles + 1 add button)
    val focusRequesters = remember { List(6) { FocusRequester() } }

    // Guard against Enter key events from previous screen (TV only — touch devices don't need this)
    val isTouchDevice = LocalDeviceType.current.isTouchDevice()
    var isReadyForInput by remember { mutableStateOf(isTouchDevice) }


    val context = LocalContext.current
    val motionEnabled = remember(context) {
        android.provider.Settings.Global.getFloat(context.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }

    // Coordinate tracking & 3-step transition states
    var rootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val avatarCoordinatesMap = remember { mutableMapOf<String, LayoutCoordinates>() }
    var transitionProfile by remember { mutableStateOf<Profile?>(null) }
    var initialDeltaOffset by remember { mutableStateOf(Offset.Zero) }
    var isTransitioning by remember { mutableStateOf(false) }
    var minAnimationCompleted by remember { mutableStateOf(false) }

    val transitionProgress by animateFloatAsState(
        targetValue = if (isTransitioning) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (motionEnabled) 320 else 0,
            easing = FastOutSlowInEasing
        ),
        label = "profile_transition_progress"
    )

    fun startTransitionForProfile(profile: Profile) {
        val coords = avatarCoordinatesMap[profile.id]
        val root = rootCoordinates
        if (coords != null && root != null && root.isAttached && coords.isAttached) {
            val avatarCenterInRoot = root.localPositionOf(coords, Offset(coords.size.width / 2f, coords.size.height / 2f))
            val rootCenter = Offset(root.size.width / 2f, root.size.height / 2f)
            initialDeltaOffset = Offset(
                x = avatarCenterInRoot.x - rootCenter.x,
                y = avatarCenterInRoot.y - rootCenter.y
            )
        } else {
            initialDeltaOffset = Offset.Zero
        }
        transitionProfile = profile
        isTransitioning = true
    }

    // Set ready for input after a short delay to ignore stray key events (TV only)
    LaunchedEffect(Unit) {
        if (!isTouchDevice) {
            delay(300)
            isReadyForInput = true
        }
    }

    // Reset input guard when dialogs close (TV only)
    LaunchedEffect(uiState.showAddDialog, uiState.editingProfile) {
        if (!isTouchDevice && !uiState.showAddDialog && uiState.editingProfile == null && isReadyForInput) {
            isReadyForInput = false
            delay(300)
            isReadyForInput = true
        }
    }

    // Cancel any running transition if entering manage mode or editing a profile
    LaunchedEffect(uiState.isManageMode, uiState.editingProfile) {
        if (uiState.isManageMode || uiState.editingProfile != null) {
            isTransitioning = false
            transitionProfile = null
        }
    }

    // Guarantee full completion of the expand/center animation before moving to Home
    LaunchedEffect(isTransitioning) {
        if (isTransitioning) {
            minAnimationCompleted = false
            delay(if (motionEnabled) 340L else 0L)
            minAnimationCompleted = true
        }
    }

    // Navigate to Home once the expand-to-center animation has finished AND profile data loading is complete
    LaunchedEffect(isTransitioning, minAnimationCompleted, uiState.isSwitchingProfile, uiState.activeProfile?.id,
        transitionProfile?.id, uiState.isManageMode, uiState.showPinDialog) {
        if (
            isTransitioning &&
            minAnimationCompleted &&
            uiState.canFinishSelection(transitionProfile?.id)
        ) {
            uiState.activeProfile?.id?.let(onProfileSelected)
        }
    }

    // Request focus on the first available item (profile or add button)
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && !isTouchDevice) {
            delay(50)
            val targetIndex = if (uiState.profiles.isNotEmpty()) {
                uiState.activeProfile?.let { active ->
                    uiState.profiles.indexOfFirst { it.id == active.id }.takeIf { it >= 0 }
                } ?: 0
            } else {
                0 // Focus on Add Profile button
            }
            try {
                focusRequesters.getOrNull(targetIndex)?.requestFocus()
            } catch (e: IllegalStateException) {
                delay(100)
                try {
                    focusRequesters.getOrNull(targetIndex)?.requestFocus()
                } catch (e2: IllegalStateException) {
                    // Give up silently
                }
            }
        }
    }

    val handleProfileClick: (Profile) -> Unit = { profile ->
        if (!uiState.isSwitchingProfile && !isTransitioning && (isTouchDevice || isReadyForInput)) {
            if (uiState.isManageMode) {
                viewModel.showEditDialog(profile)
            } else if (profile.isLocked && !profile.pin.isNullOrEmpty()) {
                viewModel.selectProfileWithLockCheck(profile)
            } else {
                startTransitionForProfile(profile)
                viewModel.selectProfile(profile)
            }
        }
    }

    val accent = resolveAccentColor(ArvioSkin.colors.focusOutline)
    val pageBackground = appBackgroundDark()
    val kidsBadgeLabel = stringResource(R.string.profile_kids_badge)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Neutral near-black page + a very soft glow in the user's accent colour.
                drawRect(pageBackground)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(size.width / 2f, size.height * 0.38f),
                        radius = size.minDimension * 0.95f
                    )
                )
            }
            .navigationBarsPadding()
            .onGloballyPositioned { rootCoordinates = it },
        contentAlignment = Alignment.Center
    ) {
        val isCompactHeight = maxHeight < 480.dp
        val slots = uiState.profiles.size + if (uiState.profiles.size < 5) 1 else 0
        val tileGapX = if (isTouchDevice) 20.dp else 32.dp
        val tileGapY = 24.dp
        val tilePad = 8.dp // label is a little wider than the avatar
        val columns = when {
            !isTouchDevice -> slots.coerceAtLeast(1)
            isCompactHeight || maxWidth >= 840.dp -> slots.coerceAtLeast(1) // landscape phone / large tablet: one row
            maxWidth >= 600.dp -> 3
            else -> 2
        }
        val contentWidth = if (isTouchDevice) (maxWidth - 48.dp).coerceAtMost(900.dp) else maxWidth - 96.dp
        val rawAvatarSize = (contentWidth - tileGapX * (columns - 1) - tilePad * columns) / columns
        val avatarSize = if (isTouchDevice) {
            rawAvatarSize.coerceIn(80.dp, if (isCompactHeight) 104.dp else 128.dp)
        } else {
            rawAvatarSize.coerceIn(88.dp, 168.dp)
        }
        val backgroundAlpha = if (isTransitioning) {
            (1f - transitionProgress * 2.5f).coerceIn(0f, 1f)
        } else 1f

        if (backgroundAlpha > 0f) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(contentWidth)
                    .then(if (isTouchDevice) Modifier.verticalScroll(rememberScrollState()).padding(vertical = 32.dp) else Modifier)
                    .graphicsLayer { alpha = backgroundAlpha }
            ) {
                // Brand wordmark
                Image(
                    painter = painterResource(id = R.drawable.ic_arvio_wordmark),
                    contentDescription = "ARVIO",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.height(if (isTouchDevice) 22.dp else 30.dp)
                )

                Spacer(modifier = Modifier.height(if (isTouchDevice) 28.dp else 20.dp))

                Text(
                    text = if (uiState.isManageMode) stringResource(R.string.manage_profiles) else stringResource(R.string.whos_watching),
                    style = ArvioSkin.typography.heroTitle.copy(
                        fontSize = if (isTouchDevice) 28.sp else 40.sp,
                        lineHeight = if (isTouchDevice) 34.sp else 48.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.3).sp
                    ),
                    color = ArvioSkin.colors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Fixed-height hint: toggling manage mode never shifts the layout.
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .padding(top = 6.dp)
                ) {
                    if (uiState.isManageMode) {
                        Text(
                            text = stringResource(R.string.profile_manage_hint),
                            style = ArvioSkin.typography.body,
                            color = ArvioSkin.colors.textMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (isTouchDevice) 28.dp else 20.dp))

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(avatarSize),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.loading_profile),
                            style = ArvioSkin.typography.body.copy(fontSize = 15.sp),
                            color = ArvioSkin.colors.textMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // One layout path for TV, tablet and phone: only the column count changes.
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(tileGapY)
                    ) {
                        (0 until slots).toList().chunked(columns).forEach { rowIndices ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(tileGapX, Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.Top
                            ) {
                                rowIndices.forEach { index ->
                                    val profile = uiState.profiles.getOrNull(index)
                                    val appearDelay = if (motionEnabled) index * AnimationConstants.STAGGER_CARD else 0
                                    if (profile != null) {
                                        val isActive = uiState.activeProfile?.id == profile.id && !uiState.isManageMode
                                        val isLockedProfile = profile.isLocked && !profile.pin.isNullOrEmpty()
                                        val currentCd = stringResource(R.string.profile_current_cd)
                                        val lockedCd = stringResource(R.string.profile_locked_cd)
                                        ProfileTile(
                                            label = profile.name,
                                            avatarSize = avatarSize,
                                            focusRequester = focusRequesters.getOrNull(index),
                                            isActive = isActive,
                                            isHidden = isTransitioning && transitionProfile?.id == profile.id,
                                            badge = when {
                                                uiState.isManageMode -> ProfileTileBadge.Edit
                                                isLockedProfile -> ProfileTileBadge.Locked
                                                else -> ProfileTileBadge.None
                                            },
                                            subLabel = if (profile.isKidsProfile && !profile.name.equals(kidsBadgeLabel, ignoreCase = true)) kidsBadgeLabel else null,
                                            stateText = listOfNotNull(
                                                currentCd.takeIf { isActive },
                                                lockedCd.takeIf { isLockedProfile }
                                            ).joinToString(", ").ifEmpty { null },
                                            onClickLabel = if (uiState.isManageMode) stringResource(R.string.profile_edit_cd, profile.name) else null,
                                            appearDelayMillis = appearDelay,
                                            animateAppear = motionEnabled,
                                            onAvatarPositioned = { avatarCoordinatesMap[profile.id] = it },
                                            onFocus = { viewModel.preloadForProfile(profile) },
                                            onClick = { handleProfileClick(profile) }
                                        ) {
                                            ProfileAvatarVisual(
                                                profile = profile,
                                                letterFontSize = if (isTouchDevice) 36.sp else 48.sp,
                                                iconPadding = if (isTouchDevice) 10.dp else 12.dp
                                            )
                                        }
                                    } else {
                                        ProfileTile(
                                            label = stringResource(R.string.add_profile),
                                            avatarSize = avatarSize,
                                            focusRequester = focusRequesters.getOrNull(index),
                                            appearDelayMillis = appearDelay,
                                            animateAppear = motionEnabled,
                                            onClick = {
                                                if ((isTouchDevice || isReadyForInput) && !uiState.isSwitchingProfile && !isTransitioning) {
                                                    viewModel.showAddDialog()
                                                }
                                            }
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(ArvioSkin.colors.textPrimary.copy(alpha = 0.08f))
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    tint = ArvioSkin.colors.textPrimary.copy(alpha = 0.8f),
                                                    modifier = Modifier.size(if (isTouchDevice) 36.dp else 44.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(if (isTouchDevice) 32.dp else 24.dp))

                // Quiet actions: no outlines, accent halo on focus.
                val manageAction: @Composable () -> Unit = {
                    ProfileTextButton(
                        label = stringResource(if (uiState.isManageMode) R.string.done else R.string.manage_profiles),
                        emphasized = uiState.isManageMode,
                        onClick = {
                            if ((isTouchDevice || isReadyForInput) && !uiState.isSwitchingProfile && !isTransitioning) {
                                viewModel.toggleManageMode()
                            }
                        }
                    )
                }
                val cloudAction: @Composable () -> Unit = {
                    if (uiState.cloudRestoreFailed) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.settings_cloud_signed_in_restore_failed),
                                style = ArvioSkin.typography.body,
                                color = ErrorRed,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            ProfileTextButton(
                                label = stringResource(if (uiState.isCloudAuthWorking) R.string.syncing else R.string.retry),
                                icon = Icons.Default.Refresh,
                                isWorking = uiState.isCloudAuthWorking,
                                onClick = viewModel.retryCloudRestore
                            )
                        }
                    } else if (!isCloudConnected) {
                        ProfileTextButton(
                            label = stringResource(R.string.connect_to_cloud),
                            icon = Icons.Default.Cloud,
                            onClick = {
                                if ((isTouchDevice || isReadyForInput) && !uiState.isSwitchingProfile && !isTransitioning) {
                                    if (isTouchDevice) {
                                        viewModel.openCloudEmailPasswordDialog()
                                    } else {
                                        viewModel.startCloudAuth()
                                    }
                                }
                            }
                        )
                    }
                }
                if (maxWidth < 600.dp || uiState.cloudRestoreFailed) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        manageAction()
                        cloudAction()
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        manageAction()
                        cloudAction()
                    }
                }
            }
        }

        // ── 3-Step Profile Selection Transition Overlay ──
        if (transitionProfile != null) {
            val targetScale = if (motionEnabled) 1.12f else 1f
            val currentScale = lerp(1f, targetScale, transitionProgress)
            val currentOffsetX = if (motionEnabled) lerp(initialDeltaOffset.x, 0f, transitionProgress) else 0f
            val currentOffsetY = if (motionEnabled) lerp(initialDeltaOffset.y, 0f, transitionProgress) else 0f
            val avatarBaseSize = avatarSize

            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // The enlarged profile card: centered directly at (0, 0)
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .graphicsLayer {
                            translationX = currentOffsetX
                            translationY = currentOffsetY
                            scaleX = currentScale
                            scaleY = currentScale
                        }
                        .size(avatarBaseSize)
                        .clip(RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    ProfileAvatarVisual(
                        profile = transitionProfile!!,
                        letterFontSize = 48.sp,
                        iconPadding = 12.dp
                    )
                }

                // Loading ring: appears only if loading is still in progress once the card is centered
                val showSpinner by produceState(initialValue = false, key1 = isTransitioning, key2 = uiState.isSwitchingProfile) {
                    if (isTransitioning && uiState.isSwitchingProfile) {
                        delay(200) // Smooth delay so instantaneous cached data doesn't flash the spinner
                        value = isTransitioning && uiState.isSwitchingProfile
                    } else {
                        value = false
                    }
                }

                val spinnerAlpha by animateFloatAsState(
                    targetValue = if (transitionProgress >= 0.95f && showSpinner) 1f else 0f,
                    animationSpec = tween(220),
                    label = "spinner_alpha"
                )

                if (spinnerAlpha > 0f) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = (avatarBaseSize * targetScale / 2) + 26.dp)
                            .size(26.dp)
                            .graphicsLayer { alpha = spinnerAlpha },
                        color = Color.White.copy(alpha = 0.85f),
                        strokeWidth = 2.5.dp,
                        trackColor = Color.White.copy(alpha = 0.12f)
                    )
                }
            }
        }

        // Add Profile Dialog
        if (uiState.showAddDialog) {
            AddProfileDialog(
                name = uiState.newProfileName,
                onNameChange = { viewModel.setNewProfileName(it) },
                selectedColorIndex = uiState.selectedColorIndex,
                onColorSelected = { viewModel.setSelectedColorIndex(it) },
                selectedAvatarId = uiState.selectedAvatarId,
                onAvatarSelected = { viewModel.setSelectedAvatarId(it) },
                selectedAvatarImageUri = uiState.selectedAvatarImageUri,
                useCustomAvatarImage = uiState.useCustomAvatarImage,
                onAvatarImageSelected = { viewModel.setSelectedAvatarImage(it) },
                onRemoveAvatarImage = { viewModel.removeSelectedAvatarImage() },
                isKidsProfile = uiState.isKidsProfile,
                onKidsProfileChange = viewModel.setKidsProfile,
                kidsAgeLevel = uiState.kidsAgeLevel,
                onKidsAgeLevelChange = viewModel.setKidsAgeLevel,
                onConfirm = { viewModel.createProfile() },
                onDismiss = { viewModel.hideAddDialog() }
            )
        }

        // Edit Profile Dialog
        uiState.editingProfile?.let { profile ->
            EditProfileDialog(
                profile = profile,
                name = uiState.newProfileName,
                onNameChange = { viewModel.setNewProfileName(it) },
                selectedColorIndex = uiState.selectedColorIndex,
                onColorSelected = { viewModel.setSelectedColorIndex(it) },
                selectedAvatarId = uiState.selectedAvatarId,
                onAvatarSelected = { viewModel.setSelectedAvatarId(it) },
                selectedAvatarImageUri = uiState.selectedAvatarImageUri,
                useCustomAvatarImage = uiState.useCustomAvatarImage,
                onAvatarImageSelected = { viewModel.setSelectedAvatarImage(it) },
                onRemoveAvatarImage = { viewModel.removeSelectedAvatarImage() },
                isKidsProfile = uiState.isKidsProfile,
                onKidsProfileChange = viewModel.setKidsProfile,
                kidsAgeLevel = uiState.kidsAgeLevel,
                onKidsAgeLevelChange = viewModel.setKidsAgeLevel,
                onConfirm = { viewModel.updateProfile() },
                onDelete = { viewModel.deleteProfile(profile); viewModel.hideEditDialog() },
                onDismiss = { viewModel.hideEditDialog() },
                onShowPinSetup = { viewModel.showPinSetupDialog() },
                onRemovePin = { viewModel.removeProfilePin() }
            )
        }

        // Cloud Email/Password Modal
        if (uiState.showCloudEmailPasswordDialog) {
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            val context = LocalContext.current
            CloudEmailPasswordModal(
                email = email,
                password = password,
                onEmailChange = { email = it },
                onPasswordChange = { password = it },
                onDismiss = { viewModel.closeCloudEmailPasswordDialog() },
                onSignIn = {
                    viewModel.completeCloudAuthWithEmailPassword(email, password, false)
                },
                onCreateAccount = {
                    viewModel.completeCloudAuthWithEmailPassword(email, password, true)
                },
                onOpenPrivacy = {
                    runCatching {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://arvio.tv/privacy")
                        )
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                }
            )
        }

        // Cloud TV Pairing Modal
        if (uiState.showCloudPairDialog) {
            CloudPairModal(
                verificationUrl = uiState.cloudVerificationUrl.orEmpty(),
                userCode = uiState.cloudUserCode.orEmpty(),
                isWorking = uiState.isCloudAuthWorking,
                onDismiss = { viewModel.cancelCloudAuth() },
                onUseEmailPassword = { viewModel.openCloudEmailPasswordDialog() }
            )
        }

        // Toast Notification
        Toast(
            message = uiState.toastMessage ?: "",
            type = uiState.toastType,
            isVisible = uiState.showToast,
            onDismiss = { viewModel.dismissToast() }
        )

        // PIN Entry Dialog
        if (uiState.showPinDialog) {
            if (uiState.pinDialogMode == "verify") {
                val isParentPin = uiState.pinContext.startsWith(ProfileViewModel.PIN_CONTEXT_PARENT_PREFIX)
                PinEntryDialog(
                    title = stringResource(if (isParentPin) R.string.profile_parent_pin_enter else R.string.enter_pin_to_unlock),
                    onPinConfirmed = { pin ->
                        val pending = uiState.pendingProfileForPin
                        if (pending != null && PinUtil.verifyPin(pin, pending.pin) && uiState.pinContext == "select") {
                            startTransitionForProfile(pending)
                        }
                        viewModel.verifyPinAndSelectProfile(pin)
                    },
                    onDismiss = { viewModel.hidePinDialog() },
                    isSetup = false,
                    pinError = uiState.pinError
                )
            } else if (uiState.pinDialogMode == "setup") {
                val isParentPin = uiState.pinContext.startsWith(ProfileViewModel.PIN_CONTEXT_PARENT_PREFIX)
                PinEntryDialog(
                    title = stringResource(if (isParentPin) R.string.profile_parent_pin_set else R.string.set_profile_pin),
                    onPinConfirmed = { pin ->
                        if (isParentPin) viewModel.setupParentPin(pin) else viewModel.setupProfilePin(pin)
                    },
                    setupHint = if (isParentPin) stringResource(R.string.profile_parent_pin_hint) else null,
                    onDismiss = { viewModel.hidePinDialog() },
                    isSetup = true
                )
            }
        }
    }
}
