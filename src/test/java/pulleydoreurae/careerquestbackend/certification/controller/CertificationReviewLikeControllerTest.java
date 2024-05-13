package pulleydoreurae.careerquestbackend.certification.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.google.gson.Gson;

import pulleydoreurae.careerquestbackend.basiccommunity.controller.BasicPostLikeController;
import pulleydoreurae.careerquestbackend.common.community.domain.dto.request.PostLikeRequest;
import pulleydoreurae.careerquestbackend.common.community.domain.dto.response.PostResponse;
import pulleydoreurae.careerquestbackend.common.community.service.PostLikeService;

/**
 * 자격증 좋아요 컨트롤러 테스트
 *
 * @author : parkjihyeok
 * @since : 2024/05/13
 */
@WebMvcTest(CertificationReviewLikeController.class)
@AutoConfigureRestDocs
class CertificationReviewLikeControllerTest {

	@Autowired
	MockMvc mockMvc;
	@MockBean
	@Qualifier("certificationReviewLikeService")
	PostLikeService postLikeService;
	Gson gson = new Gson();

	@Test
	@DisplayName("좋아요 상태 변환 실패")
	@WithMockUser
	void changePostLikeFailTest() throws Exception {
		// Given
		PostLikeRequest request = PostLikeRequest.builder().postId(10000L).userId("testId").isLiked(0).build();
		given(postLikeService.changePostLike(any())).willReturn(false);

		// When
		mockMvc.perform(post("/api/certifications/reviews/likes")
						.contentType(MediaType.APPLICATION_JSON)
						.with(csrf())
						.content(gson.toJson(request)))
				.andExpect(status().isBadRequest())
				.andDo(print())
				.andDo(document("{class-name}/{method-name}/",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestFields(
								fieldWithPath("postId").description("좋아요 상태를 변환할 게시글 id"),
								fieldWithPath("userId").description("요청자 id"),
								fieldWithPath("isLiked").description("현재 좋아요 상태")
						),
						responseFields(
								fieldWithPath("msg").description("요청에 대한 처리 결과")
						)));

		// Then
	}

	@Test
	@DisplayName("좋아요 상태 변환 성공")
	@WithMockUser
	void changePostLikeSuccessTest() throws Exception {
		// Given
		PostLikeRequest request = PostLikeRequest.builder().postId(10000L).userId("testId").isLiked(0).build();
		given(postLikeService.changePostLike(any())).willReturn(true);

		// When
		mockMvc.perform(post("/api/certifications/reviews/likes")
						.contentType(MediaType.APPLICATION_JSON)
						.with(csrf())
						.content(gson.toJson(request)))
				.andExpect(status().isOk())
				.andDo(print())
				.andDo(document("{class-name}/{method-name}/",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestFields(
								fieldWithPath("postId").description("좋아요 상태를 변환할 게시글 id"),
								fieldWithPath("userId").description("요청자 id"),
								fieldWithPath("isLiked").description("현재 좋아요 상태")
						),
						responseFields(
								fieldWithPath("msg").description("요청에 대한 처리 결과")
						)));

		// Then
	}

