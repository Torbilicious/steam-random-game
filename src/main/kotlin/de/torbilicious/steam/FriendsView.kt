package de.torbilicious.steam

import com.github.koraktor.steamcondenser.steam.community.SteamId
import javafx.scene.control.ScrollPane
import javafx.scene.layout.VBox
import tornadofx.*

class FriendsView(private val friends: List<SteamId>) : View("Friends") {
    override val root = ScrollPane()
//    private var pi: ProgressIndicator by singleAssign()
    private var friendsBox: VBox by singleAssign()

    init {
        ensureLoaded(friends)

        with(root) {
            paddingAll = 10

//            pi = progressindicator()

                hbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED

                friendsBox = vbox(spacing = 5)
        }

        showFriends()
    }

    private fun showFriends() {
        with(friendsBox) {
            friends.forEach {
                hbox(spacing = 10) {
                    imageview(it.avatarIconUrl)
                    label(it.nickname)
                }
            }
        }
    }

    private fun ensureLoaded(friends: List<SteamId>) {
        friends.forEach {
            if (!it.isFetched) {
                it.fetchData()
            }
        }
    }
}