package com.ttings.beatwave.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.ttings.beatwave.R
import com.ttings.beatwave.data.User
import com.ttings.beatwave.ui.theme.Typography
import timber.log.Timber

@Composable
fun UserPanel(
    user: User,
    currenUserId: String,
    following: String?,
    followers: String?,
    isFollowed: Boolean,
    onFollowClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onFollowingClick: () -> Unit = {},
    onFollowerClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onPlayClick: () -> Unit = {}
) {
    Column {
        Box(
            modifier = Modifier.height(156.dp)
        ) {
            Image(
                painter = rememberImagePainter(
                    data = user.background,
                    builder = {
                        fallback(R.drawable.logo)
                        error(R.drawable.ic_launcher_foreground)
                    }
                ),
                contentDescription = "User background image",
                modifier = Modifier
                    .height(106.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
            Image(
                painter = rememberImagePainter(
                    data = user.avatar,
                    builder = {
                        fallback(R.drawable.logo)
                        error(R.drawable.ic_launcher_foreground)
                    }
                ),
                contentDescription = "User profile image",
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(100.dp)
                    .clip(RoundedCornerShape(50))
                    .align(Alignment.BottomStart),
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = { onBackClick() },
                modifier = Modifier
                    .padding(4.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = "Back button"
                )
            }
        }
        Column {
            Text(
                text = user.username!!,
                style = Typography.bodyMedium,
                maxLines = 1,
                modifier = Modifier.padding(start = 16.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = "${stringResource(id = R.string.following)}: ${following ?: "${stringResource(id = R.string.loading)}..."}",
                    style = Typography.bodySmall,
                    maxLines = 1,
                    modifier = Modifier.clickable(onClick = { onFollowingClick() })
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "${stringResource(id = R.string.followers)}: ${followers ?: "${stringResource(id = R.string.loading)}..."}",
                    style = Typography.bodySmall,
                    maxLines = 1,
                    modifier = Modifier.clickable(onClick = { onFollowerClick() })
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Timber.tag("UserPanel").d("Current user ID: $currenUserId, User ID: ${user.userId}")
                if (currenUserId == user.userId) {
                    IconButton(
                        onClick = { onEditClick() },
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Icon(
                            modifier = Modifier.align(Alignment.Bottom),
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit profile"
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .clickable {
                                onFollowClick()
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isFollowed) Icons.Rounded.Check else Icons.Rounded.Add,
                            contentDescription = "Follow"
                        )
                        Text(
                            text = if (isFollowed) stringResource(id = R.string.followed) else stringResource(id = R.string.follow),
                            style = Typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                IconButton(
                    onClick = { onPlayClick() },
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Icon(
                        modifier = Modifier.scale(1.4f),
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause"
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun UserPanelPreview() {
    UserPanel(
        user = User(
            userId = "0",
            username = "Username",
            background = "https://example.com/image.jpg",
            avatar = "https://example.com/image.jpg"
        ),
        currenUserId = "0",
        isFollowed = true,
        following = "0",
        followers = "0"
    )
}