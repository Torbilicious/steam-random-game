//package de.torbilicious.steam
//
//import com.github.koraktor.steamcondenser.steam.community.SteamId
//import javafx.beans.property.SimpleBooleanProperty
//import javafx.scene.control.ProgressIndicator
//import javafx.scene.control.ScrollPane
//import javafx.scene.layout.VBox
//import tornadofx.*
//
//class FriendsView(private val friends: List<SteamId>) : View("Friends") {
//    override val root = ScrollPane()
//    private var pi: ProgressIndicator by singleAssign()
//    private var friendsBox: VBox by singleAssign()
//
//    private val friendsLoaded = SimpleBooleanProperty(false)
//
//    init {
//        with(root) {
//            paddingAll = 10
//
//            pi = progressindicator {
//                removeWhen { friendsLoaded }
//            }
//
//            hbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
//
//            friendsBox = vbox(spacing = 5)
//        }
//    }
//
//    fun showFriends() {
//        with(friendsBox) {
//            friends.forEach {
//                hbox(spacing = 10) {
//                    ensureLoaded(it)
//                    checkbox()
//                    imageview(it.avatarIconUrl)
//                    label(it.nickname)
//                }
//            }
//        }
//
//        friendsLoaded.set(true)
//    }
//
//    private fun ensureLoaded(friend: SteamId) {
//        if (!friend.isFetched) {
//            friend.fetchData()
//        }
//    }
//}