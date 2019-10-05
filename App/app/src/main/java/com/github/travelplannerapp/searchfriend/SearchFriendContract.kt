package com.github.travelplannerapp.searchfriend

import com.github.travelplannerapp.communication.model.UserInfo
import com.github.travelplannerapp.deleteactionmode.DeleteContract

interface SearchFriendContract {
    interface View : DeleteContract.View {
        fun showSnackbar(messageCode: Int)
        fun showAddFriend(friend: UserInfo)
        fun onDataSetChanged()
        fun closeKeyboard()
        fun showFriends()
        fun setLoadingIndicatorVisibility(isVisible: Boolean)
    }

    interface FriendItemView : DeleteContract.ItemView {
        fun setEmail(email: String)
    }

    interface Presenter : DeleteContract.Presenter {
        fun addFriend(friend: UserInfo)
        fun loadFriends()
        fun getFriendsCount(): Int
        fun addFriendIdToDelete(position: Int)
        fun removeFriendIdToDelete(position: Int)
        fun onBindFriendAtPosition(position: Int, itemView: SearchFriendContract.FriendItemView)
        fun deleteFriends()
    }
}
