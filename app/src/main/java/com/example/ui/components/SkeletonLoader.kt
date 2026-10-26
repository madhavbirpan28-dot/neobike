package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeoCharcoalSurface
import com.example.ui.theme.NeoCharcoalSurfaceCard
import com.example.ui.theme.NeoCharcoalSurfaceElevated

/**
 * Creates an animated shimmer brush for loading skeleton states.
 */
@Composable
fun shimmerBrush(
    targetValue: Float = 1000f,
    showShimmer: Boolean = true
): Brush {
    if (!showShimmer) {
        return Brush.linearGradient(
            colors = listOf(
                NeoCharcoalSurfaceCard,
                NeoCharcoalSurfaceCard
            )
        )
    }

    val shimmerColors = listOf(
        NeoCharcoalSurface,
        NeoCharcoalSurfaceElevated,
        NeoCharcoalSurface
    )

    val transition = rememberInfiniteTransition(label = "ShimmerTransition")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerTranslation"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnimation, y = translateAnimation)
    )
}

/**
 * A basic box placeholder component with shimmer animation.
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp,
    brush: Brush = shimmerBrush()
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
    )
}

/**
 * Skeleton placeholder representing a Ride Card item in lists.
 */
@Composable
fun RideCardSkeleton(
    modifier: Modifier = Modifier,
    brush: Brush = shimmerBrush()
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ride_card_skeleton"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeoCharcoalSurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title & Capacity badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(22.dp),
                    brush = brush
                )
                Spacer(modifier = Modifier.width(12.dp))
                SkeletonBox(
                    modifier = Modifier
                        .width(70.dp)
                        .height(22.dp),
                    cornerRadius = 12.dp,
                    brush = brush
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Route visual representation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(brush)
                )
                Spacer(modifier = Modifier.width(8.dp))
                SkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(16.dp),
                    brush = brush
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(brush)
                )
                Spacer(modifier = Modifier.width(8.dp))
                SkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(16.dp),
                    brush = brush
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer: Organizer avatar & button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(brush)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SkeletonBox(
                        modifier = Modifier
                            .width(90.dp)
                            .height(14.dp),
                        brush = brush
                    )
                }

                SkeletonBox(
                    modifier = Modifier
                        .width(80.dp)
                        .height(32.dp),
                    cornerRadius = 20.dp,
                    brush = brush
                )
            }
        }
    }
}

/**
 * Skeleton placeholder representing a Community/Feed item in home or community feeds.
 */
@Composable
fun FeedItemSkeleton(
    modifier: Modifier = Modifier,
    brush: Brush = shimmerBrush()
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("feed_item_skeleton"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeoCharcoalSurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // User header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(brush)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(16.dp),
                        brush = brush
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .height(12.dp),
                        brush = brush
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body text
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp),
                brush = brush
            )
            Spacer(modifier = Modifier.height(6.dp))
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(14.dp),
                brush = brush
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Optional image placeholder
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                cornerRadius = 12.dp,
                brush = brush
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Actions row (Like, Comment, Share placeholders)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(3) {
                    SkeletonBox(
                        modifier = Modifier
                            .width(60.dp)
                            .height(20.dp),
                        cornerRadius = 10.dp,
                        brush = brush
                    )
                }
            }
        }
    }
}

/**
 * Reusable container that displays a list of skeleton placeholders.
 */
@Composable
fun SkeletonList(
    modifier: Modifier = Modifier,
    count: Int = 3,
    itemSpacing: Dp = 12.dp,
    skeletonItem: @Composable () -> Unit = { RideCardSkeleton() }
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("skeleton_loader"),
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        repeat(count) {
            skeletonItem()
        }
    }
}
