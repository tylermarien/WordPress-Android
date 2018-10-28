package org.wordpress.android.ui

import android.support.v7.util.DiffUtil
import org.wordpress.android.fluxc.model.list.ItemOrMarker.Item
import org.wordpress.android.fluxc.model.list.ItemOrMarker.Marker
import org.wordpress.android.fluxc.model.list.ListManager
import org.wordpress.android.fluxc.store.PostListMarker

/**
 * A helper class which can be used to compare two [ListManager]s.
 *
 * @param oldListManager The current [ListManager] to be used in comparison
 * @param newListManager The new [ListManager] that'll replace the old one
 * @param areItemsTheSame A compare function to be used to determine if two items refer to the same object. This is
 * not the function to compare the contents of the items. In most cases, this should compare the ids of two items.
 * @param areContentsTheSame A compare function to be used to determine if two items has the same contents. This
 * function will be triggered for items who return true for [areItemsTheSame] and actual content comparison should be
 * made depending on the view they are used in.
 */
class ListManagerDiffCallback<T, M>(
    private val oldListManager: ListManager<T, M>?,
    private val newListManager: ListManager<T, M>,
    private val areItemsTheSame: (T, T) -> Boolean,
    private val areContentsTheSame: (T, T) -> Boolean
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldListManager?.size ?: 0
    }

    override fun getNewListSize(): Int {
        return newListManager.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // We shouldn't fetch items or load more pages prematurely when we are just trying to compare them
        val oldOne = oldListManager?.getItem(
                position = oldItemPosition,
                shouldFetchIfNull = false,
                shouldLoadMoreIfNecessary = false
        )
        val newOne = newListManager.getItem(
                position = newItemPosition,
                shouldFetchIfNull = false,
                shouldLoadMoreIfNecessary = false
        )
        if (oldOne is Marker<*> && newOne is Marker<*>) {
            return (oldOne.marker as PostListMarker).id == (newOne.marker as PostListMarker).id
        }
        if (oldOne is Marker<*> || newOne is Marker<*>) {
            return false
        }
        val oldRemoteItemId = oldListManager?.getRemoteItemId(oldItemPosition)
        val newRemoteItemId = newListManager.getRemoteItemId(newItemPosition)
        if (oldRemoteItemId != null && newRemoteItemId != null) {
            // both remote items
            return oldRemoteItemId == newRemoteItemId
        }
        val oldItem = (oldOne as Item<*>).value
        val newItem = (newOne as Item<*>).value
        if (oldItem == null || newItem == null) {
            // One remote and one local item. The remote item is not fetched yet, it can't be the same items.
            return false
        }
        // Either one remote item and one local item or both local items. In either case, we'll let the caller
        // decide how to compare the two. In most cases, a local id comparison should be enough.
        // TODO: can't erase the type! sigh..
        return areItemsTheSame(oldItem as T, newItem as T)
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // We shouldn't fetch items or load more pages prematurely when we are just trying to compare them
        val oldOne = oldListManager?.getItem(
                position = oldItemPosition,
                shouldFetchIfNull = false,
                shouldLoadMoreIfNecessary = false
        )
        val newOne = newListManager.getItem(
                position = newItemPosition,
                shouldFetchIfNull = false,
                shouldLoadMoreIfNecessary = false
        )
        if (oldOne is Marker<*> && newOne is Marker<*>) {
            return (oldOne.marker as PostListMarker).id == (newOne.marker as PostListMarker).id
        }
        if (oldOne is Marker<*> || newOne is Marker<*>) {
            return false
        }
        val oldItem = (oldOne as Item<*>).value
        val newItem = (newOne as Item<*>).value
        // If two items are null, they are the same for our intends and purposes
        if (oldItem == null && newItem == null) {
            return true
        }
        // If one of the items is null, but the other one is not, they can't be the same item
        if (oldItem == null || newItem == null) {
            return false
        }
        // TODO: can't erase the type! sigh..
        return areContentsTheSame(oldItem as T, newItem as T)
    }
}
