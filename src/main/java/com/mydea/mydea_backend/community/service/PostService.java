package com.mydea.mydea_backend.community.service;

import com.mydea.mydea_backend.account.domain.Account;
import com.mydea.mydea_backend.account.repo.AccountRepository;
import com.mydea.mydea_backend.community.domain.*;
import com.mydea.mydea_backend.community.repo.*;
import com.mydea.mydea_backend.work.domain.Work;
import com.mydea.mydea_backend.work.repo.WorkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PostService {
    private final CommunityPostRepository posts;
    // Like/Comment/Copy services will be wired when features are implemented
    private final WorkRepository works;
    private final AccountRepository accounts;

    @Transactional
    public Long create(Long authorId, Long workId, String title, String description,
            boolean allowCopy, boolean allowComments) {
        // 소유 검증
        Work work = works.findById(workId)
                .orElseThrow(() -> new NoSuchElementException("작업물을 찾을 수 없습니다."));
        if (!authorId.equals(work.getUserId()))
            throw new SecurityException("본인 작업물만 게시할 수 있습니다.");

        Account author = accounts.findById(authorId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        CommunityPost post = CommunityPost.builder()
                .work(work)
                .author(author)
                .title(title)
                .description(description)
                .visibility(CommunityVisibility.PUBLIC)
                .status(CommunityPostStatus.ACTIVE)
                .allowCopy(allowCopy)
                .allowComments(allowComments)
                .snapshotName(work.getName())
                .snapshotThumbUrl(work.getPreviewUrl())
                .snapshotPayload(null) // TODO: 필요 시 Work 스냅샷 JSON 직렬화
                .build();

        posts.save(post);
        return post.getPostId();
    }

    @Transactional(readOnly = true)
    public CommunityPost getPublic(Long postId) {
        return posts.findByPostIdAndStatus(postId, CommunityPostStatus.ACTIVE)
                .orElseThrow(() -> new NoSuchElementException("게시물을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Page<CommunityPost> feed(Pageable pageable) {
        return posts.findByVisibilityAndStatusOrderByPublishedAtDesc(
                CommunityVisibility.PUBLIC, CommunityPostStatus.ACTIVE, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CommunityPost> myPosts(Long authorId, Pageable pageable) {
        return posts.findByAuthorIdAndStatusOrderByCreatedAtDesc(authorId, CommunityPostStatus.ACTIVE, pageable);
    }

    @Transactional
    public void updateFlags(Long authorId, Long postId, CommunityVisibility visibility,
            Boolean allowCopy, Boolean allowComments, String title, String description) {
        CommunityPost post = posts.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("게시물을 찾을 수 없습니다."));
        if (!post.getAuthor().getId().equals(authorId))
            throw new SecurityException("본인 게시물만 수정할 수 있습니다.");

        if (visibility != null)
            post.setVisibility(visibility);
        if (allowCopy != null)
            post.setAllowCopy(allowCopy);
        if (allowComments != null)
            post.setAllowComments(allowComments);
        if (title != null)
            post.setTitle(title);
        if (description != null)
            post.setDescription(description);
    }

    @Transactional
    public void delete(Long authorId, Long postId) {
        CommunityPost post = posts.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("게시물을 찾을 수 없습니다."));
        if (!post.getAuthor().getId().equals(authorId))
            throw new SecurityException("본인 게시물만 삭제할 수 있습니다.");
        post.setStatus(CommunityPostStatus.REMOVED);
    }
}
