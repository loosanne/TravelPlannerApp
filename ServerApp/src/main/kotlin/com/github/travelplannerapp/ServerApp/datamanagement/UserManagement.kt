package com.github.travelplannerapp.ServerApp.datamanagement

import com.github.travelplannerapp.ServerApp.db.dao.User
import com.github.travelplannerapp.ServerApp.db.dao.UserFriend
import com.github.travelplannerapp.ServerApp.db.merge
import com.github.travelplannerapp.ServerApp.db.repositories.UserFriendRepository
import com.github.travelplannerapp.ServerApp.db.repositories.UserRepository
import com.github.travelplannerapp.ServerApp.exceptions.*
import com.github.travelplannerapp.communication.commonmodel.SignInRequest
import com.github.travelplannerapp.communication.commonmodel.SignUpRequest
import com.github.travelplannerapp.communication.commonmodel.UserInfo
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate
import java.util.*

@Component
class UserManagement : IUserManagement {

    @Autowired
    lateinit var userRepository: UserRepository
    @Autowired
    lateinit var userFriendRepository: UserFriendRepository

    private val ACCESS_TOKEN_SUB = "AccessToken"
    private val ACCESS_TOKEN_ISSUER = "TravelApp_Server"
    private val SECRET_KEY =
            "RdY6EVfEJdMIdxTkUYkZWS3QL9PFrAjxgQXrLloba20BBe4qNaDN9coybj9J5Z6JoVfSt8DepQRyQKbvgpveS8oZUnIknFJsKuDYJ4McQgCm5rZCMpy67EXqxJufoNaDAMhEAkYQhNe3kXfObgmhD6S01v235we6AJ7XITamkhzbzDjx7tmolm6IZYkzkEEEzVWk4ZhotVDP2s2iL5teTe0to7jGNQxrXrU8y3qxEFIjGDfAY7YlrayntqssnbLW"


    override fun getUserId(token: String): Int {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).body["id"].toString().toInt()
    }

    override fun verifyUser(token: String) {
        val expirationDate = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).body.expiration

        if (expirationDate.before(Date.from(Instant.now()))) {
            throw AuthorizationException("Token expired")
        }
    }

    override fun authenticateUser(request: SignInRequest): Int {
        val user = userRepository.getUserByEmail(request.email)
        if (user == null || user.password != request.password) {
            throw WrongCredentialsException("Wrong email or password")
        }
        return user.id!!
    }

    override fun updateAuthorizationToken(id: Int, request: SignInRequest): String {
        val claims: HashMap<String, Any?> = HashMap()

        claims["iss"] = ACCESS_TOKEN_ISSUER
        claims["sub"] = ACCESS_TOKEN_SUB
        claims["email"] = request.email
        claims["id"] = id
        claims["generatedTimestamp"] = LocalDate.now()

        val expiryDate = Instant.now().plusSeconds(3600 * 24)

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(Date.from(expiryDate))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact()
    }

    override fun addUser(request: SignUpRequest) {
        val user = userRepository.getUserByEmail(request.email)
        if (user != null) throw EmailAlreadyExistsException("User with given email already exists")

        val userId = userRepository.getNextId()
        val newUser = User(userId, request.email, request.password)
        userRepository.add(newUser)
    }

    override fun updateUser(id: Int, changes: MutableMap<String, Any?>) {
        val user = userRepository.get(id)
        val userChanges = User(changes)
        val updatedUser = userChanges merge user!!
        userRepository.update(updatedUser)
    }

    override fun addFriend(userId: Int, friend: UserInfo): UserInfo {
        val userFriendId = userFriendRepository.getNextId()
        val userFriend = UserFriend(userFriendId, userId, friend.id)
        if (userFriendRepository.add(userFriend)) {
            return friend
        } else {
            throw AddFriendException("Error when adding friend")
        }
    }

    override fun deleteFriends(userId: Int, friendsIds: MutableSet<Int>) {
        for (friendId in friendsIds) {
            if (!userFriendRepository.deleteUserFriendBinding(userId, friendId)) {
                throw  DeleteFriendsException("Error when deleting friends")
            }
        }
    }

    override fun findMatchingEmails(userId: Int, query: String): MutableList<UserInfo> {
        val userInfos = mutableListOf<UserInfo>()
        val users = userRepository.findMatchingEmails(query, userId)
        users.forEach { matchingUser ->
            userInfos.add(UserInfo(matchingUser.id!!, matchingUser.email!!))
        }
        return userInfos
    }

    override fun getAllFriendsByUserId(userId: Int): MutableList<UserInfo> {
        val userInfos = mutableListOf<UserInfo>()
        val friends = userRepository.getAllFriendsByUserId(userId)
        friends.forEach { user ->
            userInfos.add(UserInfo(user.id!!, user.email!!))
        }
        return userInfos
    }

    override fun getFriendsBySharedTravel(userId: Int, travelId: Int, selectFriendsWithAccess: Boolean): MutableList<UserInfo> {
        val userInfos = mutableListOf<UserInfo>()
        val friendsWithoutAccess = userRepository.getFriendsBySharedTravel(userId, travelId, selectFriendsWithAccess)
        friendsWithoutAccess.forEach { friend ->
            userInfos.add(UserInfo(friend.id!!, friend.email!!))
        }
        return userInfos
    }
}
