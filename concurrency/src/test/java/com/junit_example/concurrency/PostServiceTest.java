package com.junit_example.concurrency;

import com.junit_example.utils.concurrency.ConcurrencyTestUtils;
import com.junit_example.concurrency.model.Post;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class PostServiceTest {

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private PostService postService;


  Post post;

  @BeforeEach
  public void setup() {
    post = Post.builder().title("Dummy post").build();
    postRepository.save(post);
  }

  @AfterEach
  public void cleanup() {
    post = null;
    postRepository.deleteAll();
  }

  @Test
  public void 한_게시글을_1회_조회하면_조회수가_1증가한다() {
    long postId = post.getPostId();

    Post beforeQuery = postRepository.findById(postId).get();
    postService.getPost(postId);
    Post afterQuery = postRepository.findById(postId).get();

    assertEquals(beforeQuery.getViewCount() + 1, afterQuery.getViewCount());
  }

  @Test
  public void 한_게시글을_10회_조회_할_경우_조회수가_10이_증가한다() {
    long postId = post.getPostId();

    Post beforeQuery = postRepository.findById(postId).get();

    for (int i = 0; i < 10; i++) {
      postService.getPost(postId);
    }

    Post afterQuery = postRepository.findById(postId).get();

    assertEquals(beforeQuery.getViewCount() + 10, afterQuery.getViewCount());
  }

  @Test
  public void 한_게시글을_동시에_10명의_유저가_조회를_할_경우_조회수가_10이_증가한다() {
    long postId = post.getPostId();
    int concurrencyCount = 1000;

    Post beforeQuery = postRepository.findById(postId).get();

    ConcurrencyTestUtils.executeConcurrently(
            () -> postService.getPost(postId), concurrencyCount);

    Post afterQuery = postRepository.findById(postId).get();

    assertEquals(beforeQuery.getViewCount() + concurrencyCount, afterQuery.getViewCount());
  }

}
