package com.yechan.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Controller
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;
    private final ResponseFormatter responseFormatter;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/search")
    public String searchForm() {
        return "search";
    }

    @PostMapping("/search")
    public String search(@RequestParam String keyword, Model model) {
        String rawResult = searchService.search(keyword);
        String formattedResult = responseFormatter.formatToHtml(rawResult);
        
        model.addAttribute("keyword", keyword);
        model.addAttribute("result", formattedResult);
        return "search-result";
    }

    @GetMapping(path = "/api/search/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSearch(@RequestParam String keyword) {
        return searchService.searchWithSse(keyword);
    }
}