	@Test
	@DisplayName("한 사용자가 좋아요 누른 게시글 리스트 반환 테스트")
	@WithMockUser
	void findAllPostLikeByUserAccountTest() throws Exception {
		// Given
		PostResponse postResponse1 = PostResponse.builder()
				.userId("testId").title("제목1").content("내용1").view(0L).postLikeCount(0L).category(1L)
				.isLiked(0)
				.build();
		PostResponse postResponse2 = PostResponse.builder()
				.userId("testId").title("제목2").content("내용2").view(0L).postLikeCount(0L).category(1L)
				.isLiked(0)
				.build();
		PostResponse postResponse3 = PostResponse.builder()
				.userId("testId").title("제목3").content("내용3").view(0L).postLikeCount(0L).category(1L)
				.isLiked(0)
				.build();
		PostResponse postResponse4 = PostResponse.builder()
				.userId("testId").title("제목4").content("내용4").view(0L).postLikeCount(0L).category(1L)
				.isLiked(0)
				.build();
		PostResponse postResponse5 = PostResponse.builder()
				.userId("testId").title("제목5").content("내용5").view(0L).postLikeCount(0L).category(1L)
				.isLiked(0)
				.build();

		given(postLikeService.findAllPostLikeByUserAccount(any(), any())).willReturn(
				List.of(postResponse1, postResponse2, postResponse3, postResponse4, postResponse5));

		// When
		mockMvc.perform(get("/api/certifications/likes/{userId}", "testId")
						.queryParam("page", "0")
						.with(csrf()))
				.andExpect(status().isOk())
				.andDo(print())
				.andDo(document("{class-name}/{method-name}/",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						pathParameters(
								parameterWithName("userId").description("요청자 id")
						),
						queryParameters(
								parameterWithName("page").description("페이지 정보 (0부터 시작)")
						),
						responseFields(
								fieldWithPath("[].userId").description("게시글 작성자"),
								fieldWithPath("[].title").description("제목"),
								fieldWithPath("[].content").description("내용"),
								fieldWithPath("[].images").description("사진 리스트"),
								fieldWithPath("[].category").description("카테고리"),
								fieldWithPath("[].view").description("조회수"),
								fieldWithPath("[].commentCount").description("댓글 수"),
								fieldWithPath("[].postLikeCount").description("좋아요 수"),
								fieldWithPath("[].isLiked").description("좋아요 상태 (리스트에선 상관 X)"),
								fieldWithPath("[].createdAt").description("작성일자"),
								fieldWithPath("[].modifiedAt").description("수정일자")
						)));

		// Then
	}

	@Test
	@DisplayName("좋아요 상태 변환 검증 실패(postId 없음)")
	@WithMockUser
	void changePostLikeValidFail1Test() throws Exception {
		// Given
		PostLikeRequest request = PostLikeRequest.builder().userId("testId").isLiked(0).build();

		// When
		mockMvc.perform(post("/api/certifications/reviews/likes")
						.contentType(MediaType.APPLICATION_JSON)
						.with(csrf())
						.content(gson.toJson(request)))
				.andExpect(status().isBadRequest())
				.andDo(print())
				.andDo(document("{class-name}/{method-name}/",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestFields(
								fieldWithPath("userId").description("요청자 id"),
								fieldWithPath("isLiked").description("현재 좋아요 상태")
						),
						responseFields(
								fieldWithPath("msg").description("요청에 대한 처리 결과")
						)));

		// Then
	}

	@Test
	@DisplayName("좋아요 상태 변환 검증 실패(userId 없음)")
	@WithMockUser
	void changePostLikeValidFail2Test() throws Exception {
		// Given
		PostLikeRequest request = PostLikeRequest.builder().postId(10000L).userId("").isLiked(0).build();

		// When
		mockMvc.perform(post("/api/certifications/reviews/likes")
						.contentType(MediaType.APPLICATION_JSON)
						.with(csrf())
						.content(gson.toJson(request)))
				.andExpect(status().isBadRequest())
				.andDo(print())
				.andDo(document("{class-name}/{method-name}/",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestFields(
								fieldWithPath("postId").description("좋아요 상태를 변환할 게시글 id"),
								fieldWithPath("userId").description("요청자 id"),
								fieldWithPath("isLiked").description("현재 좋아요 상태")
						),
						responseFields(
								fieldWithPath("msg").description("요청에 대한 처리 결과")
						)));

		// Then
	}

	@Test
	@DisplayName("좋아요 상태 변환 검증 실패(isLiked 없음)")
	@WithMockUser
	void changePostLikeValidFail3Test() throws Exception {
		// Given
		PostLikeRequest request = PostLikeRequest.builder().postId(10000L).userId("").build();

		// When
		mockMvc.perform(post("/api/certifications/reviews/likes")
						.contentType(MediaType.APPLICATION_JSON)
						.with(csrf())
						.content(gson.toJson(request)))
				.andExpect(status().isBadRequest())
				.andDo(print())
				.andDo(document("{class-name}/{method-name}/",
						preprocessRequest(prettyPrint()),
						preprocessResponse(prettyPrint()),
						requestFields(
								fieldWithPath("postId").description("좋아요 상태를 변환할 게시글 id"),
								fieldWithPath("userId").description("요청자 id")
						),
						responseFields(
								fieldWithPath("msg").description("요청에 대한 처리 결과")
						)));

		// Then
	}
}
