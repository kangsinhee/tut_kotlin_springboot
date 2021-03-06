package sinhee.kang.tutorial.domain.post.domain.emoji.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import sinhee.kang.tutorial.domain.post.domain.emoji.Emoji
import sinhee.kang.tutorial.domain.post.domain.emoji.enums.EmojiStatus
import sinhee.kang.tutorial.domain.post.domain.post.Post
import sinhee.kang.tutorial.domain.user.domain.user.User

@Repository
interface EmojiRepository: CrudRepository<Emoji, Int> {
    fun findByUserAndPostAndStatus(user: User, post: Post, status: EmojiStatus): Emoji?
}
