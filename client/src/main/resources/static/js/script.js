document.addEventListener('DOMContentLoaded', function() {
    // 검색 양식 제출 시 로딩 표시
    const searchForm = document.getElementById('searchForm');
    if (searchForm) {
        searchForm.addEventListener('submit', function() {
            const loadingElement = document.querySelector('.loading');
            if (loadingElement) {
                loadingElement.style.display = 'block';
            }
            
            // 검색 시간이 오래 걸릴 수 있으므로 버튼 비활성화
            const submitButton = searchForm.querySelector('button[type="submit"]');
            if (submitButton) {
                submitButton.disabled = true;
                submitButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 검색 중...';
            }
        });
    }
    
    // 인기 키워드 클릭 시 검색 필드에 설정
    const popularKeywords = document.querySelectorAll('.popular-keyword');
    popularKeywords.forEach(keyword => {
        keyword.addEventListener('click', function() {
            const keywordInput = document.getElementById('keyword');
            if (keywordInput) {
                keywordInput.value = this.textContent.trim();
                // 자동으로 포커스 설정
                keywordInput.focus();
            }
        });
    });
    
    // 결과 페이지에서 결과 포맷팅
    const resultContent = document.querySelector('.result-content');
    if (resultContent) {
        // 모든 문자열 내에서 이중 URL이나 깨진 링크 수정
        const htmlContent = resultContent.innerHTML;
        
        // 1. 먼저 깨진 URL 링크 패턴 수정 (고급 패턴 매칭)
        let fixedHtml = htmlContent.replace(
            /([^"'>]+)" target="_blank" class="text-primary">([^<]+)<\/a>/g,
            function(match, part1, part2) {
                // URL 부분만 추출
                let url;
                if (part2.includes('http')) {
                    url = part2.match(/(https?:\/\/[^\s<>"]+)/);
                    if (url) {
                        return '<a href="' + url[0] + '" target="_blank" class="text-primary">' + url[0] + '</a>';
                    }
                }
                // URL이 part1에 있는 경우
                url = part1.match(/(https?:\/\/[^\s<>"]+)/);
                if (url) {
                    return '<a href="' + url[0] + '" target="_blank" class="text-primary">' + url[0] + '</a>';
                }
                return match;
            }
        );
        
        // 2. URL 태그가 깨진 경우 수정
        fixedHtml = fixedHtml.replace(
            /URL:<\/strong> ([^<]+)" target="_blank/g,
            'URL:</strong> <a href="$1" target="_blank'
        );
        
        // 3. README.md 링크 수정
        fixedHtml = fixedHtml.replace(
            /README\.md" target="_blank" class="text-primary">([^<]+)">README\.md/g,
            function(match, url) {
                if (url.includes('http')) {
                    return '<a href="' + url + '" target="_blank" class="text-primary">README.md</a>';
                }
                return match;
            }
        );
        
        // 4. 일반적인 URL 텍스트를 링크로 변환
        fixedHtml = fixedHtml.replace(
            /(?<!href="|">)(https?:\/\/[^\s"<]+)(?!<\/a>)/g, 
            '<a href="$1" target="_blank" class="text-primary">$1</a>'
        );
        
        // 5. Star 수에 별 이모지 추가
        fixedHtml = fixedHtml.replace(
            /Star 수:<\/strong>\s*(\d+)/g, 
            'Star 수:</strong> $1 <span class="text-warning">★</span>'
        );
        
        // 결과를 다시 설정
        resultContent.innerHTML = fixedHtml;
        
        // 프로젝트 섹션에 카드 클래스 추가
        const projectSections = resultContent.querySelectorAll('h3');
        projectSections.forEach(section => {
            const projectCard = document.createElement('div');
            projectCard.className = 'card project-card mb-4';
            
            // 섹션의 모든 내용을 카드로 이동
            const cardHeader = document.createElement('div');
            cardHeader.className = 'card-header py-3';
            cardHeader.appendChild(section.cloneNode(true));
            projectCard.appendChild(cardHeader);
            
            // 카드 본문 생성
            const cardBody = document.createElement('div');
            cardBody.className = 'card-body';
            
            // 다음 섹션까지의 모든 요소를 카드 본문으로 이동
            let currentElement = section.nextElementSibling;
            const elementsToMove = [];
            
            while (currentElement && currentElement.tagName !== 'H3') {
                const nextElement = currentElement.nextElementSibling;
                elementsToMove.push(currentElement);
                currentElement = nextElement;
                if (!currentElement) break;
            }
            
            elementsToMove.forEach(element => {
                cardBody.appendChild(element.cloneNode(true));
            });
            
            projectCard.appendChild(cardBody);
            
            // 원래 섹션 앞에 카드 삽입
            section.parentNode.insertBefore(projectCard, section);
            
            // 원래 요소들 제거
            section.remove();
            elementsToMove.forEach(element => {
                if (element.parentNode) {
                    element.remove();
                }
            });
        });
    }
});
