package sinhee.kang.tutorial.domain.post.service.comment

import org.springframework.stereotype.Service
import sinhee.kang.tutorial.domain.auth.service.auth.AuthService
import sinhee.kang.tutorial.domain.post.domain.comment.Comment
import sinhee.kang.tutorial.domain.post.domain.comment.repository.CommentRepository
import sinhee.kang.tutorial.domain.post.domain.post.repository.PostRepository
import sinhee.kang.tutorial.domain.post.domain.subComment.SubComment
import sinhee.kang.tutorial.domain.post.domain.subComment.repository.SubCommentRepository
import sinhee.kang.tutorial.domain.post.dto.request.CommentRequest
import sinhee.kang.tutorial.global.exception.exceptions.notFound.ApplicationNotFoundException
import sinhee.kang.tutorial.global.exception.exceptions.unAuthorized.PermissionDeniedException
import sinhee.kang.tutorial.domain.user.domain.user.enums.AccountRole

@Service
class CommentServiceImpl(
    private val authService: AuthService,

    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val subCommentRepository: SubCommentRepository
): CommentService {

    override fun uploadComment(postId: Int, commentRequest: CommentRequest): Int {
        val user = authService.verifyCurrentUser()
        val post = postRepository.findById(postId)
            .orElseThrow { ApplicationNotFoundException() }

        val commentContent = commentRequest.content

        val comment = commentRepository.save(
            Comment(user = user, post = post, content = commentContent)
        )

        return comment.commentId
    }

    override fun updateComment(commentId: Int, commentRequest: CommentRequest): Int {
        val user = authService.verifyCurrentUser()
        val comment = commentRepository.findById(commentId)
            .orElseThrow { ApplicationNotFoundException() }

        val newContent = commentRequest.content

        if (comment.user == user || user.isRoles(AccountRole.ADMIN))
            commentRepository.save(comment.update(newContent))
        else throw PermissionDeniedException()

        return comment.commentId
    }

    override fun removeComment(commentId: Int) {
        val user = authService.verifyCurrentUser()

        commentRepository.findById(commentId)
            .orElseThrow { ApplicationNotFoundException() }
            .takeIf { it.user == user || user.isRoles(AccountRole.ADMIN) }
            ?.also { commentRepository.deleteById(it.commentId) }
            ?: throw PermissionDeniedException()
    }

    override fun uploadSubComment(commentId: Int, commentRequest: CommentRequest): Int {
        val user = authService.verifyCurrentUser()
        val comment = commentRepository.findById(commentId)
            .orElseThrow{ ApplicationNotFoundException() }

        val commentContent = commentRequest.content

        val subComment = subCommentRepository.save(
            SubComment(user = user, comment = comment, content = commentContent)
        )
        return subComment.subCommentId
    }

    override fun updateSubComment(subCommentId: Int, commentRequest: CommentRequest): Int {
        val user = authService.verifyCurrentUser()
        val subComment = subCommentRepository.findById(subCommentId)
            .orElseThrow { ApplicationNotFoundException() }

        val newContent = commentRequest.content

        if (subComment.user == user || user.isRoles(AccountRole.ADMIN)) {
            subCommentRepository.save(subComment.update(newContent))
        } else throw PermissionDeniedException()

        return subComment.subCommentId
    }

    override fun removeSubComment(subCommentId: Int) {
        val user = authService.verifyCurrentUser()

        subCommentRepository.findById(subCommentId)
            .orElseThrow { ApplicationNotFoundException() }
            .takeIf { it.user == user || user.isRoles(AccountRole.ADMIN) }
            ?.also { subCommentRepository.deleteById(it.subCommentId) }
            ?: throw PermissionDeniedException()
    }
}
