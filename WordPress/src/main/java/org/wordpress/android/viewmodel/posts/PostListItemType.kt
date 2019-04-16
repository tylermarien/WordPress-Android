package org.wordpress.android.viewmodel.posts

import android.support.annotation.ColorRes
import org.wordpress.android.fluxc.model.LocalOrRemoteId
import org.wordpress.android.fluxc.model.LocalOrRemoteId.LocalId
import org.wordpress.android.fluxc.model.LocalOrRemoteId.RemoteId
import org.wordpress.android.ui.utils.UiString
import org.wordpress.android.viewmodel.posts.PostListItemIdentifier.LocalPostId
import org.wordpress.android.viewmodel.posts.PostListItemIdentifier.RemotePostId
import org.wordpress.android.viewmodel.posts.PostListItemType.EndListIndicatorItem
import org.wordpress.android.viewmodel.posts.PostListItemType.LoadingItem
import org.wordpress.android.viewmodel.posts.PostListItemType.PostListItemUiState
import org.wordpress.android.widgets.PostListButtonType

sealed class PostListItemType {
    class PostListItemUiState(
        val data: PostListItemUiStateData,
        val actions: List<PostListItemAction>,
        val onSelected: () -> Unit
    ) : PostListItemType()

    class LoadingItem(val localOrRemoteId: LocalOrRemoteId) : PostListItemType()
    object EndListIndicatorItem : PostListItemType()
}

data class PostListItemUiStateData(
    val remotePostId: RemotePostId,
    val localPostId: LocalPostId,
    val title: UiString?,
    val excerpt: UiString?,
    val imageUrl: String?,
    val dateAndAuthor: UiString?,
    @ColorRes val statusesColor: Int?,
    val statuses: List<UiString>,
    val statusesDelimiter: UiString,
    val showProgress: Boolean,
    val showOverlay: Boolean
)

sealed class PostListItemAction(val buttonType: PostListButtonType, val onButtonClicked: (PostListButtonType) -> Unit) {
    class SingleItem(buttonType: PostListButtonType, onButtonClicked: (PostListButtonType) -> Unit) :
            PostListItemAction(buttonType, onButtonClicked)

    class MoreItem(
        val actions: List<PostListItemAction>,
        onButtonClicked: (PostListButtonType) -> Unit
    ) : PostListItemAction(PostListButtonType.BUTTON_MORE, onButtonClicked)
}

fun doesPostListItemTypesRepresentTheSameActualItem(first: PostListItemType, second: PostListItemType): Boolean {
    val (firstLocalPostId, firstRemotePostId) = getLocalAndRemoteIdForPostListItemType(first)
    val (secondLocalPostId, secondRemotePostId) = getLocalAndRemoteIdForPostListItemType(second)
    if (firstLocalPostId != null && secondLocalPostId != null && firstLocalPostId == secondLocalPostId) {
        // If the local post ids are the same, they represent the same item
        return true
    }
    if (firstRemotePostId != null && secondRemotePostId != null && firstRemotePostId == secondRemotePostId) {
        // If the remote post ids are the same, they represent the same item
        return true
    }
    // In all other cases, we can't be sure if they represent the same item or not
    return false
}

private fun getLocalAndRemoteIdForPostListItemType(itemType: PostListItemType): Pair<LocalPostId?, RemotePostId?> {
    return when (itemType) {
        is PostListItemUiState -> Pair(itemType.data.localPostId, itemType.data.remotePostId)
        is LoadingItem -> {
            when (itemType.localOrRemoteId) {
                is LocalId -> Pair(LocalPostId(itemType.localOrRemoteId), null)
                is RemoteId -> Pair(null, RemotePostId(itemType.localOrRemoteId))
            }
        }
        EndListIndicatorItem -> Pair(null, null)
    }
}
