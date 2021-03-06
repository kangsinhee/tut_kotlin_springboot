package sinhee.kang.tutorial.domain.post.domain.view

import sinhee.kang.tutorial.domain.post.domain.post.Post
import sinhee.kang.tutorial.domain.user.domain.user.User
import java.time.LocalDateTime
import javax.persistence.*

@Entity(name = "tbl_view")
data class View (
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        val Id: Int = 0,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user", nullable = true)
        val user: User,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "post", nullable = true)
        val post: Post,

        @Column(nullable = false)
        val createdAt: LocalDateTime = LocalDateTime.now()
)
