package com.yechan.client;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final ChatClient chatClient;
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    public String search(String keyword) {
        String systemPrompt = buildSystemPrompt(keyword);
        return chatClient.prompt(systemPrompt)
                .call()
                .content();
    }

    public SseEmitter searchWithSse(String keyword) {
        // 3분 타임아웃 설정
        SseEmitter emitter = new SseEmitter(180000L);
        
        executorService.submit(() -> {
            try {
                sendEvent(emitter, SearchResultEvent.builder()
                        .type(SearchResultEvent.EventType.STATUS)
                        .data("검색을 시작합니다...")
                        .progress(10)
                        .build());
                sendEvent(emitter, SearchResultEvent.builder()
                        .type(SearchResultEvent.EventType.STATUS)
                        .data("GitHub 저장소를 검색 중입니다...")
                        .progress(30)
                        .build());
                String systemPrompt = buildSystemPrompt(keyword);
                sendEvent(emitter, SearchResultEvent.builder()
                        .type(SearchResultEvent.EventType.STATUS)
                        .data("GitHub 프로젝트를 분석 중입니다...")
                        .progress(60)
                        .build());

                try {
                    String result = chatClient.prompt(systemPrompt)
                            .call()
                            .content();

                    sendEvent(emitter, SearchResultEvent.builder()
                            .type(SearchResultEvent.EventType.RESULT)
                            .data(result)
                            .progress(100)
                            .build());

                    emitter.complete();
                } catch (Exception e) {
                    sendEvent(emitter, SearchResultEvent.builder()
                            .type(SearchResultEvent.EventType.ERROR)
                            .data("GitHub 프로젝트 분석 중 오류가 발생했습니다: " + e.getMessage())
                            .progress(0)
                            .build());
                    emitter.completeWithError(e);
                }
                
            } catch (Exception e) {
                try {
                    // 일반 오류 처리
                    sendEvent(emitter, SearchResultEvent.builder()
                            .type(SearchResultEvent.EventType.ERROR)
                            .data("검색 처리 중 오류가 발생했습니다: " + e.getMessage())
                            .progress(0)
                            .build());
                } catch (IOException ex) {
                    e.printStackTrace();
                }
                emitter.completeWithError(e);
            }
        });
        
        return emitter;
    }

    private void sendEvent(SseEmitter emitter, SearchResultEvent event) throws IOException {
        emitter.send(SseEmitter.event()
                .name(event.getType().toString().toLowerCase())
                .data(event));
    }

    private String buildSystemPrompt(String keyword) {
        return """
                system:
                너는 사용자가 특정 기술을 학습하기 좋은 GitHub 프로젝트를 추천해 주는 어시스턴트야.
                Tool의 결과로 받은 저장소의 정보를 바탕으로, 사용자가 제시한 키워드에 맞는 GitHub 프로젝트를 심층 분석하고 추천해줘.
                모든 대답은 반드시 **한국어**로 작성해야 해.
                
                ## 사용 가능한 도구
                1. client_server_search_in_github: 키워드로 GitHub 저장소를 검색합니다.
                   * 필수 매개변수: keyword (검색 키워드)
                
                2. client_github_get_file_contents: GitHub 저장소에서 특정 파일의 내용을 가져옵니다.
                   * 필수 매개변수: path (파일 경로), repo (저장소 이름), owner (저장소 소유자)
                   * 선택 매개변수: branch (브랜치 이름)
                
                3. client_github_search_code: GitHub 저장소 내 코드를 검색합니다.
                   * 필수 매개변수: q (검색 쿼리)
                   * 선택 매개변수: order (정렬 순서), page (페이지 번호), per_page (페이지당 결과 수)
                
                ## 분석 프로세스
                1. 먼저 `client_server_search_in_github` 도구를 사용해 키워드 "%s"로 관련 GitHub 저장소를 검색해.
                   * 이 도구는 각 저장소마다 Repo(저장소 이름), URL, Stars(Star 개수), description(설명) 등의 정보를 반환해.
                
                2. 검색 결과에서 관심 있는 저장소들을 선별한 후, 각 저장소에 대해 다음 도구들을 활용해 심층 분석할 수 있어:
                   * `client_github_get_file_contents`로 README.md 파일을 확인해 프로젝트 개요 파악
                     - 예: client_github_get_file_contents(owner: "검색된_소유자", repo: "검색된_저장소명", path: "README.md")
                   * `client_github_search_code`로 저장소 내에서 특정 코드 패턴 검색
                
                3. 다양한 저장소를 비교 분석한 후, 다음 기준으로 최대 5개의 프로젝트를 선정해:
                   * Star 수(인기도)
                   * 코드 품질 및 가독성
                   * 문서화 수준 (README.md, 주석 등)
                   * 초보자가 학습하기에 적합한 난이도
                
                ## 출력 포맷
                결과는 다음과 같은 간단한 HTML 형식으로 각 프로젝트를 표시해야 합니다:
                
                <h3>프로젝트 1: [프로젝트 이름]</h3>
                <p><strong>URL:</strong> [URL만 입력, HTML 태그 사용하지 말것]</p>
                <p><strong>Star 수:</strong> [숫자만 입력]</p>
                <p><strong>설명:</strong> [간략한 설명]</p>
                <p><strong>추천 이유:</strong></p>
                <ul>
                  <li>[추천 이유 1]</li>
                  <li>[추천 이유 2]</li>
                </ul>
                <p><strong>학습 포인트:</strong></p>
                <ul>
                  <li>[학습 포인트 1]</li>
                  <li>[학습 포인트 2]</li>
                </ul>
                <p><strong>시작점 추천:</strong> [간략한 시작점 설명, URL은 텍스트로만 제공하고 HTML 태그 사용하지 말것]</p>
                
                URL과 경로는 항상 HTML 태그 없이 순수 텍스트로만 제공하세요. 예를 들어 <a> 태그를 사용하거나 "target", "class" 등의 속성을 추가하지 말고, 단순히 "https://github.com/example/repo" 형식으로 작성하세요.
                
                README.md 파일을 참조할 때도 마찬가지로 HTML 태그를 사용하지 말고 단순한 텍스트로 URL을 제공하세요.
                
                모든 설명은 반드시 **한국어**로 작성하고, 기술적 정확성을 유지해.
                assistant:
                지금부터 "%s" 키워드를 사용해 GitHub 프로젝트를 검색하고 분석해 드리겠습니다. 먼저 `client_server_search_in_github` 도구를 활용해 관련 저장소를 검색해보겠습니다.
                """.formatted(keyword, keyword);
    }
}
