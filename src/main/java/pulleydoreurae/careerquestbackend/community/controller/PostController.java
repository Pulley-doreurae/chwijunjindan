package pulleydoreurae.careerquestbackend.community.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import pulleydoreurae.careerquestbackend.common.dto.response.SimpleResponse;
import pulleydoreurae.careerquestbackend.community.domain.dto.request.PostRequest;
import pulleydoreurae.careerquestbackend.community.domain.dto.response.PostFailResponse;
import pulleydoreurae.careerquestbackend.community.domain.dto.response.PostResponse;
import pulleydoreurae.careerquestbackend.community.service.PostService;

/**
 * 게시글을 담당하는 Controller
 *
 * @author : parkjihyeok
 * @since : 2024/03/28
 */
@RestController
@RequestMapping("/api")
public class PostController {

	private final PostService postService;

	@Autowired
	public PostController(PostService postService) {
		this.postService = postService;
	}

	@GetMapping("/posts")
	public ResponseEntity<List<PostResponse>> getPostList(@PageableDefault(size = 15) Pageable pageable) {
		List<PostResponse> posts = postService.getPostResponseList(pageable);

		return ResponseEntity.status(HttpStatus.OK)
				.body(posts);
	}

	@GetMapping("/posts/category/{category}")
	public ResponseEntity<List<PostResponse>> getPostListByCategory(@PathVariable Long category,
			@PageableDefault(size = 15) Pageable pageable) {

		List<PostResponse> posts = postService.getPostResponseListByCategory(category, pageable);

		return ResponseEntity.status(HttpStatus.OK)
				.body(posts);
	}

	@GetMapping("/posts/user/{userId}")
	public ResponseEntity<?> getPostListByUserId(@PathVariable String userId,
			@PageableDefault(size = 15) Pageable pageable) {

		List<PostResponse> posts = postService.getPostListByUserAccount(userId, pageable);

		if (posts == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(SimpleResponse.builder()
							.msg("해당 사용자 정보를 찾을 수 없습니다.")
							.build());
		}
		return ResponseEntity.status(HttpStatus.OK)
				.body(posts);
	}

	@GetMapping("/posts/search")
	public ResponseEntity<List<PostResponse>> searchPosts(@RequestParam(name = "keyword") String keyword,
			@RequestParam(name = "category", required = false) Long category,
			@PageableDefault(size = 15, direction = Sort.Direction.DESC) Pageable pageable) {

		List<PostResponse> posts = postService.searchPosts(keyword, category, pageable);

		return ResponseEntity.status(HttpStatus.OK)
				.body(posts);
	}

	@GetMapping("/posts/{postId}")
	public ResponseEntity<?> getPost(HttpServletRequest request, HttpServletResponse response,
			@PathVariable Long postId) {

		PostResponse post = postService.findByPostId(request, response, postId);

		if (post == null) { // 게시글을 찾을 수 없다면
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(SimpleResponse.builder()
							.msg("해당 게시글을 찾을 수 없습니다.")
							.build());
		}

		return ResponseEntity.status(HttpStatus.OK)
				.body(post);
	}

	@PostMapping("/posts")
	public ResponseEntity<?> savePost(@Valid @RequestBody PostRequest postRequest,
			BindingResult bindingResult) {

		// 검증
		ResponseEntity<PostFailResponse> BAD_REQUEST = validCheck(postRequest, bindingResult);
		if (BAD_REQUEST != null) {
			return BAD_REQUEST;
		}

		if (!postService.savePost(postRequest)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(SimpleResponse.builder()
							.msg("게시글 저장에 실패했습니다.")
							.build());
		}

		return ResponseEntity.status(HttpStatus.OK)
				.body(SimpleResponse.builder()
						.msg("게시글 등록에 성공했습니다.")
						.build());
	}

	@PatchMapping("/posts/{postId}")
	public ResponseEntity<?> updatePost(@PathVariable Long postId,
			@Valid @RequestBody PostRequest postRequest, BindingResult bindingResult) {

		// 검증
		ResponseEntity<PostFailResponse> BAD_REQUEST = validCheck(postRequest, bindingResult);
		if (BAD_REQUEST != null) {
			return BAD_REQUEST;
		}

		if (!postService.updatePost(postId, postRequest)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(SimpleResponse.builder()
							.msg("해당 게시글을 수정할 수 없습니다.")
							.build());
		}
		return ResponseEntity.status(HttpStatus.OK)
				.body(SimpleResponse.builder()
						.msg("게시글 수정에 성공했습니다.")
						.build());
	}

	@DeleteMapping("/posts/{postId}")
	public ResponseEntity<SimpleResponse> deletePost(@PathVariable Long postId, String userId) {
		if (!postService.deletePost(postId, userId)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(SimpleResponse.builder()
							.msg("해당 게시글을 삭제할 수 없습니다.")
							.build());
		}
		return ResponseEntity.status(HttpStatus.OK)
				.body(SimpleResponse.builder()
						.msg("게시글 삭제에 성공하였습니다.")
						.build());
	}

	/**
	 * 검증 메서드
	 *
	 * @param postRequest   게시글 요청 (검증에 실패하더라도 입력한 값은 그대로 돌려준다.)
	 * @param bindingResult 검증 결과
	 * @return 검증결과 에러가 없다면 null 에러가 있다면 해당 에러를 담은 ResponseEntity 반환
	 */
	private ResponseEntity<PostFailResponse> validCheck(PostRequest postRequest, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			String[] errors = new String[bindingResult.getAllErrors().size()];
			int index = 0;
			for (ObjectError error : bindingResult.getAllErrors()) {
				errors[index++] = error.getDefaultMessage();
			}

			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(PostFailResponse.builder()
							.title(postRequest.getTitle())
							.content(postRequest.getContent())
							.category(postRequest.getCategory())
							.errors(errors)
							.build());
		}
		return null;
	}
}
