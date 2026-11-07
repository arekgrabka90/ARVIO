package com.arflix.tv.ui.screens.profile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.arflix.tv.ui.skin.ArvioSkin
import com.arflix.tv.ui.skin.resolveAccentColor
import com.arflix.tv.ui.theme.AnimationConstants
import com.arflix.tv.ui.theme.appBackgroundDark
import com.arflix.tv.util.LocalDeviceType
import kotlinx.coroutines.delay

/** Small status badge drawn on the corner of a profile avatar. */
internal enum class ProfileTileBadge { None, Locked, Edit }

/**
 * Focus ring in the accent colour, drawn *outside* the element with a small gap
 * (Apple-TV style halo). Pass [cornerRadius] = null for a circle.
 * The ring appears immediately (no fade) so fast D-pad moves never leave two
 * highlighted items at once.
 */
internal fun Modifier.profileFocusRing(
    visible: Boolean,
    color: Color,
    gap: Dp = 5.dp,
    stroke: Dp = 2.5.dp,
    cornerRadius: Dp? = null,
): Modifier = drawWithContent {
    drawContent()
    if (visible) {
        val g = gap.toPx()
        val s = stroke.toPx()
        if (cornerRadius == null) {
            drawCircle(
                color = color,
                radius = size.minDimension / 2f + g + s / 2f,
                style = Stroke(width = s)
            )
        } else {
            val inset = g + s / 2f
            drawRoundRect(
                color = color,
                topLeft = Offset(-inset, -inset),
                size = Size(size.width + 2f * inset, size.height + 2f * inset),
                cornerRadius = CornerRadius(cornerRadius.toPx() + g),
                style = Stroke(width = s)
            )
        }
    }
}

/**
 * One implementation of a profile avatar tile for touch AND remote control.
 *
 * - Focus: scale 1.08 + accent halo, name brightens (no pill behind the name).
 * - Active profile: small accent dot under the name.
 * - Kids: neutral chip under the name ([subLabel]).
 * - Lock / manage-mode edit: small corner badge ([badge]).
 * - Accessibility: contentDescription is the plain profile name.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
internal fun ProfileTile(
    label: String,
    avatarSize: Dp,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    isActive: Boolean = false,
    isHidden: Boolean = false,
    badge: ProfileTileBadge = ProfileTileBadge.None,
    subLabel: String? = null,
    stateText: String? = null,
    onClickLabel: String? = null,
    appearDelayMillis: Int = 0,
    animateAppear: Boolean = true,
    onAvatarPositioned: (LayoutCoordinates) -> Unit = {},
    onFocus: () -> Unit = {},
    onClick: () -> Unit,
    avatar: @Composable () -> Unit,
) {
    val isTouch = LocalDeviceType.current.isTouchDevice()
    val colors = ArvioSkin.colors
    val accent = resolveAccentColor(colors.focusOutline)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    var focused by remember { mutableStateOf(false) }

    // Staggered entrance (skipped when the system animation scale is 0).
    var appeared by remember { mutableStateOf(!animateAppear) }
    LaunchedEffect(Unit) {
        if (!appeared) {
            delay(appearDelayMillis.toLong())
            appeared = true
        }
    }
    val appear by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(AnimationConstants.DURATION_EMPHASIS, easing = AnimationConstants.EaseOut),
        label = "profile_tile_appear"
    )
    val scale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.96f
            focused -> 1.08f
            else -> 1f
        },
        animationSpec = tween(ArvioSkin.motion.focusDurationMillis + 30, easing = ArvioSkin.motion.focusEasing),
        label = "profile_tile_scale"
    )
    val labelAlpha by animateFloatAsState(
        targetValue = if (focused || isActive) 1f else 0.62f,
        animationSpec = tween(ArvioSkin.motion.focusDurationMillis),
        label = "profile_tile_label"
    )

    val labelWidth = avatarSize + 8.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.graphicsLayer {
            alpha = if (isHidden) 0f else appear
            translationY = (1f - appear) * 12.dp.toPx()
        }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(avatarSize)
                .onGloballyPositioned(onAvatarPositioned)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                .onFocusChanged { state ->
                    if (state.isFocused != focused) {
                        focused = state.isFocused
                        if (state.isFocused) onFocus()
                    }
                }
                .profileFocusRing(visible = focused, color = accent)
                .semantics {
                    contentDescription = label
                    if (stateText != null) stateDescription = stateText
                }
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    role = Role.Button,
                    onClickLabel = onClickLabel,
                    onClick = onClick
                )
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
            ) { avatar() }

            when (badge) {
                ProfileTileBadge.Edit -> TileBadge(Icons.Default.Edit, avatarSize)
                ProfileTileBadge.Locked -> TileBadge(Icons.Default.Lock, avatarSize)
                ProfileTileBadge.None -> Unit
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = label,
            style = ArvioSkin.typography.cardTitle.copy(fontSize = if (isTouch) 15.sp else 17.sp),
            color = colors.textPrimary.copy(alpha = labelAlpha),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(labelWidth)
        )

        // Fixed-height status row keeps every avatar top-aligned, with or without a chip.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(labelWidth)
                .height(24.dp)
                .padding(top = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(accent, CircleShape)
                    )
                }
                if (subLabel != null) {
                    Text(
                        text = subLabel,
                        style = ArvioSkin.typography.caption,
                        color = colors.textMuted,
                        maxLines = 1,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(colors.surfaceRaised)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun BoxScope.TileBadge(icon: ImageVector, avatarSize: Dp) {
    val badgeSize = (avatarSize * 0.26f).coerceIn(24.dp, 36.dp)
    // Outer disc in the page colour makes a clean "cut-out" around the badge.
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .offset(x = 2.dp, y = 2.dp)
            .size(badgeSize + 6.dp)
            .background(appBackgroundDark(), CircleShape)
            .padding(3.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(badgeSize)
                .background(ArvioSkin.colors.surfaceRaised, CircleShape)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ArvioSkin.colors.textPrimary,
                modifier = Modifier.size(badgeSize * 0.5f)
            )
        }
    }
}

/**
 * Quiet text action (no outline, no box at rest). Focus = accent halo + brighter text.
 * Used for "Manage profiles", "Connect to cloud" and "Retry".
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
internal fun ProfileTextButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    emphasized: Boolean = false,
    enabled: Boolean = true,
    isWorking: Boolean = false,
) {
    val isTouch = LocalDeviceType.current.isTouchDevice()
    val colors = ArvioSkin.colors
    val accent = resolveAccentColor(colors.focusOutline)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    var focused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(100),
        label = "profile_button_scale"
    )
    val contentColor = if (focused || emphasized) colors.textPrimary else colors.textMuted

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .onFocusChanged { focused = it.isFocused }
            .profileFocusRing(
                visible = focused,
                color = accent,
                gap = 3.dp,
                stroke = 2.dp,
                cornerRadius = 22.dp
            )
            .clip(RoundedCornerShape(22.dp))
            .background(if (emphasized) colors.textPrimary.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(
                enabled = enabled && !isWorking,
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 11.dp)
    ) {
        if (isWorking) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = label,
            style = ArvioSkin.typography.button.copy(
                fontSize = if (isTouch) 14.sp else 15.sp,
                fontWeight = FontWeight.Medium
            ),
            color = contentColor
        )
    }
}
