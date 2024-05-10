package com.blogzip.crawler.service

import org.junit.jupiter.api.Test

class HtmlCompressorTest {

    @Test
    fun compress() {
        val htmlCompressor = HtmlCompressor()
        val result = htmlCompressor.compress(
            "<![CDATA[<b><blockquote><p style=\"text-align:justify;\"><strong>[커리어 리팩토링: 개발자의 성장법] 9. 조현영 스모어톡 CTO 인터뷰</strong></p></blockquote><p style=\"text-align:justify;\">&nbsp;</p><figure class=\"table\" style=\"text-align:justify;\"><table style=\"border-bottom:none;border-left:none;border-right:none;border-top:none;\"><tbody><tr><td style=\"border-bottom:1pt solid hsl(0, 0%, 60%);border-left:1pt solid hsl(0, 0%, 60%);border-right:1pt solid hsl(0, 0%, 60%);border-top:1pt solid hsl(0, 0%, 60%);padding:5pt;\"><p style=\"text-align:justify;\">Editor’s note</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\">어떤 직업이든, 그 직업에 종사하는 사람들을 공통적으로 묶어주는 특징이 있지만, 막상 개개인을 들여다보면 업무 원칙이나 커리어, 성장에 관한 관점, 자신만의 노하우가 다 다릅니다. 개발자들도 마찬가지입니다. 개발을 시작하게 된 계기부터 다루는 기술스택, 도메인, 커리어와 성장에 대한 관점과 노하우 등은 모두 다릅니다. 요즘IT 기획 [커리어 리팩토링: 개발자의 성장법]을 통해 다양한 분야에 걸쳐 다양한 커리어를 다져온 개발자 한사람 한사람의 이야기를 직접 들어 보며, 이 시대 개발자들에게 다양한 성장의 길을 제안하고자 합니다.</p></td></tr></tbody></table></figure><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\">이번에 소개할 개발자는 스타트업의 CTO로 엑시트를 경험한 인물입니다. 그와 동시에 수강생 5.4만 명이 선택한 강의자, 구독자 3.6만 명을 모은 유튜버기도 하죠. 본업인 개발과 부업인 강의, 두 마리 토끼를 다 잡은 오늘의 주인공 ‘제로초’ 조현영 개발자입니다.</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\">지식공유자 ‘제로초’로 널리 알려져 있지만, 조현영 개발자는 본업에서도 독특한 이력을 쌓아가는 중입니다. “스타트업에서 1인분을 할 수 있는 사람”이 되고자 개발을 시작했다는 그는 군에서 홀로 자바스크립트를 익혔습니다. 지식인과 블로그, 외주로 실력을 쌓고는 곧 초기 스타트업의 1인 개발자이자 CTO로 무작정 합류하게 되었죠. “결국 잘 돌아가면 장땡이라는 마인드로 작동이 잘 되는 데 집중”한 개발자가 된 지 6년, 당시 CTO로 일하던 ‘오늘의 픽업'이라는 스타트업이 카카오모빌리티에 인수되며 창업자들이 꿈꾸는 엑시트를 경험했습니다.</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\">그러나 그는 엑시트의 결과로 얻은 대기업 파트장 자리를 2년 만에 내려놓고 다시 초기 스타트업으로 돌아왔습니다. 이런 대담한 도전에는 강의자로 쌓아온 경력이 큰 자산이 되었습니다. 어차피 “엑시트했을 때를 제외하고는 늘 강의가 주 수입”이었기 때문이죠. 그는 강의와 유튜브 활동뿐만 아니라 매해 책을 낼 정도로 활발하게 지식 공유를 이어오고 있습니다. 해외여행을 나가서도 밤이면 수강생 질문에 답변을 달 정도의 열정이 가장 큰 무기입니다.</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\">그는 효율성을 무엇보다 중요시하는 개발자이자 확실한 A/S를 보장하는 지식공유자입니다. 조현영 개발자에게 개발은 단순한 일이 아닌 자아실현의 도구이며 강의는 또 다른 성장의 방법이자 든든한 뒷받침입니다. 그에게 개발자와 강의자, 그 사이에서 균형을 잡으며 발전하는 법을 들어봤습니다.</p><p style=\"text-align:justify;\">&nbsp;</p><figure class=\"image image_resized\" style=\"width:100%;\"><img src=\"https://yozm.wishket.com/media/news/2547/image3.jpg\"><figcaption>‘제로초' 조현영 스모어톡 CTO &lt;사진: 요즘IT&gt;</figcaption></figure><p style=\"text-align:justify;\">&nbsp;</p><figure class=\"table\" style=\"text-align:justify;\"><table style=\"border-bottom:none;border-left:none;border-right:none;border-top:none;\"><tbody><tr><td style=\"border-bottom:1pt solid hsl(0, 0%, 60%);border-left:1pt solid hsl(0, 0%, 60%);border-right:1pt solid hsl(0, 0%, 60%);border-top:1pt solid hsl(0, 0%, 60%);padding:5pt;\"><p style=\"text-align:justify;\"><strong>첫 프로그래밍:</strong> 2014년</p><p style=\"text-align:justify;\"><strong>첫 언어:</strong> 자바스크립트</p><p style=\"text-align:justify;\"><strong>좋아하는 장비:</strong> 맥북 프로</p><p style=\"text-align:justify;\"><strong>특이사항:</strong> 효율성을 매우 좋아해 적은 시간 대비 최대 산출물을 지향한다. 하지만 또 귀찮음도 많고 생각보다 비효율적으로 일한다. 단축키도 거의 안 외운다.</p><p style=\"text-align:justify;\"><strong>대학 전공:</strong> 경영학과/컴퓨터학과(미복학제적)</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\"><strong>주요 활동 이력:</strong> 2017년, 외주 회사에서 개발자로 업무를 시작해 곧 MYFIT이라는 스타트업의 CTO로 합류했다. 이후 2020년까지 비정기적으로 CTO 업무를 진행했다. 그 사이 Anothers 리드 프로그래머, GIBLIB 프리랜서 개발자, 글로벌 오픈소스 SW 전담 개발자 등으로 경력을 쌓았다. 이후 오늘의픽업 CTO로 합류해 인수를 통한 엑시트를 경험했다. 카카오모빌리티에서 풀필먼트개발팀 당일배송파트장으로 재직하다 2023년 스모어톡의 CTO를 맡았다. ‘제로초’라는 이름으로 인프런 자바스크립트 강의, 유튜브 활동을 하며 『코딩 자율학습 제로초의 자바스크립트 입문』 등 4편의 저서를 집필했다.</p></td></tr></tbody></table></figure><div class=\"page-break\" style=\"page-break-after:always;\"><span style=\"display:none;\">&nbsp;</span></div><h3 style=\"text-align:justify;\"><strong>혼공 비전공자에서 CTO로 엑시트하기까지</strong></h3><p style=\"text-align:justify;\"><strong>Q. 비전공자 개발자로 출발해 경력 초기부터 스타트업의 CTO라는 큰 직책을 맡았어요. 어떻게 경력을 시작하게 되었나요?</strong></p><p style=\"text-align:justify;\">스타트업에서는 창업자들끼리 CEO, COO, CTO 등 C레벨의 직책을 나눠 가지잖아요. 막 창업한 스타트업에 합류했는데, 개발자라고는 저밖에 없으니까 CTO 달고 일한 게 시작이었어요. 처음 CTO로 일한 스타트업은 ‘MYFIT’이란 곳이었죠. 옷과 체형을 비교해 사이즈가 맞는지 예측해 주는 그런 서비스를 준비했어요. 당시 공동 대표 2명, 의류 디자이너 1명, 저까지 4명이 다였거든요. 할 줄 아는 건 없는데 혼자 모든 걸 해야만 하니 정말 어려웠어요. 그렇다고 제가 해내지 못하면 답이 없었고요. “잘 돌아가면 장땡이다”라는 마인드로 작동이 잘 되게 만드는 데 집중했어요. 그렇게 서비스를 론칭하고 운영하게 되었죠. 막연한 아이디어가 제품이 되고 실제로 사람들이 쓰기도 하니 신기했어요. 쾌감도 있었고요. 여태 한 모든 일 중 그때가 제일 열심히 한 일 같아요. 밤이고 새벽이고 에러 나면 일어나서 고칠 정도로 절박하게 달려들었죠. 그러다 보니 자연스레 개발 실력도 늘고 스타트업에 대한 생각도 정립이 된 것 같아요.</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\"><strong>Q. 대학에서 경영학을 전공하다 개발자로, 그것도 극초기 스타트업에서 경력을 시작하게 된 계기가 있을까요?</strong></p><p style=\"text-align:justify;\">고등학교 때부터 스타트업 창업을 꿈꿨어요. 경영학과도 그런 맥락으로 선택했죠. 대학 수업을 듣다 보니 창업보다는 일반 회사에서 일하는 법만 가르쳐 주더라고요. 무언가 좀 더 창업을 위한 공부를 하고 싶었어요. 대신 제가 아이디어는 약한 편이니 대표보다는 공동 창업자로 할 만한 걸 찾았죠. 그러다 프로그래밍을 만났어요. 당시에도 스타트업이 다루는 서비스는 대부분 웹 아니면 모바일 앱으로 나왔으니까요. 마침 제가 수학을 좋아하기도 했고요. 그래서 일단 교양 수업으로 프로그래밍을 선택했어요. 아주 간단한 게시판 만들기를 배웠는데 정말 재미있더라고요. 전공 수업보다 성적도 아주 잘 나왔고요. 그래서 무작정 이중 전공을 신청하고는 군대에 갔어요. 전역하고 집중할 목적으로요. 카투사에서 군 생활을 하다 보니 자유 시간이 조금 생겨 그때부터 개발을 제대로 공부했어요.</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\"><strong>Q. 혼자, 군대에서 공부하는 것이 쉽지 않았을 텐데요. 어떤 방법으로 공부했나요?</strong></p><p style=\"text-align:justify;\">아무래도 군이다 보니 기기 활용이 자유롭지 않아 C++ 같은 언어를 써볼 수가 없었어요. 그래서 자바스크립트를 접하기 시작했죠. 브라우저만 있어도 다룰 수 있는 프로그래밍 언어였으니까요. 마침 서버에 DB까지 다 쌓을 수 있다 하니, 이 언어 하나만 알아도 이것저것 다 만들 수 있겠다 싶어 제대로 파기 시작했어요. 그런데 인터넷에서 정보를 찾아 공부하다 보니 무언가 부족한 거예요. 한계가 있었죠. 그래서 지식인이랑 블로그 활동을 했어요. <strong>(지식인에 질문을 올린 건가요?)</strong> 아니요. 제가 답을 달았죠. 저도 검색하다 지식인을 종종 보게 되었는데요, 나오는 질문이 다 비슷하더라고요. 그래서 적어도 여기 올라오는 질문에 대한 답은 다 알아야겠다 싶었어요. 모르는 문제가 있으면 답을 찾아 공부해서라도 답변을 달았죠. 그러다 보니 채택된 답변이 천 개가 넘어 ‘신’ 등급이 되었어요. 블로그도 비슷한 용도로 썼어요. 일단 글을 쓰고 커뮤니티에 공유했죠. 이건 틀렸다, 저건 틀렸다 답변도 많이 달렸는데 틀린 부분을 수정하는 것 역시 공부가 되더라고요.</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\"><strong>Q. 그래도 학습과 실전은 다르다고 하잖아요. 실제 개발 경험은 어떻게 쌓았나요?</strong></p><p style=\"text-align:justify;\">외주를 통해서요. 저는 외주를 정말 많이 했어요. 군을 전역한 다음 처음 일한 회사도 친구가 차린 외주 회사였어요. 프론트엔드 개발을 맡았는데, 혼 많이 나면서 배웠죠. MYFIT에서 CTO로 일할 때도 외주는 꾸준히 했어요. 제가 사실 그때 무급으로 일했거든요. 세상 물정 하나도 모르고 열정만 있었으니까. 그래도 먹고는 살아야 하니 외주로 수익을 냈어요. 다행히도 제가 개발 속도가 좀 빠른 편이라 남들보다 훨씬 수익이 좋은 편이었어요. 여러 차례 하다 보니 노하우도 생겼고요. 개발뿐만 아니라 클라이언트와 소통하기, 요구사항 정의하기 같은 경험도 쌓였죠. 계약 조건을 꼼꼼하게 따지는 게 아주 중요한데, 제 나름대로 쓰는 계약서 템플릿도 생겼고요.</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\"><strong>Q. MYFIT에서 시작해 여러 스타트업과 외주 개발을 거쳐 ‘오늘의픽업’에 합류했어요. 이 회사에는 어떻게 합류하게 되었나요?</strong></p><p style=\"text-align:justify;\">첫 회사 외에도 초기 스타트업 여러 곳에서 일했어요. 대여섯 번 망했죠. 비정기적으로 참여하던 MYFIT에서도 완전히 손을 뗐어요. 2020년 쯤이었는데, 피로가 많이 쌓였더라고요. 번아웃이 세게 왔죠. 그렇게 스타트업 일은 계속 안 풀리는데, 부업으로 하던 강의는 잘 되고 있었어요. 그 참에 아예 스타트업은 그만하고 전업 강사를 할까 생각했죠. 그때 고등학교 동창이 창업을 했는데 개발자가 필요하다며 연락을 했어요. 당일 배송을 아이템으로 하는 회사라고요. 친구의 설득에 마지막이다 생각하고 해 보기로 한 거죠. 친구랑 하니까 그나마 스트레스가 덜하지 않을까 싶기도 했고요. 그런데 6개월 만에 다시 번아웃이 왔어요. 한번 온 스트레스를 제대로 풀지 않고 덮어 두다 보니 회복이 안 되더라고요. 그 시점에 대표님들의 배려로 한 달 반 정도 푹 쉴 수 있었어요. 좀 쉬고 오니 다시 달릴 수 있었죠.</p><p style=\"text-align:justify;\">&nbsp;</p><figure class=\"image image_resized\" style=\"width:100%;\"><img src=\"https://yozm.wishket.com/media/news/2547/image1.png\"><figcaption>오늘의픽업 동료들과 함께. &lt;출처: 조현영&gt;</figcaption></figure><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\"><strong>Q. ‘오늘의픽업’은 꽤 빠른 시점에 카카오모빌리티에 인수되었어요. 스타트업에서 엑시트란 흔히 할 수 있는 경험이 아니잖아요. 당시 팀 내에서는 어떤 논의가 오갔나요?</strong></p><p style=\"text-align:justify;\">스타트업은 투자를 받는 시점이 언제나 힘들잖아요. 피곤하기도 하고 사람들도 예민해지고. 게다가 오늘의픽업이 하는 당일 배송 서비스는 적자 구조를 개선하기 어려운 사업이었어요. 망할지도 모른다는 불안감이 늘 있었죠. 2021년 연말, 오늘의픽업에는 두 가지 선택지가 생겼어요. 하나는 시리즈 A 투자를 받는 것, 다른 하나는 카카오 모빌리티의 인수였죠. 재밌는 에피소드가 있는데요, 경영진이 결정하기 앞서 팀원 모두 모여 거수를 한 번 했어요. 기업 규모를 더 키울까, 아니면 여기서 멈출까, 선택지를 본 거죠. 당시 사무실에 있던 직원 스무 명 정도가 참여했는데, 모두 인수에 손을 들더라고요. 거수가 결정과 이어진 건 아니지만 구성원들의 마음을 알 수 있었던 일이였어요. 결국 인수 쪽으로 진행이 되었어요.</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\">&nbsp;</p><h3 style=\"text-align:justify;\"><strong>대기업을 그만두고 다시 스타트업으로</strong></h3><p style=\"text-align:justify;\"><strong>Q. 오늘의픽업 팀과 함께 카카오모빌리티라는 대기업에 합류하게 되었어요. 그동안 익숙했던 스타트업과 무엇이 가장 달랐나요?</strong></p><p style=\"text-align:justify;\">기본적인 프로세스가 있다는 점이었죠. 특히 개발자의 관점에서 문서화가 잘 되어 있어 좋았어요. 솔직히 서비스를 구성하는 코드가 아주 특별하지는 않았거든요. 오히려 교과서적인 코드 활용에 가까웠죠. 그보다는 문서를 어떤 체계로 정리하는지, 개발팀과 비개발팀이 어떻게 일을 시작하는지 이런 부분을 많이 배웠어요. 회의록 작성이나 인수인계 방법도 볼 수 있었고요. 외적으로는 복지도 좋고 안정적인 환경도 편했죠. 스타트업에서 미처 신경 쓰지 못했던 규제 관련 부분도 기존 서비스에 많이 추가되었고요.</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\"><strong>Q. 하지만 그런 안정적인 환경을 뒤로 하고 다시 스타트업으로 돌아왔어요. ‘스모어톡’이란 기업인데요, 어떤 배경으로 합류하게 되었나요?</strong></p><p style=\"text-align:justify;\">카카오모빌리티에 합류한 지 얼마 안 있어 규제 등 이슈로 사업들이 홀딩되었어요. 당일 배송 서비스도 확장을 억제했고요. 체계를 익히고 배우는 건 좋았지만, 제가 새로 코딩하며 무언가 만들 일은 많지 않았던 거죠. 그렇게 지내도 괜찮을지 고민하던 중에 지인에게 연락을 받았어요. 스모어톡이란 회사를 창업했는데, 아이템이 ‘생성형 AI’고 개발자를 구한다고요. 이미지 생성 AI였는데, 레퍼런스 이미지를 넣으면 비슷한 스타일로 여러 이미지를 만들어 주는 서비스였죠. AI가 요즘 워낙 핫하잖아요. MYFIT에서도 ML 기반 예측 모델을 적용했는데, 그동안 기술이 어느 정도 발전했는지 궁금하기도 했고요. 그렇게 그냥 과감하게 사직서를 냈어요. 보상도 포기하고요.</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\"><strong>Q. 생성형 AI 기반 서비스는 지금 가장 주목 받는 시장이잖아요. 직접 서비스를 만들어보니 어땠나요?</strong></p><p style=\"text-align:justify;\">AI 기반 서비스는 크게 두 가지 허들이 있다고 느꼈어요. 우선 AI로 서비스 잘 만들기가 힘들고, 잘 만들었다 해도 수익을 내는 건 더 힘들다는 거죠. 프로그래밍할 때는 에러는 에러고 버그는 고치면 된다, 이렇게 명확했거든요. 그런데 AI는 또 달라요. 이를테면 AI 모델의 정확도가 93%, 94% 이렇게 나와요. 그럼 늘 6~7%의 오류를 염두에 두어야 하는 거죠. 이게 작아 보이지만 그렇지 않아요. 스모어톡은 이미지를 생성해 주는 서비스인데요, 100장 중에 7장이 이상한 이미지라면 문제가 크겠죠. 한 장 한 장이 다 돈이니까요. 여기서 정확도 1%를 올리려면 비용이 10배씩 늘어나기도 해요. 그러다 보니 또 수익화 문제로 이어지고요. AI 모델 구축에 드는 비용이 아주 큰데, 고객이 과연 이런 것까지 감수하고 높은 비용을 낼까 싶은 거죠. 당장 최근 한국에서도 어느 정도 알려진 AI 서비스는 있어도 흑자를 내는 기업은 거의 없으니까요.</p><p style=\"text-align:justify;\">&nbsp;</p><figure class=\"image image_resized\" style=\"width:100%;\"><img src=\"https://yozm.wishket.com/media/news/2547/image2.jpg\"><figcaption>스모어톡 동료들과 함께. 사진 왼쪽이 ‘제로초' 조현영 CTO. &lt;출처: 스모어톡 홈페이지&gt;</figcaption></figure><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\"><strong>Q. 확실히 쉽지 않겠네요. 이런 어려움에도 다시 스타트업에서 도전하는 이유는 무엇일까요?</strong></p><p style=\"text-align:justify;\">스타트업의 매력이라고 하면 내가 큰 영향력을 가질 수 있다는 거예요. 대기업에서 일할 때는 안정되어 있었지만 가끔은 부품처럼 일한다고 느껴졌어요. 제가 어찌할 수 없는 문제로 일이 엎어지기도 했죠. 스타트업은 달라요. 무엇보다 내가 만든 코드가 제품, 그리고 고객에게 미치는 임팩트가 훨씬 강해요. 그래서 무언가 ‘진짜’ 일을 하고 있구나, 해내고 있구나 그런 감을 얻을 수 있어요. 또 그만큼 저절로 책임감도 커져요. 오늘의픽업에서 일할 때는 라이더용 앱에 버그가 나면 실시간으로 욕이 막 올라오는 게 보였어요. 필터 없이 개발자한테 바로 이런 상황이 전달되니까요, 저절로 내가 제품에 큰 영향을 주는 구나 생각하게 되죠.</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\"><strong>Q. 책임감. 그만큼 스타트업에서 일할 때는 주인 의식을 가져야 한다, 이런 이야기도 많이 하잖아요. 스타트업, 특히 초기 단계 스타트업에서 개발자로 일하려면 무엇이 중요할까요?</strong></p><p style=\"text-align:justify;\">솔직히 주인 의식이란 단어 많이 쓰는데, 저는 그보다는 책임감이라고 말해요. 그런데 이게 남들 말하듯 회사를 위한 책임만은 아니에요. 나 자신을 위해서도 책임감이 필요하죠. 이런 마음가짐이 없는 개발자들은 코드를 대충 짜고 그게 습관이 되어요. 예를 들어 스타트업에서는 제품 방향이 자주 바뀌어요. 내가 짠 코드를 6개월 뒤에 다시 크게 바꿔야 하는 상황도 오죠. 책임감 없이 대충 코드를 짜면 그게 다 고통으로 돌아온다는 뜻이요. 그래서 팀원들한테 “나 자신을 위해서라도 코드를 더 깔끔하게 짜라” 말해요. 창업자 입장에서도 같아요. 스타트업에 온 사람 모두가 평생 그 회사에만 머물지는 않잖아요. 그 사람도 잘 되고 회사도 잘 되려면 좋은 코드가 필요해요. 그래서 개발 팀원에게 코드를 고민할 시간, 발표하고 토론하는 시간을 충분히 주려고 해요. 이런 방법은 단기적으로는 비효율일 수 있지만, 장기적으로는 시간을 절약하는 길이예요. 만약 그 팀원이 떠난다 해도 회사에는 수정과 확장이 편한 좋은 코드가 남으니까요. 팀 전체, 회사 전체의 효율성을 확보하는 거죠.</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\">&nbsp;</p><h3 style=\"text-align:justify;\"><strong>인기 강의자 제로초</strong></h3><p style=\"text-align:justify;\"><strong>Q. 이제 개발자 조현영이 아닌 강의자 ‘제로초’ 이야기를 해볼까해요. 제로초님은 업계에서 유명한 강의자죠. 인기도 많고요. 비결이 뭘까요?</strong></p><p style=\"text-align:justify;\">선점 효과가 크죠. 제가 2018년에 처음 강의를 시작했을 때는 강의자가 그리 많지 않았어요. 개발 강의 플랫폼인 인프런에도 아마 초창기에 강의를 올리던 사람 중 하나였을 거예요. 그렇게 팬이나 인지도가 쌓인 시간이 6년을 넘어가다 보니 여기까지 왔지 싶어요. 처음에는 무료 강좌를 많이 연 게 도움이 되었어요. 강좌를 만들어 두고 커뮤니티에 많이 알리려 노력했죠. 지금도 제 입문 강좌는 다 무료인데요. 커리큘럼을 입문부터 시작해 순서대로 로드맵 맞춰 따라오도록 구성했어요. 입문 강의를 보고 잘 맞다 생각하는 분들이 끝까지 제 강의를 들을 수 있도록요.</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\"><strong>Q. 지금은 개발 강의가 꽤 많아요. 경쟁도 치열하죠. 선점만으로 꾸준히 인기를 유지하고 성장하는 건 어려웠을 텐데요. 다른 특징은 무엇이 있을까요?</strong></p><p style=\"text-align:justify;\">아무래도 빠른 A/S를 장점으로 많이들 봐주세요. 강의를 자주 수정하거나 빠르게 질문에 답하는 걸 A/S라고 표현하는데요, 이런 소통이 중요하다고 봐요. 사실 제가 인프런 답변왕이거든요. 해외여행을 가도 밤에 숙소 돌아오면 그날 쌓인 QnA 전부 답변을 달아요. 질문 중에 생각지도 못한 부분이 나오면 모아서 기존 수강생들에게 메일도 보내고요. 제가 운영하는 슬랙 커뮤니티, 유튜브 커뮤니티에도 공유하죠. 강의만 전달하는 게 아니라 이를 보는 사람들이 진짜 도움을 얻으면 좋겠다, 그런 생각을 많이 했죠. 그래서 취업 관련 고민 상담 같은 콘텐츠도 찍어보고 해요. 그렇게 제로초 강의 덕분에 취업했다는 후기가 달리면 저한테도 도움이 되니까요.</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\"><strong>Q. 제로초 강의를 들은 수강생이 인프런에서만 5만 4천 명이 넘었어요. 이렇게 쌓인 수강생의 커뮤니티가 다시 강의에 도움을 주기도 하나요?</strong></p><p style=\"text-align:justify;\">제 강의의 노트는 수강생들이 만들어줘요. 수강생 입장에서 강의를 듣다 보면 아무래도 의지가 부족할 때가 있잖아요. 그래서 매주 강의를 정리한 노트를 만들어 올리도록 독려해요. 꾸준히 강의가 끝날 때까지 노트를 정리한 분들한테는 선물도 보내고요. 특히 새로 연 강의를 처음 듣는 기수에게 신경을 쓰죠. 그렇게 한 기수 돌고 나면 노트가 쌓여요. 그럼 그 노트를 다시 다음 기수한테 공유해요. 수강생들은 끝까지 집중해 강의 들으니 좋고, 저는 강의에 부가로 제공할 수 있는 콘텐츠가 생겼으니 좋은 거죠. 이렇게 서로 윈윈할 수 있는 효율적인 구조를 만드는 걸 좋아해요.</p><p style=\"text-align:justify;\">&nbsp;</p><figure class=\"image image_resized\" style=\"width:100%;\"><img src=\"https://yozm.wishket.com/media/news/2547/image5.jpg\"><figcaption>제로초 인프런 페이지(왼쪽)와 유튜브 채널 &lt;출처: 인프런, 유튜브&gt;</figcaption></figure><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\"><strong>Q. 그럼 지금은 새로운 강의자가 시장에 들어오기 어려운 환경일까요?</strong></p><p style=\"text-align:justify;\">처음보다는 쉽지 않죠. 그래서 전략이 필요하다고 봐요. 특히 셀프 브랜딩을 많이 해야 해요. 요즘에는 사람들이 강의를 올렸다고 알아서 찾아 듣지 않아요. 이미 각 분야에 제일 유명한 사람이 다 정해져 있거든요. 기존에 듣던 강사가 3만 원짜리 자바 강의를 냈는데, 처음 보는 강사가 같은 강의를 똑같이 3만 원에 팔면 당연히 안 듣겠죠. 그럼 처음에는 이벤트로 1천 원에 뿌려 보는 거예요. 맛보기 강의 듣고 재미있다 느껴야만 다음 강의로 넘어갈 거예요. 강의 만드는 일에는 시간이 정말 많이 들어가요. 처음 찍으면 더 힘들고요. 그렇게 고생해서 이왕 시작한 일이니, 이런 전략을 꼭 가지고 가면 좋죠.</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\"><strong>Q. 강의라는 활동 자체는 어떻게 생각하세요? 이렇게 쉽지 않은 시장에서도 노력을 기울여야 할 가치는 어디에 있을까요?</strong></p><p style=\"text-align:justify;\">물론 어렵다는 얘기 많이 했지만, 그럼에도 저는 기본적으로 추천해요. 우선 이 활동 자체가 하나의 스펙이 되어요. 강의 이력이 있으면 “아, 이 사람이 그래도 이 기술에는 자신이 있구나” 추측하게 되죠. 블로그나 오픈 소스로 알려진 개발자들도 정말 많잖아요. 이름이나 닉네임이 익숙한 사람이 회사에 지원하면 아무래도 채용 과정에서 도움이 될 거고요. 또 강의를 통해 자연스레 그 분야에 대해 더 잘 알게 돼요. 강의할 때는 하나라도 모르는 게 있으면 많이 막혀요. 수강생 질문 중에 날카로운 것도 많으니 답변을 준비하다 보면 실력이 늘어요. 자기 학습에도 도움을 받는 거죠.</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\"><strong>Q. 그렇다면 수익 창출은 어떤가요? 강의가 개발자 커리어의 한 방향이 될 수 있을까요?</strong></p><p style=\"text-align:justify;\">지금도 제 주 수입은 강의 수익이에요. 사실 엑시트했을 때를 제외하고는 늘 강의로 돈을 더 벌었어요. 요즘도 스타트업에 다니며 보상은 거의 포기했기 때문에 강의로 생활을 유지하고 있어요. 애초에 강의를 시작한 계기도 부업 목적이 컸어요. 다만 이때는 개발자와 조금 다른 능력이 필요해요. 저는 콘텐츠 크리에이터의 역량에 가깝다고 생각해요. 솔직히 초중고 선생님들이 각 분야에 있어 엄청난 전문가는 아니잖아요? 대신 학생에게 지식을 잘 전달하는 능력을 가진 거죠. 반면 대학교수들은 대부분 뛰어난 전문가지만, 수업 시간에는 학생들이 졸기도 하고 그렇잖아요. 시장이 포화된 지금은 그런 능력이 더 중요해요. 자기 콘텐츠가 시장을 비집고 들어갈 수 있을지 봐야죠. 말투가 아주 웃겨서 재미있다든가 하는 식으로요.</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\"><strong>Q. 꽤 많은 수익을 창출할 수 있다면 강의를 사업으로 더 발전 시켜볼 고민은 한 적 없나요? 요즘 유튜버들이 회사를 세우고 운영하는 것처럼요.</strong></p><p style=\"text-align:justify;\">그런 생각도 해본 적 있죠. 최근에는 글로벌 강의 플랫폼 쪽으로 컨택도 해봤어요. 한국은 점점 시장이 제한된다고 느껴서요. 자체 플랫폼을 만들어서 판매하는 것도 고려해 봤고요. 그렇게 되면 정말 회사를 차려서 하는 게 좋아요. 제가 잘 모르는 마케팅이나 판매 부분도 있고요. 다만 저는 아직 강의를 부업이라 생각하고 있어요. 제 본업은 스타트업에서 개발하는 거죠. 그래서 지금 하고 있는 스모어톡이 잘 되는 것을 우선 신경 쓰고 있어요.</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\"><strong>Q. 다음 지식 공유 활동은 어떻게 이어나갈 계획인가요? 제로초 강의를 기다리는 분들을 위해 알려 주세요.</strong></p><p style=\"text-align:justify;\">유튜브 구독자를 모으는 건 제 취미예요. 최근 개발 트렌드 관련 숏츠를 좀 만들었는데 반응이 좋아서 더 만들어볼 계획이에요. 최근에는 책도 새로 나왔어요. 『코딩 자율학습 제로초의 자바스크립트 입문』이라는 제목인데요, 이렇게 입문 관련 콘텐츠를 만드는 게 제일 어렵더라고요. 입문자들은 프로그래머처럼 사고하지 않으니까요. 처음 배울 때는 언어에 대한 집착을 버려야 해요. 생각하는 구조 자체를 바꾸는 것이 더 중요하죠. 그래서 책도 언어보다 프로그래밍 훈련에 초점을 맞춰 구성했어요. 그다음으로는 파이썬 강의도 염두에 두고 있어요. 사실 자바스크립트 쪽은 이제 거의 다 다뤘거든요. 물론 AI 연구까지는 어렵겠지만, 데이터 엔지니어링 정도는 강의로 풀어볼 수 있지 않을까 보고 있습니다.</p><p style=\"text-align:justify;\">&nbsp;</p><figure class=\"table\"><table><tbody><tr><td style=\"background-color:#EEE1FF;border-bottom:1px solid hsl(0, 0%, 90%);border-left:1px solid hsl(0, 0%, 90%);border-right:1px solid hsl(0, 0%, 90%);border-top:1px solid hsl(0, 0%, 90%);text-align:center;\"><p style=\"text-align:center;\"><strong>요즘IT X(구 트위터)에서 진행하는 북 이벤트 참여하고</strong><br><strong>‘코딩 자율학습 제로초의 자바스크립트 입문’ 읽어 보세요!</strong></p><p style=\"text-align:center;\"><a href=\"https://x.com/yozm_it/status/1780405555124973707\"><strong>▶ 참여하기</strong></a><strong>(~04.25일까지)</strong></p></td></tr></tbody></table></figure><p style=\"text-align:justify;\">&nbsp;</p><h3 style=\"text-align:justify;\"><strong>대체되지 않는 개발자</strong></h3><p style=\"text-align:justify;\"><strong>Q. 최근 유튜브 채널에 올린 조언 영상(</strong><a href=\"https://www.youtube.com/watch?v=VTrwObhwaT8\"><strong><u>개발자 정신교육 1편</u></strong></a><strong>)을 재미있게 봤습니다. 입문 강의를 많이 하는 강의자답게 예비 개발자를 위한 양질의 조언이 가득했는데요. 그중 프로그래머에 대한 정의가 인상 깊었어요.</strong></p><p style=\"text-align:justify;\">네, 저는 프로그래머를 “문제를 해결하는 사람”으로 정의해요. 단순히 코딩하는 사람이 아니라요. 특히 AI 시대에서 이런 준비가 더 중요하다고 봐요. AI에 대체되는 게 아니라 활용하는 사람이 되어야 해요. 앞으로는 경쟁이 심해질 거예요. 실제 꽤 많은 개발자가 AI로 대체되기도 할 거고요. 그렇기 때문에 기계적으로 일하면 안 돼요. 만약 회사에서 반복 작업, 특히 내가 아니어도 할 수 있는 반복 작업 위주로 하고 있다면 경각심을 가져야 합니다. 창의성을 길러야 해요.</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\"><strong>Q. 창의성. 프로그래머에게 창의성은 어떤 부분에서 나타날까요?</strong></p><p style=\"text-align:justify;\">제가 희망을 가지는 부분은 프로그래밍에 “정답이 없다”는 점이에요. 정답이 없다는 말은 다른 말로 스스로 정답을 만들어 낼 수도 있다는 거죠. 이때 창의성을 발휘할 수 있어요. 내 답이 왜 문제 해결에 가까운지 설명하는 논리력도 필요하고요. 또 AI를 활용할 때도 창의성이 필요해요. 이제 혼자 생각해서 업무하는 건 효율성이 떨어져요. 가상의 존재이기는 하지만 AI와 대화하며 풀어내야죠. 챗GPT를 쓰다 보면 질문을 정형화해서 하기 어려워요. 살살 어르고 달래며 답을 얻어내야 하는데, 그러니 질문을 잘해야 해요. 이런 부분도 창의성의 영역이라 볼 수 있고요.</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\"><strong>Q. 업무에서 창의성을 기르려면 어떻게 하면 좋을까요? 특히 주니어 개발자에게 좀 더 직접적인 조언을 해주실 수 있을까요?</strong></p><p style=\"text-align:justify;\">주니어에서 시니어가 되려면 반복 작업이 아닌 “의사결정을 얼마나 많이 하는가?” 이 부분을 따져 보는 게 어떨까 싶어요. 5년 차가 넘었는데 반복 작업만 하고 있다, 그러면 빨간불이 켜진 상황이거든요. 앞으로 사람은 AI가 여러 선택지를 주면 고르는 역할을 더 많이 할 거라고 봐요. 이제 왜 그런 선택을 했는지, 고민하고 설명할 수 있는 그런 영역으로 업무를 조금씩 옮겨가는 것이 좋을 듯해요.</p><p style=\"text-align:justify;\">&nbsp;</p><figure class=\"image image_resized\" style=\"width:100%;\"><img src=\"https://yozm.wishket.com/media/news/2547/image4.jpg\"><figcaption>‘제로초' 조현영 CTO &lt;사진: 요즘IT&gt;</figcaption></figure><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\"><strong>Q. 그렇다면 현영 님은 AI를 어떻게 활용하고 계신가요?</strong></p><p style=\"text-align:justify;\">강의 관련 작업할 때 특히 잘 쓰고 있어요. 자막 만들기, 기본 컷편집 등에 도움을 정말 많이 받아요. 처음 시작할 때는 컴퓨터 앞에 앉아 종일 편집을 했어요. 강의를 12시간 분량으로 만들었으면 12시간은 기본 편집에 쏟아야 했죠. 그런데 이제 AI 도구가 어색한 부분을 알아서 잘라줘요. 또 제가 댓글로 답변을 많이 다는데, 매 강의마다 자주 올라오는 질문이 많았거든요. 원래 답변을 찾아 알려주는 것이 힘들었는데, 이제 인프런 플랫폼에 도입된 AI가 제 답변을 학습하고 유사한 질문에는 대신 답을 달아줘요. 이 기능을 아주 편하게 쓰고 있어요. 시간을 정말 많이 아껴주었죠. 또, AI 관련 코딩을 할 때 챗GPT의 도움을 얻으며 코드를 작성하고 있습니다. 파이썬 문법은 자바스크립트만큼 익숙하지 않은데, 큰 도움이 되더라고요.</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\"><strong>Q. 꽤 긴 시간 대화를 했는데요. 대화하다 보니 현영 님의 자신감을 많이 느낄 수 있었어요. 어쩌면 이토록 다양한 활동을 성공적으로 병행한 건 자신감 덕분이 아닐까 합니다.</strong></p><p style=\"text-align:justify;\">처음부터 저도 자신감이 넘치지는 않았어요. 당연히 불안하고 이게 맞나 싶은 순간 투성이였죠. 그런데 스타트업에서 혼자 개발할 때는 그렇게 불안할 틈도 없더라고요. 당장 뭐라도 해야 했고, 어떻게든 기능이 돌아가면 한시름 놓는 순간의 반복이었죠. 그렇게 경력이 쌓이다 보니 제가 짠 코드로 엑시트까지 경험하게 되었고요. 초기에 자신감이 없는 건 인터넷 여기저기서 모은 지식과 방법이 맞는지 확신할 수 없기 때문이잖아요? 그럴 수밖에 없다고 생각해야 해요. 모두 정답이거든요. 앞서 프로그래밍에 정답이 없다고 했는데, 정말 상황마다 정답이 달라져요. 한 때 정답이었던 것이 정답이 아니게 되기도 하고요. 그러니 불안해하고 고민하기보다 일단 돌아가게 만들고 그다음 더 좋은 코드를 고민해도 늦지 않아요. 이런 경험을 쌓다 보면 자신감도 생기고요.</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\"><strong>Q. 앞으로도 이런 자신감이 유지될까요? 스타트업 CTO로, 또 강의자로 현영 님은 어떤 미래를 그리고 있나요?</strong></p><p style=\"text-align:justify;\">여전히 자신감만큼 불안감도 커요. 스타트업은 언제나 실패할 확률이 있잖아요. 그래서 플랜 B를 세워야만 하죠. 강의도 비슷해요. 한계에 다다른 시장이나 판매량도 생각해야 하니 고민이 많죠. 이런 환경에서 일하다 보니 계획이 수포로 많이 돌아가더라고요. 처음에는 일하던 스타트업이 잘 안될 때마다 번아웃이 왔어요. 이제는 이런 감정 소모를 좀 줄이려고 해요. 그래서 큰 방향성만 잡아두고 세부적인 건 그때그때 대처하려고 해요. 우선 스타트업과 개발은 제게 자아실현에 가까워요. 내 자아를 찾듯이 최선을 다해야겠다, 대신 안 풀려도 결과에 너무 연연하지 말자, 그렇게 생각하려 노력해요. 강의는 우상향에 초점을 맞췄어요. 앞으로도 수치가 떨어지지 않는 선에서 다양하게 해볼 예정입니다. 정신력이 허락하는 두 가지 일 모두 해낼 거예요.</p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"text-align:justify;\">장대청 에디터 <a href=\"mailto:jdc@wishket.com\">jdc@wishket.com</a></p><p style=\"text-align:justify;\">&nbsp;</p><p style=\"margin-left:0px;text-align:center;\"><span style=\"color:rgb(153,153,153);\">요즘IT의 모든 콘텐츠는 저작권법의 보호를 받는 바, 무단 전재와 복사, 배포 등을 금합니다.</span></p></b>]]>"
        )
        println(result)

        val result2 = htmlCompressor.compress(
            ""
        )
        println(result2)
    }
}